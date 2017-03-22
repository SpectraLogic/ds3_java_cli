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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.BulkJobType;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.models.RecoveryJob;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectGetter;
import com.spectralogic.ds3client.helpers.FolderNameFilter;
import com.spectralogic.ds3client.helpers.MetadataReceivedListener;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.helpers.pagination.GetBucketLoaderFactory;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.Metadata;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.collections.LazyIterable;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.AbstractDocument;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.*;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class GetBulk extends CliCommand<DefaultResult> {

    private final static Logger LOG = LoggerFactory.getLogger(GetBulk.class);

    private final static int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    private final static long DEFAULT_FILE_SIZE = 1024L;

    private String bucketName;
    private String directory;
    private Path outputPath;
    private ImmutableList<String> prefixes;
    private Priority priority;
    private boolean sync;
    private boolean discard;
    private int numberOfThreads;
    private boolean pipe;
    private ImmutableList<String> pipedFileNames;

    private static final Option PREFIXES = Option.builder("p").hasArgs().argName("prefixes")
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
        processCommandOptions(requiredArgs, optionalArgs, args);
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

        this.pipe = CliUtils.isPipe();
        if (this.pipe) {
            if (this.isOtherArgs(args)) {
                throw new BadArgumentException("--discard, -o and -p arguments are not supported when using piped input");
            }

            this.pipedFileNames = FileUtils.getPipedListFromStdin(getFileSystemProvider());
            if (Guard.isNullOrEmpty(this.pipedFileNames)) {
                throw new MissingOptionException("Stdin is empty"); //We should never see that since we checked isPipe
            }
        } else {
            final String[] prefix = args.getOptionValues(PREFIXES.getOpt());
            if (prefix != null && prefix.length > 0) {
                this.prefixes = ImmutableList.copyOf(prefix);
            }
        }

        if (args.isSync()) {
            LOG.info("Using sync command");
            this.sync = true;
        }
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final Ds3ClientHelpers.ObjectChannelBuilder getter;
        if (this.pipe) {
            getter = new PipedFileObjectGetter(this.outputPath, FileUtils.normalizedObjectNames(this.pipedFileNames));
        } else if (this.discard) {
            getter = new MemoryObjectChannelBuilder(DEFAULT_BUFFER_SIZE, DEFAULT_FILE_SIZE);
        } else {
            getter = new FileObjectGetter(this.outputPath);
        }
        LOG.info(getLogDescription());
        return new DefaultResult(this.restore(getter));
    }

    // preserve legacy descriptions from different code paths
    private String getLogDescription() {
        if (this.pipe) {
            return "Getting piped objects from " + this.bucketName;
        }
        if (this.sync) {
            if (Guard.isNullOrEmpty(this.prefixes)) {
                return "Syncing all objects from " + this.bucketName;
            } else {
                return "Syncing only those objects that start with " + Joiner.on(" ").join(this.prefixes);
            }
        }
        if (Guard.isNotNullAndNotEmpty(prefixes)) {
            return "Getting only those objects that start with " + Joiner.on(" ").join(this.prefixes);
        }
        return "Getting all objects from " + this.bucketName;
    }

    private String restore(final Ds3ClientHelpers.ObjectChannelBuilder getter) throws IOException, XmlProcessingException {
        final Ds3ClientHelpers helper = getClientHelpers();
        final LoggingFileObjectGetter loggingFileObjectGetter = new LoggingFileObjectGetter(getter, this.outputPath);

        final Ds3ClientHelpers.Job job;
        if (!this.pipe && !this.sync && Guard.isNullOrEmpty(this.prefixes)) {
            // restore all
            job = helper.startReadAllJob(this.bucketName, ReadJobOptions.create().withPriority(this.priority));
        } else {
            // restore some
            final Iterable<Contents> contentMatches;
            if (this.pipe) {
                contentMatches = getObjectsByPipe();
            } else if (!Guard.isNullOrEmpty(prefixes)) {
                contentMatches = getObjectsByPrefix();
            } else {
                contentMatches = getAllObjectsInBucket();
            }
            if (Iterables.isEmpty(contentMatches)) {
                return "No matching objects in bucket " + this.bucketName;
            }

            final Iterable<Ds3Object> objects;
            if (this.sync) {
                final Iterable<Contents> filteredContents = this.filterContents(contentMatches, this.outputPath);
                if (Iterables.isEmpty(filteredContents)) {
                    return "SUCCESS: All files are up to date";
                }
                objects = helper.toDs3Iterable(filteredContents, FolderNameFilter.filter());
            } else {
                objects = helper.toDs3Iterable(contentMatches, FolderNameFilter.filter());
            }
            job = helper.startReadJob(this.bucketName, objects,
                    ReadJobOptions.create().withPriority(this.priority));
        }
        job.withMaxParallelRequests(this.numberOfThreads);
        job.attachMetadataReceivedListener(loggingFileObjectGetter);

        // write parameters to file to enable recpvery
        createRecoveryCommand(job.getJobId());

        job.transfer(loggingFileObjectGetter);

        // clean up recovery file on success of job.transfer()
        RecoveryFileManager.deleteRecoveryCommand(job.getJobId());

        // Success -- build the response
        final StringBuilder response = new StringBuilder("SUCCESS: ");
        response.append(this.sync ? "Synced" : this.discard ? "Retrieved and discarded" : "Wrote");
        response.append((!this.pipe && !this.sync && Guard.isNullOrEmpty(this.prefixes))
                ? " all objects" : this.pipe ? " object names listed in stdin " :
                " all the objects that start with '" + Joiner.on(" ").join(this.prefixes) + "'");
        response.append(" from ");
        response.append(this.bucketName);
        response.append(this.discard ? "" : " to directory " + this.outputPath.toString());

        return response.toString();
    }

    private Iterable<Contents> filterContents(final Iterable<Contents> contents, final Path outputPath) throws IOException {
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

    private Iterable<Contents> getAllObjectsInBucket() {
        return new LazyIterable<>(
                    new GetBucketLoaderFactory(getClient(), this.bucketName, null, null, 100, 5));
    }

    private Iterable<Contents> getObjectsByPrefix() {
        Iterable<Contents> allPrefixMatches = Collections.emptyList();
        for (final String prefix : prefixes) {
            final Iterable<Contents> prefixMatch = new LazyIterable<>(
                    new GetBucketLoaderFactory(getClient(), this.bucketName, prefix, null, 100, 5));
            allPrefixMatches = Iterables.concat(allPrefixMatches, prefixMatch);
        }
        return allPrefixMatches;
    }

    private Iterable<Contents> getObjectsByPipe() {
        final ImmutableMap<String, String> objectMap
                = FileUtils.normalizedObjectNames(this.pipedFileNames);
        ImmutableList.Builder<Contents> allMatches = new ImmutableList.Builder<>();
        final Iterable<Contents> objects = new LazyIterable<>(
                new GetBucketLoaderFactory(getClient(), this.bucketName, null, null, 100, 5));
        for (final Contents object: objects) {
            LOG.info("Contents key: {}", object.getKey());
            if (objectMap.containsKey(object.getKey())) {
                allMatches.add(object);
                LOG.info("Matched: {}", object.getKey());
            }
        }
        return allMatches.build();
    }

    private void createRecoveryCommand(final UUID jobId) {
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

    public boolean isOtherArgs(final Arguments args) {
        return  args.isDiscard() || // --discard
                !Guard.isStringNullOrEmpty(args.getObjectName()) || //-o
                (args.getOptionValues(PREFIXES.getOpt()) != null
                 && args.getOptionValues(PREFIXES.getOpt()).length > 0); // --prefixes
    }

    private class PipedFileObjectGetter implements Ds3ClientHelpers.ObjectChannelBuilder {
        private final ImmutableMap<String, String> mapNormalizedObjectNameToObjectName;
        private final Path root;

        public PipedFileObjectGetter(final Path rootPath, final ImmutableMap<String, String> normalizedObjectNames) {
            this.mapNormalizedObjectNameToObjectName = normalizedObjectNames;
            this.root = rootPath;
        }

        public SeekableByteChannel buildChannel(String key) throws IOException {
            LOG.info("Piped name: {}", key);
            final Path normalizedName = Paths.get(this.mapNormalizedObjectNameToObjectName.get(key));
            if (normalizedName == null) {
                throw new IOException("No match for piped name " + key);
            }
            Path objectPath = this.root.resolve(normalizedName);
            Path parentPath = objectPath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath, new FileAttribute[0]);
            }
            return FileChannel.open(objectPath, new OpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING});
        }
    }


}
