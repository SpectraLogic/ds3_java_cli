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

import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectGetter;
import com.spectralogic.ds3client.helpers.MetadataReceivedListener;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.*;
import com.spectralogic.ds3client.networking.Metadata;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GetObject extends CliCommand<DefaultResult> {

    private final static Logger LOG = LoggerFactory.getLogger(GetObject.class);

    private String bucketName;
    private String objectName;
    private String prefix;
    private boolean sync;
    private boolean force;
    private int numberOfThreads;
    private Priority priority;

    public GetObject() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.priority = args.getPriority();
        this.bucketName = args.getBucket();
        if (this.bucketName == null) {
            throw new MissingOptionException("The get object command requires '-b' to be set.");
        }

        this.objectName = args.getObjectName();
        if (this.objectName == null) {
            throw new MissingOptionException("The get object command requires '-o' to be set.");
        }

        this.prefix = args.getDirectory();
        if (this.prefix == null) {
            this.prefix = ".";
        }

        if (args.isSync()) {
            LOG.info("Using sync command");
            this.sync = true;
        }

        this.force = args.isForce();
        this.numberOfThreads = Integer.valueOf(args.getNumberOfThreads());

        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        try {
            final Ds3ClientHelpers helpers = getClientHelpers();
            final Path filePath = Paths.get(this.prefix, this.objectName);
            LOG.info("Output path: {}", filePath.toString());

            final Ds3Object ds3Obj = new Ds3Object(this.objectName.replace("\\", "/"));
            if (this.sync && Utils.fileExists(filePath)) {
                if (SyncUtils.needToSync(helpers, this.bucketName, filePath, ds3Obj.getName(), false)) {
                    this.Transfer(helpers, ds3Obj);
                    return new DefaultResult("SUCCESS: Finished syncing object.");
                } else {
                    return new DefaultResult("SUCCESS: No need to sync " + this.objectName);
                }
            }

            this.Transfer(helpers, ds3Obj);
            return new DefaultResult("SUCCESS: Finished downloading object.  The object was written to: " + filePath);
        } catch (final FailedRequestException e) {
            switch (e.getStatusCode()) {
                case 500:
                    throw new CommandException("Error: Cannot communicate with the remote DS3 appliance.", e);
                case 404:
                    throw new CommandException("Error: " + e.getMessage(), e);
                default:
                    throw new CommandException("Error: Encountered an unknown error of (" + e.getStatusCode() + ") while accessing the remote DS3 appliance.", e);
            }
        }
    }

    private void Transfer(final Ds3ClientHelpers helpers, final Ds3Object ds3Obj) throws IOException, XmlProcessingException {
        final List<Ds3Object> ds3ObjectList = Lists.newArrayList(ds3Obj);
        final ReadJobOptions readJobOptions = ReadJobOptions.create();
        if (priority != null) {
            readJobOptions.withPriority(priority);
        }
        final Ds3ClientHelpers.Job job = helpers.startReadJob(this.bucketName, ds3ObjectList, readJobOptions);
        job.withMaxParallelRequests(this.numberOfThreads);
        job.attachMetadataReceivedListener(new MetadataReceivedListener() {
            @Override
            public void metadataReceived(final String filename, final Metadata metadata) {
                Utils.restoreLastModified(filename, metadata, Paths.get(prefix, filename));
            }
        });
        job.transfer(new FileObjectGetter(Paths.get(this.prefix)));
    }
}
