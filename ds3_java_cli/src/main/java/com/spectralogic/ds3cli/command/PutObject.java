/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.Main;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.util.SyncUtils;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class PutObject extends CliCommand<DefaultResult> {

    private final static Logger LOG = LoggerFactory.getLogger(PutObject.class);

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, OBJECT_NAME);
    private final static ImmutableList<Option> optionalArgs
            = ImmutableList.of(PREFIX, SYNC, NUMBER_OF_THREADS,
            USER_METADATA, FILE_METADATA, PRIORITY, FORCE);

    private String bucketName;
    private Path objectPath;
    private String objectName;
    private String prefix;
    private boolean sync;
    private boolean force;
    private int numberOfThreads;
    private ImmutableMap<String, String> metadata;
    private Priority priority;
    private boolean archiveFileMetadata;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.priority = args.getPriority();
        this.bucketName = args.getBucket();
        this.objectName = args.getObjectName();
        this.objectPath = FileSystems.getDefault().getPath(this.objectName);
        this.prefix = args.getPrefix();
        this.force = args.isForce();

        this.sync = args.isSync();
        if (this.sync) {
            LOG.info("Using sync command");
        }
        this.numberOfThreads = args.getNumberOfThreads();
        this.metadata = args.getMetadata();
        this.archiveFileMetadata = args.doFileMetadata();

        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final Ds3ClientHelpers helpers = getClientHelpers();
        final Ds3Object ds3Obj = new Ds3Object(FileUtils.normalizeObjectName(this.objectName), getFileSystemProvider().size(this.objectPath));

        if (this.prefix != null) {
            LOG.info("Pre-appending {} to object name", this.prefix);
            ds3Obj.setName(this.prefix + ds3Obj.getName());
        }

        /* Ensure the bucket exists and if not create it */
        helpers.ensureBucketExists(this.bucketName);

        if (this.sync) {
            if (!SyncUtils.isSyncSupported(getClient())) {
                return new DefaultResult("Failed: The sync command is not supported with your version of BlackPearl.");
            }

            if (SyncUtils.needToSync(helpers, this.bucketName, this.objectPath, ds3Obj.getName(), true)) {
                this.transfer(helpers, ds3Obj);
                return new DefaultResult("Success: Finished syncing file to ds3 appliance.");
            } else {
                return new DefaultResult("Success: No need to sync " + this.objectName);
            }
        }

        this.transfer(helpers, ds3Obj);
        return new DefaultResult("Success: Finished writing file to ds3 appliance.");
    }

    private void transfer(final Ds3ClientHelpers helpers, final Ds3Object ds3Obj) throws IOException, XmlProcessingException {
        final WriteJobOptions writeJobOptions = WriteJobOptions.create();
        writeJobOptions.setForce(force);
        if (priority != null) {
            writeJobOptions.withPriority(priority);
        }
        final Ds3ClientHelpers.Job putJob = helpers.startWriteJob(this.bucketName, Lists.newArrayList(ds3Obj), writeJobOptions)
                .withMaxParallelRequests(this.numberOfThreads);

        final ImmutableMap.Builder<String, String> metadataBuilder = ImmutableMap.builder();

        if (!Guard.isMapNullOrEmpty(metadata)) {
            metadataBuilder.putAll(metadata);
        }

        if (archiveFileMetadata) {
            metadataBuilder.putAll(Main.metadataUtils().getMetadataValues(objectPath));
        }

        putJob.withMetadata(fileOrObjectName -> metadataBuilder.build());

        putJob.transfer(fileOrObjectName -> FileChannel.open(objectPath, StandardOpenOption.READ));
    }

}
