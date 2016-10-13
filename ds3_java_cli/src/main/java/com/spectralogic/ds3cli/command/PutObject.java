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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import static com.spectralogic.ds3cli.ArgumentFactory.*;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.exceptions.SyncNotSupportedException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.Priority;
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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class PutObject extends CliCommand<DefaultResult> {

    private final static Logger LOG = LoggerFactory.getLogger(PutObject.class);

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, OBJECT_NAME);
    private final static ImmutableList<Option> optionalArgs
            = ImmutableList.of(PREFIX, SYNC, NUMBER_OF_THREADS,
            METADATA, PRIORITY);

    private String bucketName;
    private Path objectPath;
    private String objectName;
    private String prefix;
    private boolean sync;
    private int numberOfThreads;
    private ImmutableMap<String, String> metadata;
    private Priority priority;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        addOptionalArguments(optionalArgs, args);
        args.parseCommandLine();

        this.priority = args.getPriority();
        this.bucketName = args.getBucket();
        this.objectName = args.getObjectName();
        this.objectPath = FileSystems.getDefault().getPath(this.objectName);
        this.prefix = args.getPrefix();
        if (this.sync = args.isSync()) {
            LOG.info("Using sync command");
        }
        this.numberOfThreads = Integer.valueOf(args.getNumberOfThreads());
        this.metadata = args.getMetadata();
        this.viewType = args.getOutputFormat();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final Ds3ClientHelpers helpers = getClientHelpers();
        final Ds3Object ds3Obj = new Ds3Object(Utils.normalizeObjectName(this.objectName), getFileUtils().size(this.objectPath));

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
        if (priority != null) {
            writeJobOptions.withPriority(priority);
        }
        final Ds3ClientHelpers.Job putJob = helpers.startWriteJob(this.bucketName, Lists.newArrayList(ds3Obj), writeJobOptions)
                .withMaxParallelRequests(this.numberOfThreads);

        if (!Guard.isMapNullOrEmpty(metadata)) {
            putJob.withMetadata(new Ds3ClientHelpers.MetadataAccess() {
                @Override
                public Map<String, String> getMetadataValue(final String s) {

                    return new ImmutableMap.Builder<String, String>()
                            .putAll(Utils.getMetadataValues(objectPath))
                            .putAll(metadata).build();
                }
            });
        }

        putJob.transfer(new Ds3ClientHelpers.ObjectChannelBuilder() {
            @Override
            public SeekableByteChannel buildChannel(final String s) throws IOException {
                return FileChannel.open(objectPath, StandardOpenOption.READ);
            }
        });
    }

}
