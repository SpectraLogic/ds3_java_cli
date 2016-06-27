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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.exceptions.SyncNotSupportedException;
import com.spectralogic.ds3cli.models.PutObjectResult;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SignatureException;
import java.util.Map;

public class PutObject extends CliCommand<PutObjectResult> {

    private final static Logger LOG = LoggerFactory.getLogger(PutObject.class);

    private String bucketName;
    private Path objectPath;
    private String objectName;
    private String prefix;
    private boolean sync;
    private boolean force;
    private int numberOfThreads;
    private ImmutableMap<String, String> metadata;

    public PutObject(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        if (this.bucketName == null) {
            throw new MissingOptionException("The put object command requires '-b' to be set.");
        }
        this.objectName = args.getObjectName();
        if (this.objectName == null) {
            throw new MissingOptionException("The put object command requires '-o' to be set.");
        }

        if (args.getDirectory() != null) {
            throw new BadArgumentException("'-d' should not be used with the command 'put_object'.  If you want to move an entire directory, use 'put_bulk' instead.");
        }

        this.objectPath = FileSystems.getDefault().getPath(args.getObjectName());
        if (!getFileUtils().exists(this.objectPath)) {
            throw new BadArgumentException("File '" + this.objectName + "' does not exist.");
        }
        if (!getFileUtils().isRegularFile(this.objectPath)) {
            throw new BadArgumentException("The '-o' command must be a file and not a directory.");
        }

        this.prefix = args.getPrefix();
        this.force = args.isForce();

        if (args.isSync()) {
            if (!SyncUtils.isSyncSupported(getClient())) {
                throw new SyncNotSupportedException("The sync command is not supported with your version of BlackPearl.");
            }

            LOG.info("Using sync command");
            this.sync = true;
        }

        this.numberOfThreads = Integer.valueOf(args.getNumberOfThreads());

        this.metadata = args.getMetadata();

        return this;
    }

    @Override
    public String getLongHelp() {
        final StringBuffer helpStringBuffer = new StringBuffer();
        helpStringBuffer.append("This is provided for Amazon S3 compatibility.\n");
        helpStringBuffer.append("Spectra Logic recommends using Spectra S3 requests to create a PUT job.\n");
        helpStringBuffer.append("Requires the '-b' parameter to specify bucket (name or UUID).\n");
        helpStringBuffer.append("Requires the '-o' parameter to specify local object name.\n");
        helpStringBuffer.append("Optional '-p' parameter (unless | ) to specify prefix or directory name.\n");
        helpStringBuffer.append("Optional '--sync' flag to put only newer or non-extant files.\n");
        helpStringBuffer.append("Optional '-nt' parameter to specify number of threads.\n");
        helpStringBuffer.append("Optional '--ignore-errors' flag to continue on errors.\n");
        helpStringBuffer.append("Optional '--follow-symlinks' flag to follow symlink (default is disregard).\n");
        helpStringBuffer.append("Optional '--metadata' parameter to add metadata (key:value,key2:value2).\n");

        return helpStringBuffer.toString();
    }

    @Override
    public PutObjectResult call() throws Exception {
        final Ds3ClientHelpers helpers = getClientHelpers();
        final Ds3Object ds3Obj = new Ds3Object(Utils.normalizeObjectName(this.objectName), getFileUtils().size(this.objectPath));

        if (this.prefix != null) {
            LOG.info("Pre-appending " + this.prefix + " to object name");
            ds3Obj.setName(this.prefix + ds3Obj.getName());
        }

        /* Ensure the bucket exists and if not create it */
        helpers.ensureBucketExists(this.bucketName);

        if (this.sync) {
            if (!SyncUtils.isSyncSupported(getClient())) {
                return new PutObjectResult("Failed: The sync command is not supported with your version of BlackPearl.");
            }

            if (SyncUtils.needToSync(helpers, this.bucketName, this.objectPath, ds3Obj.getName(), true)) {
                this.transfer(helpers, ds3Obj);
                return new PutObjectResult("Success: Finished syncing file to ds3 appliance.");
            } else {
                return new PutObjectResult("Success: No need to sync " + this.objectName);
            }
        }

        this.transfer(helpers, ds3Obj);
        return new PutObjectResult("Success: Finished writing file to ds3 appliance.");
    }

    private void transfer(final Ds3ClientHelpers helpers, final Ds3Object ds3Obj) throws SignatureException, IOException, XmlProcessingException {
        final Ds3ClientHelpers.Job putJob = helpers.startWriteJob(this.bucketName, Lists.newArrayList(ds3Obj));

        if (!Guard.isMapNullOrEmpty(metadata)) {
            putJob.withMetadata(new Ds3ClientHelpers.MetadataAccess() {
                @Override
                public Map<String, String> getMetadataValue(final String s) {
                    return metadata;
                }
            });
        }

        putJob.withMaxParallelRequests(this.numberOfThreads);
        putJob.withMetadata(new Ds3ClientHelpers.MetadataAccess() {
            @Override
            public Map<String, String> getMetadataValue(final String filename) {
                return Utils.getMetadataValues(objectPath);
            }
        }).transfer(new Ds3ClientHelpers.ObjectChannelBuilder() {
            @Override
            public SeekableByteChannel buildChannel(final String s) throws IOException {
                return FileChannel.open(objectPath, StandardOpenOption.READ);
            }
        });
    }

}
