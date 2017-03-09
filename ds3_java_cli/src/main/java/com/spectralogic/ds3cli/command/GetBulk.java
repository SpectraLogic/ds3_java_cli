/*
 * *****************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.BulkJobType;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.models.RecoveryJob;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.helpers.*;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.helpers.pagination.GetBucketLoaderFactory;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.Metadata;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.collections.LazyIterable;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class GetBulk extends CliCommand<DefaultResult> {

    private final static Logger LOG = LoggerFactory.getLogger(GetBulk.class);

    protected final static int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    protected final static long DEFAULT_FILE_SIZE = 1024L;

    protected String bucketName;
    protected String directory;
    protected Path outputPath;
    protected ImmutableList<String> prefixes;
    protected Priority priority;
    protected boolean sync;
    protected boolean discard;
    protected int numberOfThreads;

    protected static final Option PREFIXES = Option.builder("p").hasArgs().argName("prefixes")
            .desc("get only objects whose names start with prefix  "
                    + "separate multiple prefixes with spaces").build();

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET);
    private final static ImmutableList<Option> optionalArgs
            = ImmutableList.of(DIRECTORY, PREFIXES, NUMBER_OF_THREADS,
            DISCARD, PRIORITY, SYNC, FORCE);

    public GetBulk() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        // keep a copy of argumnets to reconstruct command line for recovery
        this.arguments = args;
        processCommandOptions(requiredArgs, optionalArgs, args);
        return populateFromArguments(args);
    }

    protected CliCommand populateFromArguments(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        this.priority = args.getPriority();
        this.numberOfThreads = args.getNumberOfThreads();

        this.directory = args.getDirectory();
        if (Guard.isStringNullOrEmpty(this.directory) || directory.equals(".")) {
            this.outputPath = FileSystems.getDefault().getPath(".");
        } else {
            final Path dirPath = FileSystems.getDefault().getPath(directory);
            this.outputPath = FileSystems.getDefault().getPath(".").resolve(dirPath);
        }
        LOG.info("Output Path = {}", this.outputPath);

        this.discard = args.isDiscard();
        if (this.discard && !Guard.isStringNullOrEmpty(directory)) {
            throw new CommandException("Cannot set both directory and --discard");
        }
        if (this.discard) {
            LOG.warn("Using /dev/null getter -- all incoming data will be discarded");
        }

        final String[] prefix = args.getOptionValues(PREFIXES.getOpt());
        if(prefix != null && prefix.length > 0) {
            this.prefixes = ImmutableList.copyOf(prefix);
        }

        if (args.isSync()) {
            LOG.info("Using sync command");
            this.sync = true;
        }
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final Ds3ClientHelpers.ObjectChannelBuilder getter
                = this.discard ?  new MemoryObjectChannelBuilder(DEFAULT_BUFFER_SIZE, DEFAULT_FILE_SIZE)
                : new FileObjectGetter(this.outputPath);

        if (this.sync) {
            if (Guard.isNullOrEmpty(this.prefixes)) {
                LOG.info("Syncing all objects from {}", this.bucketName);
            } else {
                LOG.info("Syncing only those objects that start with {}", Joiner.on(" ").join(this.prefixes));
            }
            return new DefaultResult(this.restoreSome(getter));
        }

        if (!Guard.isNotNullAndNotEmpty(prefixes)) {
            LOG.info("Getting all objects from {}", this.bucketName);
            return new DefaultResult(this.restoreAll(getter));
        }

        LOG.info("Getting only those objects that start with {}", Joiner.on(" ").join(this.prefixes));
        return new DefaultResult(this.restoreSome(getter));
    }

    protected String restoreSome(final Ds3ClientHelpers.ObjectChannelBuilder getter) throws JobRecoveryException, IOException, XmlProcessingException {
        final Ds3ClientHelpers helper = getClientHelpers();

        final Iterable<Contents> prefixMatches;
        if (Guard.isNullOrEmpty(prefixes)) {
            prefixMatches = new LazyIterable<>(
                    new GetBucketLoaderFactory(getClient(), this.bucketName, null, null, 100, 5));
        } else {
            prefixMatches = getObjectsByPrefix();
        }
        if (Iterables.isEmpty(prefixMatches)) {
            return "No objects in bucket " + this.bucketName + " with prefixes '" + Joiner.on(" ").join(this.prefixes) + "'";
        }

        final Iterable<Ds3Object> objects;
        if (this.sync) {
            final Iterable<Contents> filteredContents = this.filterContents(prefixMatches, this.outputPath);
            if (Iterables.isEmpty(filteredContents)) {
                return "SUCCESS: All files are up to date";
            }
            objects = helper.toDs3Iterable(filteredContents, FolderNameFilter.filter());
        } else {
            objects = helper.toDs3Iterable(prefixMatches, FolderNameFilter.filter());
        }

        final Ds3ClientHelpers.Job job = helper.startReadJob(this.bucketName, objects,
                ReadJobOptions.create().withPriority(this.priority));
        job.withMaxParallelRequests(this.numberOfThreads);
        final LoggingFileObjectGetter loggingFileObjectGetter = new LoggingFileObjectGetter(getter, this.outputPath);
        job.attachMetadataReceivedListener(loggingFileObjectGetter);

        // provide recovery command in case of failure
        createRecoveryCommand(job.getJobId());

        // start transfer
        job.transfer(loggingFileObjectGetter);

        // Success -- build the response
        final StringBuilder response = new StringBuilder("SUCCESS: ");
        response.append(this.sync ? "Synced" : this.discard ? "Retrieved and discarded" : "Wrote");
        response.append(Guard.isNullOrEmpty(this.prefixes) ? " all the objects"
                : " all the objects that start with '" + Joiner.on(" ").join(this.prefixes) + "'");
        response.append(" from ");
        response.append(this.bucketName);
        response.append(this.discard ? "" : " to " + this.outputPath.toString());

        return response.toString();
    }

    protected String restoreAll(final Ds3ClientHelpers.ObjectChannelBuilder getter) throws JobRecoveryException, XmlProcessingException, IOException {
        final Ds3ClientHelpers helper = getClientHelpers();
        final Ds3ClientHelpers.Job job = helper.startReadAllJob(this.bucketName,
                ReadJobOptions.create().withPriority(this.priority));
        job.withMaxParallelRequests(this.numberOfThreads);
        final LoggingFileObjectGetter loggingFileObjectGetter = new LoggingFileObjectGetter(getter, this.outputPath);
        job.attachMetadataReceivedListener(loggingFileObjectGetter);
        // provide recovery command in case of failure
        createRecoveryCommand(job.getJobId());

        // start transfer
        job.transfer(loggingFileObjectGetter);

        // delete recovery file on success
        deleteRecoveryCommand(job.getJobId());

        if (this.discard) {
            return "SUCCESS: Retrieved and discarded all objects from " + this.bucketName;
        } else {
            return "SUCCESS: Wrote all the objects from " + this.bucketName + " to directory " + this.outputPath.toString();
        }
    }

    protected Iterable<Contents> filterContents(final Iterable<Contents> contents, final Path outputPath) throws IOException {
        final Iterable<Path> localFiles = FileUtils.listObjectsForDirectory(outputPath);
        final Map<String, Path> mapLocalFiles = new HashMap<>();
        for (final Path localFile : localFiles) {
            mapLocalFiles.put(FileUtils.getFileName(outputPath, localFile), localFile);
        }

        final List<Contents> filteredContents = new ArrayList<>();
        for (final Contents content : contents) {
            final Path filePath = mapLocalFiles.get(content.getKey());
            if (filePath == null) {
                filteredContents.add(content);
            } else if (SyncUtils.isNewFile(filePath, content, false)) {
                LOG.info("Syncing new version of {}", filePath.toString());
                filteredContents.add(content);
            } else {
                LOG.info("No need to sync {}", filePath.toString());
            }
        }
        return filteredContents;
    }

    protected class LoggingFileObjectGetter implements Ds3ClientHelpers.ObjectChannelBuilder, MetadataReceivedListener {

        final private Ds3ClientHelpers.ObjectChannelBuilder objectGetter;
        final private Path outputPath;

        public LoggingFileObjectGetter(final Ds3ClientHelpers.ObjectChannelBuilder getter, final Path outputPath) {
            this.objectGetter = getter;
            this.outputPath = outputPath;
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            LOG.info("Getting object {}", s);
            return this.objectGetter.buildChannel(s);
        }

        @Override
        public void metadataReceived(final String filename, final Metadata metadata) {
            final Path path = outputPath.resolve(filename);
            MetadataUtils.restoreLastModified(filename, metadata, path);
        }
    }

    protected Iterable<Contents> getObjectsByPrefix() {
        Iterable<Contents> allPrefixMatches = Collections.emptyList();
        for (final String prefix : prefixes) {
            final Iterable<Contents> prefixMatch = new LazyIterable<>(
                    new GetBucketLoaderFactory(getClient(), this.bucketName, prefix, null, 100, 5));
            allPrefixMatches = Iterables.concat(allPrefixMatches, prefixMatch);
        }
        return allPrefixMatches;
    }

    protected void createRecoveryCommand(final UUID jobId) {
        RecoveryJob recoveryJob = new RecoveryJob(BulkJobType.GET_BULK);
        recoveryJob.setBucketName(bucketName);
        recoveryJob.setId(jobId);
        recoveryJob.setNumberOfThreads(numberOfThreads);
        recoveryJob.setDirectory(directory);
        recoveryJob.setPrefix(prefixes);
        if (!RecoveryFileManager.writeRecoveryJob(recoveryJob)) {
            LOG.info("Could not create recovery file in temporary space.");
        }
    }

    protected void deleteRecoveryCommand(final UUID jobId) {
        try {
            RecoveryFileManager.deleteFiles(jobId.toString(), null, null);
        } catch (final IOException e) {
            LOG.info("Could not delete recovery file in temporary space.", e);
        }
    }

}
