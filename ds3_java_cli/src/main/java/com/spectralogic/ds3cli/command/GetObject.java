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
import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.util.MetadataUtils;
import com.spectralogic.ds3cli.util.SyncUtils;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectGetter;
import com.spectralogic.ds3client.helpers.MetadataReceivedListener;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.models.bulk.PartialDs3Object;
import com.spectralogic.ds3client.models.common.Range;
import com.spectralogic.ds3client.networking.Metadata;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class GetObject extends CliCommand<DefaultResult> {

    private final static Logger LOG = LoggerFactory.getLogger(GetObject.class);

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, OBJECT_NAME);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(DIRECTORY, SYNC,
            FORCE, NUMBER_OF_THREADS, PRIORITY, RANGE_START, RANGE_LENGTH);

    private String bucketName;
    private String objectName;
    private String prefix;
    private boolean sync;
    private int numberOfThreads;
    private Priority priority;
    private long rangeStart = 0L;
    private long rangeLength = 0L;

    public GetObject() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);
        this.bucketName = args.getBucket();
        this.priority = args.getPriority();
        this.objectName = args.getObjectName();
        this.prefix = args.getDirectory();
        if (this.prefix == null) {
            this.prefix = ".";
        }
        if (args.isSync()) {
            LOG.info("Using sync command");
            this.sync = true;
        }
        this.numberOfThreads = args.getNumberOfThreads();

        // if you set one, you must set both start and length
        if (args.getRangeLength() != null || args.getRangeStart() != null ) {
            if (args.getRangeLength() == null || args.getRangeStart() == null) {
                throw new MissingArgumentException("Partial recovery must provide values for both "
                                                    + RANGE_START.getLongOpt() + " and "
                                                    + RANGE_LENGTH.getLongOpt());
            } else {
                this.rangeStart = Long.parseLong(args.getOptionValue(RANGE_START.getLongOpt()));
                this.rangeLength = Long.parseLong(args.getOptionValue(RANGE_LENGTH.getLongOpt()));
            }
        }
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final Ds3ClientHelpers helpers = getClientHelpers();
        final Path filePath = Paths.get(this.prefix, this.objectName);
        LOG.info("Output path: {}", filePath.toString());

        final Ds3Object ds3Obj;
        if (this.rangeLength > 0L) {
            ds3Obj = new PartialDs3Object(this.objectName.replace("\\", "/"),
                    Range.byLength(this.rangeStart, this.rangeLength));
        } else {
            ds3Obj = new Ds3Object(this.objectName.replace("\\", "/"));
        }

        if (this.sync && FileUtils.fileExists(filePath)) {
            if (SyncUtils.needToSync(helpers, this.bucketName, filePath, ds3Obj.getName(), false)) {
                this.Transfer(helpers, ds3Obj);
                return new DefaultResult("SUCCESS: Finished syncing object.");
            } else {
                return new DefaultResult("SUCCESS: No need to sync " + this.objectName);
            }
        }

        this.Transfer(helpers, ds3Obj);
        return new DefaultResult("SUCCESS: Finished downloading object.  The object was written to: " + filePath);
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
                MetadataUtils.restoreLastModified(filename, metadata, Paths.get(prefix, filename));
            }
        });
        job.transfer(new FileObjectGetter(Paths.get(this.prefix)));
    }
}
