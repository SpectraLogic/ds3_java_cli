/*
 * *****************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ***************************************************************************
 */

package com.spectralogic.ds3cli.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.MemoryObjectChannelBuilder;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.DeleteBucketRequest;
import com.spectralogic.ds3client.commands.DeleteObjectRequest;
import com.spectralogic.ds3client.commands.PutBucketRequest;
import com.spectralogic.ds3client.helpers.DataTransferredListener;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.ObjectCompletedListener;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.util.List;

import static com.spectralogic.ds3cli.ArgumentFactory.*;


public class Performance extends CliCommand<DefaultResult> {

    private final static ImmutableList<Option> requiredArgs
            = ImmutableList.of(BUCKET, NUMBER_OF_FILES, SIZE_OF_FILES);

    private String bucketName;
    private String numberOfFiles;
    private String sizeOfFiles;
    private int bufferSize;
    private int numberOfThreads;
    private boolean doNotDelete;

    public Performance() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);

        this.viewType = ViewType.CLI;
        bucketName = args.getBucket();
        numberOfFiles = args.getNumberOfFiles();
        sizeOfFiles = args.getSizeOfFiles();
        bufferSize = args.getBufferSize();
        this.numberOfThreads = args.getNumberOfThreads();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final Ds3ClientHelpers helpers = Ds3ClientHelpers.wrap(getClient());
        final int numberOfFiles = Integer.valueOf(this.numberOfFiles);
        final long sizeOfFiles = Long.valueOf(this.sizeOfFiles);

        try {
            try {
                final PutBucketRequest request = new PutBucketRequest(bucketName);
                getClient().putBucket(request);
            } catch(final FailedRequestException e) {
                this.doNotDelete = true;
                if (e.getStatusCode() == 409) {
                    throw new CommandException("Bucket " + bucketName + " already exists. To avoid any conflicts please use a non-existent bucket.");
                }
                throw new CommandException("Encountered a DS3 Error", e);
            }

            final List<Ds3Object> objList = getDs3Objects(numberOfFiles, sizeOfFiles);

            /**** PUT ****/
            transfer(helpers, numberOfFiles, sizeOfFiles, objList, true);

            /**** GET ****/
            transfer(helpers, numberOfFiles, sizeOfFiles, objList, false);

        } finally {
            if (!doNotDelete) deleteAllContents(getClient(), this.bucketName);
        }
        return new DefaultResult("Done!");
    }

    private List<Ds3Object> getDs3Objects(final int numberOfFiles, final long sizeOfFiles) {
        final List<Ds3Object> objList = Lists.newArrayList();
        for (int i = 0; i < numberOfFiles; i++) {
            final long testFileSize = sizeOfFiles * 1024L * 1024L;
            final Ds3Object obj = new Ds3Object("file_" + i, testFileSize);
            objList.add(obj);
        }
        return objList;
    }

    private void transfer(final Ds3ClientHelpers helpers, final int numberOfFiles, final long sizeOfFiles, final List<Ds3Object> objList, final boolean isPutCommand) throws IOException, XmlProcessingException {
        final Ds3ClientHelpers.Job job;
        if (isPutCommand) {
            job = helpers.startWriteJob(this.bucketName, objList);
        } else {
            job = helpers.startReadJob(this.bucketName, objList);
        }
        job.withMaxParallelRequests(this.numberOfThreads);

        final PerformanceListener getPerformanceListener = new PerformanceListener(
                System.currentTimeMillis(),
                numberOfFiles,
                numberOfFiles * sizeOfFiles,
                isPutCommand);
        job.attachObjectCompletedListener(getPerformanceListener);
        job.attachDataTransferredListener(getPerformanceListener);
        job.transfer(new MemoryObjectChannelBuilder(bufferSize, sizeOfFiles));
        System.out.println();
    }

    private void deleteAllContents(final Ds3Client client, final String bucketName) throws IOException {
        final Ds3ClientHelpers helpers = Ds3ClientHelpers.wrap(client);
        final Iterable<Contents> objects = helpers.listObjects(bucketName);
        for (final Contents contents : objects) {
            client.deleteObject(new DeleteObjectRequest(bucketName, contents.getKey()));
        }

        client.deleteBucket(new DeleteBucketRequest(bucketName));
    }

    private class PerformanceListener implements DataTransferredListener, ObjectCompletedListener {
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
                time = currentTime - this.startTime == 0 ? 1.0 : (currentTime - this.startTime) / 1000D;
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
            final String messagePrefix;
            if (isPutCommand) {
                messagePrefix = "Putting";
            }
            else {
                messagePrefix = "Getting";
            }


            System.out.print(String.format("\r%s Statistics: (%d/%d MB), files (%d/%d completed), Time (%.03f sec), MBps (%.03f), Highest MBps (%.03f)",
                    messagePrefix, content, numberOfMB, numberOfFiles, totalNumberOfFiles, time, mbps, highestMbps));
        }
    }

}


