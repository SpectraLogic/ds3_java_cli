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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.exceptions.SyncNotSupportedException;
import com.spectralogic.ds3cli.models.PutBulkResult;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectPutter;
import com.spectralogic.ds3client.helpers.MetadataAccess;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.WriteOptimization;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class PutBulk extends CliCommand<PutBulkResult> {

    private final static Logger LOG = LoggerFactory.getLogger(PutBulk.class);

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET);
    private final static ImmutableList<Option> optionalArgs
            = ImmutableList.of(PREFIX, NUMBER_OF_THREADS, WRITE_OPTIMIZATION,
            FOLLOW_SYMLINKS, PRIORITY, CHECKSUM,
            SYNC, FORCE, NUMBER_OF_THREADS, IGNORE_ERRORS,
            IGNORE_NAMING_CONFLICTS, DIRECTORY);

    private String bucketName;
    private Path inputDirectory;
    private String prefix;
    private boolean checksum;
    private Priority priority;
    private WriteOptimization writeOptimization;
    private boolean sync;
    private boolean force;
    private int numberOfThreads;
    private boolean ignoreErrors;
    private boolean pipe;
    private ImmutableList<Path> pipedFiles;
    private ImmutableMap<String, String> mapNormalizedObjectNameToObjectName = null;
    private boolean followSymlinks;
    private boolean ignoreNamingConflicts;

    public PutBulk() {
    }


    @Override
    public CliCommand init(final Arguments args) throws Exception {
        // set up Options and parse
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.bucketName = args.getBucket();
        this.pipe = CliUtils.isPipe();
        if (this.pipe) {
            if (this.isOtherArgs(args)) {
                throw new BadArgumentException("-d, -o and -p arguments are not supported when using piped input");
            }

            this.pipedFiles = FileUtils.getPipedFilesFromStdin(getFileSystemProvider());
            if (Guard.isNullOrEmpty(this.pipedFiles)) {
                throw new MissingOptionException("Stdin is empty"); //We should never see that since we checked isPipe
            }
            this.mapNormalizedObjectNameToObjectName = this.getNormalizedObjectNameToObjectName(this.pipedFiles);
        } else if (!Guard.isStringNullOrEmpty(args.getDirectory())) {
            final String srcDir = args.getDirectory();
            this.inputDirectory = Paths.get(srcDir);
            this.prefix = args.getPrefix();
        } else {
            throw new BadArgumentException("-d argument required unless using piped input");
        }

        this.priority = args.getPriority();
        this.writeOptimization = args.getWriteOptimization();
        this.checksum = args.isChecksum();
        this.force = args.isForce();

        if (args.isSync()) {
            if (!SyncUtils.isSyncSupported(getClient())) {
                throw new SyncNotSupportedException("The sync command is not supported with your version of BlackPearl.");
            }

            LOG.info("Using sync command");
            this.sync = true;
        }

        this.numberOfThreads = args.getNumberOfThreads();

        if (args.isIgnoreErrors()) {
            LOG.info("Ignoring files that cause errors");
            this.ignoreErrors = true;
        }

        this.followSymlinks = args.isFollowSymlinks();
        LOG.info("Follow symlinks has been set to: {}", this.followSymlinks);

        this.ignoreNamingConflicts = args.doIgnoreNamingConflicts();
        LOG.info("Ignore naming conflicts has been set to: {}", this.ignoreNamingConflicts);

        return this;
    }

    @Override
    public PutBulkResult call() throws Exception {
        /* Ensure the bucket exists and if not create it */
        final Ds3ClientHelpers helpers = getClientHelpers();
        helpers.ensureBucketExists(this.bucketName);

        Iterable<Path> filesToPut = this.getFilesToPut();
        if (this.sync) {
            filesToPut = new FilteringIterable<>(filesToPut,
                    new SyncFilter(this.prefix != null ? this.prefix : "",
                        this.inputDirectory,
                        this.getClientHelpers(),
                        this.bucketName
                    )
            );

            if (Iterables.isEmpty(filesToPut)) {
                return new PutBulkResult("SUCCESS: All files are up to date");
            }
        }

        final ObjectsToPut objectsToPut = FileUtils.getObjectsToPut(filesToPut, this.inputDirectory, this.ignoreErrors);
        final Iterable<Ds3Object> ds3Objects = objectsToPut.getDs3Objects();
        this.appendPrefix(ds3Objects);

        return this.transfer(helpers, ds3Objects, objectsToPut.getDs3IgnoredObjects());
    }

    private PutBulkResult transfer(final Ds3ClientHelpers helpers, final Iterable<Ds3Object> ds3Objects, final ImmutableList<IgnoreFile> ds3IgnoredObjects) throws IOException, XmlProcessingException {
        final WriteJobOptions writeJobOptions = WriteJobOptions.create()
                .withPriority(this.priority)
                .withWriteOptimization(this.writeOptimization)
                .withIgnoreNamingConflicts(this.ignoreNamingConflicts);
        writeJobOptions.setForce(force);
        final Ds3ClientHelpers.Job job = helpers.startWriteJob(this.bucketName, ds3Objects,
                writeJobOptions);
                job.withMaxParallelRequests(this.numberOfThreads);

        if (this.checksum) {
            throw new RuntimeException("Checksum calculation is not currently supported."); //TODO
//            Logging.log("Performing bulk put with checksum computation enabled");
//            job.withRequestModifier(new ComputedChecksumModifier());
        }
        LOG.info("Created bulk put job {}, starting transfer", job.getJobId().toString());

        String resultMessage;
        if (this.pipe) {

            final PipeFileObjectPutter pipeFileObjectPutter = new PipeFileObjectPutter(this.mapNormalizedObjectNameToObjectName);
            job.withMetadata(pipeFileObjectPutter).transfer(pipeFileObjectPutter);
            resultMessage = String.format("SUCCESS: Wrote all piped files to bucket %s", this.bucketName);
        } else {
            final PrefixedFileObjectPutter prefixedFileObjectPutter = new PrefixedFileObjectPutter(this.inputDirectory, this.prefix);
            job.withMetadata(prefixedFileObjectPutter).transfer(prefixedFileObjectPutter);
            resultMessage = String.format("SUCCESS: Wrote all the files in %s to bucket %s", this.inputDirectory.toString(), this.bucketName);
        }

        if (this.ignoreErrors && !ds3IgnoredObjects.isEmpty()) {
            resultMessage = String.format("WARN: Not all of the files were written to bucket %s", this.bucketName);
        }

        return new PutBulkResult(resultMessage, ds3IgnoredObjects);
    }

    private void appendPrefix(final Iterable<Ds3Object> ds3Objects) {
        if (this.pipe || this.prefix == null) return;

        LOG.info("Pre-appending {} to all object names", this.prefix);
        for (final Ds3Object obj : ds3Objects) {
            obj.setName(this.prefix + obj.getName());
        }
    }

    private ImmutableMap<String, String> getNormalizedObjectNameToObjectName(final ImmutableList<Path> pipedFiles) {
        final ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        for (final Path file : pipedFiles) {
            map.put(FileUtils.normalizeObjectName(file.toString()), file.toString());
        }

        return map.build();
    }

    @Override
    public View<PutBulkResult> getView() {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.PutBulkView();
        }
        return new com.spectralogic.ds3cli.views.cli.PutBulkView();
    }
    
    private static class SyncFilter implements FilteringIterable.FilterFunction<Path> {

        private final String prefix;
        private final Path inputDirectory;
        private final ImmutableMap<String, Contents> mapBucketFiles;

        public SyncFilter(final String prefix, final Path inputDirectory, final Ds3ClientHelpers helpers, final String bucketName) throws IOException {
            this.prefix = prefix;
            this.inputDirectory = inputDirectory;
            this.mapBucketFiles = generateBucketFileMap(prefix, helpers, bucketName);
        }

        private static ImmutableMap<String, Contents> generateBucketFileMap(final String prefix, final Ds3ClientHelpers helpers, final String bucketName) throws IOException {
            final Iterable<Contents> contents = helpers.listObjects(bucketName, prefix);
            final ImmutableMap.Builder<String, Contents> bucketFileMapBuilder = ImmutableMap.builder();
            for (final Contents content : contents) {
                bucketFileMapBuilder.put(content.getKey(), content);
            }
            return bucketFileMapBuilder.build();
        }

        @Override
        public boolean filter(final Path item) {
            final String fileName = FileUtils.getFileName(this.inputDirectory, item);
            final Contents content = mapBucketFiles.get(prefix + fileName);
            try {
                if (content == null) {
                    return false;
                } else if (SyncUtils.isNewFile(item, content, true)) {
                    LOG.info("Syncing new version of {}", fileName);
                    return false;
                } else {
                    LOG.info("No need to sync {}", fileName);
                    return true;
                }
            } catch (final IOException e) {
                LOG.error("Failed to check the status of a file", e);
                // return false to let other code catch the exception when trying to access it
                return false;
            }
        }
    }

    private Iterable<Path> getFilesToPut() throws IOException {
        final Iterable<Path> filesToPut;
        if (this.pipe) {
            filesToPut = this.pipedFiles;
        }
        else {
            filesToPut = FileUtils.listObjectsForDirectory(this.inputDirectory);
        }
        return new FilteringIterable<>(filesToPut, new FilteringIterable.FilterFunction<Path>() {
            @Override
            public boolean filter(final Path item) {
                final boolean filter = !(followSymlinks || !Files.isSymbolicLink(item));
                if (filter) {
                    LOG.info("Skipping: {}", item.toString());
                }
                return filter;
            }
        });
    }

    public boolean isOtherArgs(final Arguments args) {
        return  !Guard.isStringNullOrEmpty(args.getDirectory()) ||   //-d
                !Guard.isStringNullOrEmpty(args.getObjectName()) || //-o
                !Guard.isStringNullOrEmpty(args.getPrefix());   //-p
    }

    static class PrefixedFileObjectPutter implements Ds3ClientHelpers.ObjectChannelBuilder, MetadataAccess {

        final private LoggingFileObjectPutter objectPutter;
        final private String prefix;
        final private Path inputDirectory;

        public PrefixedFileObjectPutter(final Path inputDirectory, final String prefix) {
            this.objectPutter = new LoggingFileObjectPutter(inputDirectory);
            this.prefix = prefix;
            this.inputDirectory = inputDirectory;
        }

        @Override
        public SeekableByteChannel buildChannel(final String fileName) throws IOException {
            final String objectName = removePrefix(fileName);
            return this.objectPutter.buildChannel(objectName);
        }

        @Override
        public Map<String, String> getMetadataValue(final String fileName) {
            final String unPrefixedFile = removePrefix(fileName);

            final Path path = inputDirectory.resolve(unPrefixedFile);
            return MetadataUtils.getMetadataValues(path);
        }

        private String removePrefix(final String fileName) {
            if (this.prefix == null) {
                return fileName;
            } else {
                if (!fileName.startsWith(this.prefix)) {
                    LOG.info("The object ({}) does not begin with prefix {}.  Ignoring adding the prefix.", fileName,  this.prefix);
                    return fileName;
                } else {
                    return fileName.substring(this.prefix.length());
                }
            }
        }
    }

    static class LoggingFileObjectPutter implements Ds3ClientHelpers.ObjectChannelBuilder {
        final private FileObjectPutter objectPutter;

        public LoggingFileObjectPutter(final Path inputDirectory) {
            this.objectPutter = new FileObjectPutter(inputDirectory);
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            LOG.info("Putting {} to ds3 endpoint", s);
            return this.objectPutter.buildChannel(s);
        }
    }

    public static class ObjectsToPut {

        private final ImmutableList<Ds3Object> ds3Objects;
        private final ImmutableList<IgnoreFile> ds3IgnoredObjects;

        public ObjectsToPut(final ImmutableList<Ds3Object> ds3Objects, final ImmutableList<IgnoreFile> ds3IgnoredObjects) {
            this.ds3Objects = ds3Objects;
            this.ds3IgnoredObjects = ds3IgnoredObjects;
        }

        public ImmutableList<Ds3Object> getDs3Objects() {
            return this.ds3Objects;
        }

        public ImmutableList<IgnoreFile> getDs3IgnoredObjects() {
            return this.ds3IgnoredObjects;
        }
    }

    public static class IgnoreFile {
        @JsonProperty("path")
        private final String path;

        @JsonProperty("error_message")
        private final String errorMessage;

        public IgnoreFile(final Path path, final String errorMessage) {
            this.path = path.toString();
            this.errorMessage = errorMessage;
        }

        public String getPath() {
            return this.path;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }
    }

    /**
     * Returns a channel and metadata for files that have been piped in via stdin
     */
    static class PipeFileObjectPutter implements Ds3ClientHelpers.ObjectChannelBuilder, MetadataAccess {

        private final ImmutableMap<String, String> mapNormalizedObjectNameToObjectName;

        public PipeFileObjectPutter(final ImmutableMap<String, String> mapNormalizedObjectNameToObjectName) {
            this.mapNormalizedObjectNameToObjectName = mapNormalizedObjectNameToObjectName;
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            return FileChannel.open(Paths.get(this.mapNormalizedObjectNameToObjectName.get(s)), StandardOpenOption.READ);
        }

        @Override
        public Map<String, String> getMetadataValue(final String s) {
            final Path path = Paths.get(this.mapNormalizedObjectNameToObjectName.get(s));
            return MetadataUtils.getMetadataValues(path);
        }
    }
}
