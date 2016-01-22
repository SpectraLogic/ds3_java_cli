/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3cli.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.PutBulkResult;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectPutter;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.models.bulk.Priority;
import com.spectralogic.ds3client.models.bulk.WriteOptimization;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PutBulk extends CliCommand<PutBulkResult> {

    private final static Logger LOG = LoggerFactory.getLogger(PutBulk.class);

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
    private ImmutableList<String> pipedFiles;
    private ImmutableMap<String, String> mapNormalizedObjectNameToObjectName = null;

    public PutBulk(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        if (this.bucketName == null) {
            throw new MissingOptionException("The bulk put command requires '-b' to be set.");
        }

        pipe = args.isPipe();
        if (pipe) {
            this.pipedFiles = getPipedFilesFromStdin();
            if (this.pipedFiles.isEmpty()) {
                throw new MissingOptionException("Stdin is empty");
            }
        } else {
            final String srcDir = args.getDirectory();
            if (srcDir == null) {
                throw new MissingOptionException("The bulk put command required '-d' to be set.");
            }
            this.inputDirectory = Paths.get(srcDir);
        }

        if (args.getObjectName() != null) {
            System.err.println("Warning: '-o' is not used with bulk put and is ignored.");
        }

        this.prefix = args.getPrefix();
        this.priority = args.getPriority();
        this.writeOptimization = args.getWriteOptimization();
        this.checksum = args.isChecksum();
        this.force = args.isForce();

        if (args.isSync()) {
            LOG.info("Using sync command");
            this.sync = true;
        }

        this.numberOfThreads = Integer.valueOf(args.getNumberOfThreads());

        if (args.isIgnoreErrors()) {
            LOG.info("Ignoring files that cause errors");
            this.ignoreErrors = true;
        }

        return this;
    }

    @Override
    public PutBulkResult call() throws Exception {
        if (!force) {
            BlackPearlUtils.checkBlackPearlForTapeFailure(getClient());
        }

        final Ds3ClientHelpers helpers = getClientHelpers();
        final Iterable<Ds3Object> ds3Objects;

        /* Ensure the bucket exists and if not create it */
        helpers.ensureBucketExists(this.bucketName);

        final ObjectsForDirectory objectsForDirectory;
        if (pipe) {
            mapNormalizedObjectNameToObjectName = getNormalizedObjectNameToObjectName(pipedFiles);
            objectsForDirectory = Utils.getObjectsForDirectory(mapNormalizedObjectNameToObjectName, ignoreErrors);
            ds3Objects = objectsForDirectory.getDs3Objects();
        } else {
            if (sync) {
                if (!SyncUtils.isSyncSupported(getClient())) {
                    return new PutBulkResult("Failed: The sync command is not supported with your version of BlackPearl.");
                }

                final Iterable<Contents> contents = helpers.listObjects(bucketName, prefix);
                final Iterable<Path> filteredObjects = filterObjects(this.inputDirectory, prefix != null ? prefix : "", contents);
                if (Iterables.isEmpty(filteredObjects)) {
                    return new PutBulkResult("SUCCESS: All files are up to date");
                }

                objectsForDirectory = Utils.getObjectsForDirectory(filteredObjects, inputDirectory, ignoreErrors);
            } else {
                objectsForDirectory = Utils.getObjectsForDirectory(this.inputDirectory, ignoreErrors);
            }

            ds3Objects = objectsForDirectory.getDs3Objects();

            if (prefix != null) {
                LOG.info("Pre-appending " + prefix + " to all object names");
                for (final Ds3Object obj : ds3Objects) {
                    obj.setName(prefix + obj.getName());
                }
            }
        }

        final Ds3ClientHelpers.Job job = helpers.startWriteJob(this.bucketName, ds3Objects,
                WriteJobOptions.create()
                        .withPriority(this.priority)
                        .withWriteOptimization(this.writeOptimization));
        job.withMaxParallelRequests(this.numberOfThreads);
        if (this.checksum) {
            throw new RuntimeException("Checksum calculation is not currently supported."); //TODO
//            Logging.log("Performing bulk put with checksum computation enabled");
//            job.withRequestModifier(new ComputedChecksumModifier());
        }
        LOG.info("Created bulk put job " + job.getJobId().toString() + ", starting transfer");

        String resultMessage;
        if (pipe) {
            job.transfer(new PipeFileObjectPutter(this.mapNormalizedObjectNameToObjectName));
            resultMessage = String.format("SUCCESS: Wrote all piped files to bucket %s", this.bucketName);
        } else {
            job.transfer(new PrefixedFileObjectPutter(this.inputDirectory, prefix));
            resultMessage = String.format("SUCCESS: Wrote all the files in %s to bucket %s",this.inputDirectory.toString(), this.bucketName);
        }

        if (ignoreErrors && !objectsForDirectory.ds3IgnoredObjects.isEmpty()) {
            resultMessage = String.format("WARN: Not all of the files were written to bucket %s", this.bucketName);
        }

        return new PutBulkResult(resultMessage, objectsForDirectory.getDs3IgnoredObjects());
    }

    private ImmutableMap<String, String> getNormalizedObjectNameToObjectName(final ImmutableList<String> pipedFiles) {
        final ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        for (final String file : pipedFiles) {
            map.put(Utils.normalizeObjectName(file), file);
        }

        return map.build();
    }

    private Iterable<Path> filterObjects(final Path inputDirectory, final String prefix, final Iterable<Contents> contents) throws IOException {
        final Iterable<Path> localFiles = Utils.listObjectsForDirectory(inputDirectory);
        final Map<String, Contents> mapBucketFiles = new HashMap<>();
        for (final Contents content : contents) {
            mapBucketFiles.put(content.getKey(), content);
        }

        final List<Path> filteredObjects = new ArrayList<>();
        for (final Path localFile : localFiles) {
            final String fileName = Utils.getFileName(inputDirectory, localFile);
            final Contents content = mapBucketFiles.get(prefix + fileName);
            if (content == null) {
                filteredObjects.add(localFile);
            } else if (SyncUtils.isNewFile(localFile, content, true)) {
                LOG.info("Syncing new version of " + fileName);
                filteredObjects.add(localFile);
            } else {
                LOG.info("No need to sync " + fileName);
            }
        }

        return filteredObjects;
    }

    private ImmutableList<String> getPipedFilesFromStdin() throws IOException {
        final ImmutableList.Builder<String> pipedFiles = new ImmutableList.Builder<>();
        final int availableBytes = System.in.available();
        if (availableBytes > 0) {
            // Wrap the System.in inside BufferedReader
            // But do not close it in a finally block, as we
            // did no open System.in; enforcing the rule that
            // he who opens it, closes it; leave the closing to the OS.
            final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            LOG.info("Piped files are:");
            String line;
            while ((line = in.readLine()) != null) {
                final Path file = Paths.get(line);
                if (!getFileUtils().isRegularFile(file) && !Files.isSymbolicLink(file)) {
                    LOG.warn(String.format("WARN: piped data must be a regular/symbolic link file and not a directory ==> %s will be skipped", line));
                    continue;
                }
                LOG.info(line);
                pipedFiles.add(line);
            }
        }

        return pipedFiles.build();
    }

    static class PrefixedFileObjectPutter implements Ds3ClientHelpers.ObjectChannelBuilder {

        final private LoggingFileObjectPutter objectPutter;
        final private String prefix;

        public PrefixedFileObjectPutter(final Path inputDirectory, final String prefix) {
            this.objectPutter = new LoggingFileObjectPutter(inputDirectory);
            this.prefix = prefix;
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {

            final String objectName;

            if (prefix == null) {
                objectName = s;
            } else {
                if (!s.startsWith(prefix)) {
                    LOG.info("The object (" + s + ") does not begin with prefix " + prefix + ".  Ignoring adding the prefix.");
                    objectName = s;
                } else {
                    objectName = s.substring(prefix.length());
                }
            }

            return objectPutter.buildChannel(objectName);
        }
    }

    static class LoggingFileObjectPutter implements Ds3ClientHelpers.ObjectChannelBuilder {
        final private FileObjectPutter objectPutter;

        public LoggingFileObjectPutter(final Path inputDirectory) {
            this.objectPutter = new FileObjectPutter(inputDirectory);
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            LOG.info("Putting " + s + " to ds3 endpoint");
            return this.objectPutter.buildChannel(s);
        }
    }

    public static class ObjectsForDirectory {

        private final ImmutableList<Ds3Object> ds3Objects;
        private final ImmutableList<IgnoreFile> ds3IgnoredObjects;

        public ObjectsForDirectory(final ImmutableList<Ds3Object> ds3Objects, final ImmutableList<IgnoreFile> ds3IgnoredObjects) {
            this.ds3Objects = ds3Objects;
            this.ds3IgnoredObjects = ds3IgnoredObjects;
        }

        public ImmutableList<Ds3Object> getDs3Objects() {
            return ds3Objects;
        }

        public ImmutableList<IgnoreFile> getDs3IgnoredObjects() {
            return ds3IgnoredObjects;
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
            return path;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    static class PipeFileObjectPutter implements Ds3ClientHelpers.ObjectChannelBuilder {

        private final ImmutableMap<String, String> mapNormalizedObjectNameToObjectName;

        public PipeFileObjectPutter(final ImmutableMap<String, String> mapNormalizedObjectNameToObjectName) {
            this.mapNormalizedObjectNameToObjectName = mapNormalizedObjectNameToObjectName;
        }


        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            return FileChannel.open(Paths.get(this.mapNormalizedObjectNameToObjectName.get(s)), StandardOpenOption.READ);
        }
    }

}
