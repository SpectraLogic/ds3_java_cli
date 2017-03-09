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
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.RecoveryJob;

import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FolderNameFilter;
import com.spectralogic.ds3client.helpers.JobRecoveryException;
import com.spectralogic.ds3client.helpers.pagination.GetBucketLoaderFactory;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;

import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.collections.LazyIterable;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class RecoverGetBulk extends GetBulk implements RecoverableCommand {

    private final static Logger LOG = LoggerFactory.getLogger(RecoverGetBulk.class);

    private UUID jobId;

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, ID);
    private final static ImmutableList<Option> optionalArgs
            = ImmutableList.of(DIRECTORY, PREFIXES, NUMBER_OF_THREADS,
            DISCARD, PRIORITY, SYNC, FORCE);

    public RecoverGetBulk() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        // keep a copy of argumnets to reconstruct command line for recovery
        this.arguments = args;
        // PutBulk gathers all but ID
        processCommandOptions(requiredArgs, optionalArgs, args);
        this.jobId = UUID.fromString(args.getId());
        return populateFromArguments(args);
    }

    // init from recovery file
    public CliCommand init(final RecoveryJob job) throws Exception {
        this.bucketName = job.getBucketName();
        this.numberOfThreads = job.getNumberOfThreads();
        this.jobId = job.getId();
        this.directory = job.getDirectory();
        if (Guard.isStringNullOrEmpty(this.directory) || directory.equals(".")) {
            this.outputPath = FileSystems.getDefault().getPath(".");
        } else {
            final Path dirPath = FileSystems.getDefault().getPath(directory);
            this.outputPath = FileSystems.getDefault().getPath(".").resolve(dirPath);
        }
        LOG.info("Output Path = {}", this.outputPath);

        final List<String> prefix = job.getPrefixes();
        if (prefix != null && prefix.size() > 0) {
            this.prefixes = ImmutableList.copyOf(prefix);
        }
        return this;
    }

    @Override
    protected String restoreSome(final Ds3ClientHelpers.ObjectChannelBuilder getter) throws JobRecoveryException, IOException, XmlProcessingException {
        final Ds3ClientHelpers helper = getClientHelpers();

        final Iterable<Contents> prefixMatches;
        if (Guard.isNullOrEmpty(prefixes)) {
            prefixMatches = new LazyIterable<>(
                    new GetBucketLoaderFactory(getClient(), this.bucketName, null, null, 100, 5));
        } else {
            prefixMatches = getObjectsByPrefix();
        }
        if (Iterables.isEmpty(prefixMatches)) {
            return "No objects in bucket " + this.bucketName + " with prefixes '" + Joiner.on(" ").join(this.prefixes) + "'";
        }

        final Iterable<Ds3Object> objects;
        objects = helper.toDs3Iterable(prefixMatches, FolderNameFilter.filter());

        final Ds3ClientHelpers.Job job = helper.recoverReadJob(this.jobId);
        job.withMaxParallelRequests(this.numberOfThreads);
        final LoggingFileObjectGetter loggingFileObjectGetter = new LoggingFileObjectGetter(getter, this.outputPath);
        job.attachMetadataReceivedListener(loggingFileObjectGetter);

        // start transfer
        job.transfer(loggingFileObjectGetter);

        // Success -- build the response
        final StringBuilder response = new StringBuilder("SUCCESS: ");
        response.append(Guard.isNullOrEmpty(this.prefixes) ? " all the objects"
                : " all the objects that start with '" + Joiner.on(" ").join(this.prefixes) + "'");
        response.append(" from ");
        response.append(this.bucketName);
        response.append(this.discard ? "" : " to " + this.outputPath.toString());

        return response.toString();
    }

    @Override
    protected String restoreAll(final Ds3ClientHelpers.ObjectChannelBuilder getter) throws JobRecoveryException, XmlProcessingException, IOException {
        final Ds3ClientHelpers helper = getClientHelpers();
        final Ds3ClientHelpers.Job job = helper.recoverReadJob(this.jobId);
        job.withMaxParallelRequests(this.numberOfThreads);
        final LoggingFileObjectGetter loggingFileObjectGetter = new LoggingFileObjectGetter(getter, this.outputPath);
        job.attachMetadataReceivedListener(loggingFileObjectGetter);

        // start transfer
        job.transfer(loggingFileObjectGetter);

        // delete recovery file on success
        deleteRecoveryCommand(job.getJobId());

        if (this.discard) {
            return "SUCCESS: Retrieved and discarded all objects from " + this.bucketName;
        } else {
            return "SUCCESS: Wrote all the objects from " + this.bucketName + " to directory " + this.outputPath.toString();
        }
    }

}
