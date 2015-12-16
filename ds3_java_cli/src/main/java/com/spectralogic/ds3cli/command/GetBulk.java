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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.GetBulkResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.util.SyncUtils;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectGetter;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.models.bulk.Priority;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.SSLSetupException;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetBulk extends CliCommand<GetBulkResult> {

    private final static Logger LOG = LoggerFactory.getLogger(GetBulk.class);

    private String bucketName;
    private Path outputPath;
    private String prefix;
    private boolean checksum;
    private Priority priority;
    private boolean sync;

    public GetBulk(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        if (this.bucketName == null) {
            throw new MissingOptionException("The bulk get command requires '-b' to be set.");
        }

        if (args.getObjectName() != null) {
            System.out.println("Warning: '-o' is not used with bulk get and is ignored.");
        }

        final String directory = args.getDirectory();
        if (directory == null || directory.equals(".")) {
            this.outputPath = FileSystems.getDefault().getPath(".");
        } else {
            final Path dirPath = FileSystems.getDefault().getPath(directory);
            this.outputPath = FileSystems.getDefault().getPath(".").resolve(dirPath);
        }

        this.priority = args.getPriority();
        this.checksum = args.isChecksum();
        this.prefix = args.getPrefix();

        if (args.isSync()) {
            LOG.info("Using sync command");
            this.sync = true;
        }

        return this;
    }

    @Override
    public GetBulkResult call() throws Exception {
        final Ds3ClientHelpers.ObjectChannelBuilder getter;
        if (checksum) {
            throw new RuntimeException("Checksumming is currently not implemented.");//TODO
//            Logging.log("Performing get_bulk with checksum verification");
//            getter = new VerifyingFileObjectGetter(this.outputPath);
        }
        else {
            getter = new FileObjectGetter(this.outputPath);
        }

        if (sync) {
            //TODO use Guard.isStringNullOrEmpty(this.prefix) when java sdk 1.2.3 will be released
            if (this.prefix == null || this.prefix.isEmpty()) {
                LOG.info("Syncing all objects from " + this.bucketName);
            }
            else {
                LOG.info("Syncing only those objects that start with " + this.prefix);
            }
            return new GetBulkResult(restoreSome(getter));
        }

        if (this.prefix == null) {
            LOG.info("Getting all objects from " + this.bucketName);
            return new GetBulkResult(restoreAll(getter));
        }

        LOG.info("Getting only those objects that start with " + this.prefix);
        return new GetBulkResult(restoreSome(getter));
    }

    private String restoreSome(final Ds3ClientHelpers.ObjectChannelBuilder getter) throws IOException, SignatureException, XmlProcessingException, SSLSetupException {
        final Ds3ClientHelpers helper = getClientHelpers();
        final Iterable<Contents> contents = helper.listObjects(this.bucketName, this.prefix);

        final Iterable<Contents> filteredContents;
        if (sync) {
            filteredContents = filterContents(contents, this.outputPath);
            if (Iterables.isEmpty(filteredContents)) {
                return "SUCCESS: All files are up to date";
            }
        }
        else {
            filteredContents = null;
        }

        final Iterable<Ds3Object> objects = Iterables.transform(
                filteredContents == null ? contents : filteredContents,
                new Function<Contents, Ds3Object>() {
                    @Override
                    public Ds3Object apply(final Contents input) {
                        return new Ds3Object(input.getKey(), input.getSize());
                    }
                });

        final Ds3ClientHelpers.Job job = helper.startReadJob(this.bucketName, objects,
                ReadJobOptions.create()
                .withPriority(this.priority));

        job.transfer(new LoggingFileObjectGetter(getter));

        if (sync) {
            if (prefix != null) {
                return "SUCCESS: Synced all the objects that start with '" + this.prefix + "' from " + this.bucketName + " to " + this.outputPath.toString();
            }
            else {
                return "SUCCESS: Synced all the objects from " + this.bucketName + " to " + this.outputPath.toString();
            }
        }

        return "SUCCESS: Wrote all the objects that start with '"+ this.prefix +"' from " + this.bucketName + " to " + this.outputPath.toString();
    }

    private String restoreAll(final Ds3ClientHelpers.ObjectChannelBuilder getter) throws XmlProcessingException, SignatureException, IOException, SSLSetupException {
        final Ds3ClientHelpers helper = getClientHelpers();
        final Ds3ClientHelpers.Job job = helper.startReadAllJob(this.bucketName,
                ReadJobOptions.create()
                .withPriority(this.priority));

        job.transfer(new LoggingFileObjectGetter(getter));

        return "SUCCESS: Wrote all the objects from " + this.bucketName + " to directory " + this.outputPath.toString();
    }

    private Iterable<Contents> filterContents(final Iterable<Contents> contents, final Path outputPath) throws IOException {
        final Iterable<Path> localFiles = Utils.listObjectsForDirectory(outputPath);
        final Map<String, Path> mapLocalFiles = new HashMap<>();
        for (final Path localFile : localFiles) {
            mapLocalFiles.put(Utils.getFileName(outputPath, localFile), localFile);
        }

        final List<Contents> filteredContents = new ArrayList<>();
        for (final Contents content : contents) {
            final Path filePath = mapLocalFiles.get(content.getKey());
            if (filePath == null) {
                filteredContents.add(content);
            }
            else if (SyncUtils.isNewFile(filePath, content, false)) {
                LOG.info("Syncing new version of " + filePath.toString());
                filteredContents.add(content);
            }
            else {
                LOG.info("No need to sync " + filePath.toString());
            }
        }

        return filteredContents;
    }

    class LoggingFileObjectGetter implements Ds3ClientHelpers.ObjectChannelBuilder {

        final private Ds3ClientHelpers.ObjectChannelBuilder objectGetter;

        public LoggingFileObjectGetter(final Ds3ClientHelpers.ObjectChannelBuilder getter) {
            this.objectGetter = getter;
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            LOG.info("Getting object " + s);
            return this.objectGetter.buildChannel(s);
        }
    }
}
