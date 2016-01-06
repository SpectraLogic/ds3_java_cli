package com.spectralogic.ds3cli.command;

import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.PerformanceResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.DeleteBucketRequest;
import com.spectralogic.ds3client.commands.DeleteObjectRequest;
import com.spectralogic.ds3client.helpers.DataTransferredListener;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.ObjectCompletedListener;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.security.SignatureException;
import java.util.List;

public class Performance extends CliCommand<PerformanceResult>{
    private String bucketName;
    private String numberOfFiles;
    private String sizeOfFiles;

    public Performance(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The performance command requires '-b' to be set.");
        }

        numberOfFiles = args.getNumberOfFiles();
        if (numberOfFiles == null) {
            throw new MissingOptionException("The performance command requires '-n' to be set.");
        }

        sizeOfFiles = args.getSizeOfFiles();
        if (sizeOfFiles == null) {
            throw new MissingOptionException("The performance command requires '-s' to be set.");
        }

        return this;
    }

    @Override
    public PerformanceResult call() throws Exception {
        final Ds3ClientHelpers helpers = Ds3ClientHelpers.wrap(getClient());
        final int numberOfFiles = Integer.valueOf(this.numberOfFiles);
        final long sizeOfFiles = Integer.valueOf(this.sizeOfFiles);

        try {
            helpers.ensureBucketExists(this.bucketName);

            final List<Ds3Object> objList = Lists.newArrayList();
            for (int i = 0; i<numberOfFiles ; i++) {
                final long testFileSize = sizeOfFiles * 1024L * 1024L;
                final Ds3Object obj = new Ds3Object("file_" + i, testFileSize);
                objList.add(obj);
            }

            /**** PUT ****/
            final Ds3ClientHelpers.Job putJob = helpers.startWriteJob(this.bucketName, objList);
            final PerformanceListener putPerformanceListener = new PerformanceListener(
                    System.currentTimeMillis(),
                    numberOfFiles,
                    numberOfFiles * sizeOfFiles,
                    true);
            putJob.attachObjectCompletedListener(putPerformanceListener);
            putJob.attachDataTransferredListener(putPerformanceListener);
            putJob.transfer(new MemoryObjectChannelBuilder(sizeOfFiles));

            System.out.println();

            /**** GET ****/
            final Ds3ClientHelpers.Job getJob = helpers.startReadJob(this.bucketName, objList);
            final PerformanceListener getPerformanceListener = new PerformanceListener(
                    System.currentTimeMillis(),
                    numberOfFiles,
                    numberOfFiles * sizeOfFiles,
                    false);
            getJob.attachObjectCompletedListener(getPerformanceListener);
            getJob.attachDataTransferredListener(getPerformanceListener);
            getJob.transfer(new MemoryObjectChannelBuilder(sizeOfFiles));

        } finally {
            deleteAllContents(getClient(), this.bucketName);
        }
        return new PerformanceResult("");
    }

    private void deleteAllContents(final Ds3Client client, final String bucketName) throws IOException, SignatureException {
        final Ds3ClientHelpers helpers = Ds3ClientHelpers.wrap(client);

        final Iterable<Contents> objects = helpers.listObjects(bucketName);
        for(final Contents contents : objects) {
            client.deleteObject(new DeleteObjectRequest(bucketName, contents.getKey()));
        }

        client.deleteBucket(new DeleteBucketRequest(bucketName));
    }
}

class MemoryObjectChannelBuilder implements Ds3ClientHelpers.ObjectChannelBuilder {

    private final long sizeOfFiles;

    public MemoryObjectChannelBuilder(final long sizeOfFile) {
        this.sizeOfFiles = sizeOfFile;
    }

    @Override
    public SeekableByteChannel buildChannel(final String key) throws IOException {
        return new PerformanceSeekableByteChannel(this.sizeOfFiles);
    }
}

class PerformanceListener implements DataTransferredListener, ObjectCompletedListener {

    private final long startTime;
    private final int totalNumberOfFiles;
    private final long numberOfMB;
    private final boolean isPutCommand;
    private long totalByteTransferred = 0;
    private int numberOfFiles = 0;
    private double highestMbps = 0.0;
    private double time;
    private long content;
    private double mbps;

    public PerformanceListener(final long startTime, final int totalNumberOfFiles, final long numberOfMB, final boolean isPutCommand) {
        this.startTime = startTime;
        this.totalNumberOfFiles = totalNumberOfFiles;
        this.numberOfMB = numberOfMB;
        this.isPutCommand = isPutCommand;
    }

    @Override
    public void dataTransferred(final long size) {
        final long currentTime = System.currentTimeMillis();
        synchronized (this) {
            totalByteTransferred += size;
            time = (currentTime - this.startTime == 0) ? 1.0 : (currentTime - this.startTime) / 1000D;
            content = totalByteTransferred / 1024L / 1024L;
            mbps = content / time;
            if (mbps > highestMbps) highestMbps = mbps;
        }
        printStatistics();
    }

    @Override
    public void objectCompleted(final String s) {
        synchronized (this) {
            numberOfFiles += 1;
        }
        printStatistics();
    }

    private void printStatistics() {
        if (isPutCommand) {
            System.out.print(String.format("\rPut Statistics: (%d/%d MB), files (%d/%d), Time (%.03f sec), Mbps (%.03f), Highest Mbps (%.03f sec)",
                    content, numberOfMB, numberOfFiles, totalNumberOfFiles, time, mbps, highestMbps));
        }
        else {
            System.out.print(String.format("\rGet Statistics: (%d/%d MB), files (%d/%d), Time (%.03f sec), Mbps (%.03f), Highest Mbps (%.03f sec)",
                    content, numberOfMB, numberOfFiles, totalNumberOfFiles, time, mbps, highestMbps));
        }
    }
}

class PerformanceSeekableByteChannel implements SeekableByteChannel {
    private static final int BUFFER_SIZE = 10 * 1024 * 1024;
    final private byte[] backingArray;
    final private long limit;
    private int position;
    private boolean isOpen;

    public PerformanceSeekableByteChannel(final long size) {
        final byte[] bytes = new byte[BUFFER_SIZE];
        for (int i = 0; i < BUFFER_SIZE; i++) {
            bytes[i] = '1';
        }
        backingArray = bytes;

        this.position = 0;
        this.limit = size * 1024L * 1024L;
        this.isOpen = true;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public void close() throws IOException {
        this.isOpen = false;
    }

    public int read(final ByteBuffer dst) throws IOException {
        final int amountToRead = Math.min(dst.remaining(), BUFFER_SIZE);
        dst.put(this.backingArray, 0, amountToRead);

        return amountToRead;
    }

    public int write(final ByteBuffer src) throws IOException {
        final int amountToWrite = Math.min(src.remaining(), BUFFER_SIZE);
        src.get(this.backingArray, 0, amountToWrite);

        return amountToWrite;
    }

    public long position() throws IOException {
        return (long)this.position;
    }

    public SeekableByteChannel position(final long newPosition) throws IOException {
        this.position = (int)newPosition;
        return this;
    }

    public long size() throws IOException {
        return this.limit;
    }

    public SeekableByteChannel truncate(final long size) throws IOException {
        return this;
    }
}


