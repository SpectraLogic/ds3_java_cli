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
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.exceptions.SyncNotSupportedException;
import com.spectralogic.ds3cli.models.PutBulkResult;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectPutter;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.models.bulk.Priority;
import com.spectralogic.ds3client.models.bulk.WriteOptimization;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SignatureException;
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
    private ImmutableList<Path> pipedFiles;
    private ImmutableMap<String, String> mapNormalizedObjectNameToObjectName = null;

    public PutBulk(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The bulk put command requires '-b' to be set.");
        }

        pipe = Utils.isPipe();
        if (pipe) {
            if (isOtherArgs(args)) {
                throw new BadArgumentException("No other argument is supported when using piped input");
            }

            pipedFiles = Utils.getPipedFilesFromStdin(getFileUtils());
            if (Guard.isNullOrEmpty(pipedFiles)) {
                throw new MissingOptionException("Stdin is empty"); //We should never see that since we checked isPipe
            }
            mapNormalizedObjectNameToObjectName = getNormalizedObjectNameToObjectName(pipedFiles);
        } else {
            final String srcDir = args.getDirectory();
            if (srcDir == null) {
                throw new MissingOptionException("The bulk put command required '-d' to be set.");
            }
            inputDirectory = Paths.get(srcDir);

            if (args.getObjectName() != null) {
                System.err.println("Warning: '-o' is not used with bulk put and is ignored.");
            }

            prefix = args.getPrefix();
        }

        priority = args.getPriority();
        writeOptimization = args.getWriteOptimization();
        checksum = args.isChecksum();
        force = args.isForce();

        if (args.isSync()) {
            if (!SyncUtils.isSyncSupported(getClient())) {
                throw new SyncNotSupportedException("The sync command is not supported with your version of BlackPearl.");
            }

            LOG.info("Using sync command");
            sync = true;
        }

        numberOfThreads = Integer.valueOf(args.getNumberOfThreads());

        if (args.isIgnoreErrors()) {
            LOG.info("Ignoring files that cause errors");
            ignoreErrors = true;
        }

        return this;
    }

    @Override
    public PutBulkResult call() throws Exception {
        if (!force) {
            BlackPearlUtils.checkBlackPearlForTapeFailure(getClient());
        }

        /* Ensure the bucket exists and if not create it */
        final Ds3ClientHelpers helpers = getClientHelpers();
        helpers.ensureBucketExists(bucketName);

        Iterable<Path> filesToPut = getFilesToPut();
        if (sync) {
            filesToPut = filterObjects(filesToPut, prefix != null ? prefix : "");
            if (Iterables.isEmpty(filesToPut)) {
                return new PutBulkResult("SUCCESS: All files are up to date");
            }
        }

        final ObjectsToPut objectsToPut = Utils.getObjectsToPut(filesToPut, inputDirectory, ignoreErrors);
        final Iterable<Ds3Object> ds3Objects = objectsToPut.getDs3Objects();
        appendPrefix(ds3Objects);

        return Transfer(helpers, ds3Objects, objectsToPut.getDs3IgnoredObjects());
    }

    private PutBulkResult Transfer(final Ds3ClientHelpers helpers, final Iterable<Ds3Object> ds3Objects, final ImmutableList<IgnoreFile> ds3IgnoredObjects) throws SignatureException, IOException, XmlProcessingException {
        final Ds3ClientHelpers.Job job = helpers.startWriteJob(bucketName, ds3Objects,
                WriteJobOptions.create()
                        .withPriority(priority)
                        .withWriteOptimization(writeOptimization));
        job.withMaxParallelRequests(numberOfThreads);
        if (checksum) {
            throw new RuntimeException("Checksum calculation is not currently supported."); //TODO
//            Logging.log("Performing bulk put with checksum computation enabled");
//            job.withRequestModifier(new ComputedChecksumModifier());
        }
        LOG.info("Created bulk put job " + job.getJobId().toString() + ", starting transfer");

        String resultMessage;
        if (pipe) {
            job.transfer(new PipeFileObjectPutter(mapNormalizedObjectNameToObjectName));
            resultMessage = String.format("SUCCESS: Wrote all piped files to bucket %s", bucketName);
        } else {
            job.transfer(new PrefixedFileObjectPutter(inputDirectory, prefix));
            resultMessage = String.format("SUCCESS: Wrote all the files in %s to bucket %s", inputDirectory.toString(), bucketName);
        }

        if (ignoreErrors && !ds3IgnoredObjects.isEmpty()) {
            resultMessage = String.format("WARN: Not all of the files were written to bucket %s", bucketName);
        }

        return new PutBulkResult(resultMessage, ds3IgnoredObjects);
    }

    private void appendPrefix(final Iterable<Ds3Object> ds3Objects) {
        if (pipe || prefix == null) return;

        LOG.info("Pre-appending " + prefix + " to all object names");
        for (final Ds3Object obj : ds3Objects) {
            obj.setName(prefix + obj.getName());
        }
    }

    private ImmutableMap<String, String> getNormalizedObjectNameToObjectName(final ImmutableList<Path> pipedFiles) {
        final ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        for (final Path file : pipedFiles) {
            map.put(Utils.normalizeObjectName(file.toString()), file.toString());
        }

        return map.build();
    }

    private Iterable<Path> filterObjects(final Iterable<Path> filesToPut, final String prefix) throws IOException, SignatureException {
        final Iterable<Contents> contents = getClientHelpers().listObjects(bucketName, prefix);
        final Map<String, Contents> mapBucketFiles = new HashMap<>();
        for (final Contents content : contents) {
            mapBucketFiles.put(content.getKey(), content);
        }

        final List<Path> filteredObjects = new ArrayList<>();
        for (final Path fileToPut : filesToPut) {
            final String fileName = Utils.getFileName(inputDirectory, fileToPut);
            final Contents content = mapBucketFiles.get(prefix + fileName);
            if (content == null) {
                filteredObjects.add(fileToPut);
            } else if (SyncUtils.isNewFile(fileToPut, content, true)) {
                LOG.info("Syncing new version of " + fileName);
                filteredObjects.add(fileToPut);
            } else {
                LOG.info("No need to sync " + fileName);
            }
        }

        return filteredObjects;
    }

    private Iterable<Path> getFilesToPut() throws IOException {
        final Iterable<Path> filesToPut;
        if (pipe) {
            filesToPut = pipedFiles;
        }
        else {
            filesToPut = Utils.listObjectsForDirectory(inputDirectory);
        }
        return filesToPut;
    }

    public boolean isOtherArgs(final Arguments args) {
        return  args.getDirectory()  != null || //-d
                args.getObjectName() != null || //-o
                args.getPrefix()     != null;   //-p
    }

    static class PrefixedFileObjectPutter implements Ds3ClientHelpers.ObjectChannelBuilder {

        final private LoggingFileObjectPutter objectPutter;
        final private String prefix;

        public PrefixedFileObjectPutter(final Path inputDirectory, final String prefix) {
            objectPutter = new LoggingFileObjectPutter(inputDirectory);
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
            objectPutter = new FileObjectPutter(inputDirectory);
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            LOG.info("Putting " + s + " to ds3 endpoint");
            return objectPutter.buildChannel(s);
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
            return FileChannel.open(Paths.get(mapNormalizedObjectNameToObjectName.get(s)), StandardOpenOption.READ);
        }
    }

}
