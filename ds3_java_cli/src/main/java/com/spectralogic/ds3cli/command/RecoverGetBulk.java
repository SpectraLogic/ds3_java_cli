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
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.Main;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.models.RecoveryJob;

import com.spectralogic.ds3cli.util.LoggingFileObjectGetter;
import com.spectralogic.ds3cli.util.RecoveryFileManager;
import com.spectralogic.ds3client.helpers.*;

import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class RecoverGetBulk extends CliCommand<DefaultResult> implements RecoverableCommand {

    private final static Logger LOG = LoggerFactory.getLogger(RecoverGetBulk.class);

    private UUID jobId;
    private String bucketName;
    private String directory;
    private Path outputPath;
    private ImmutableList<String> prefixes;
    private int numberOfThreads;
    private boolean restoreMetadata;

    private static final Option PREFIXES = Option.builder("p").hasArgs().argName("prefixes")
            .desc("get only objects whose names start with prefix  "
                    + "separate multiple prefixes with spaces").build();

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, ID);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(DIRECTORY, PREFIXES, NUMBER_OF_THREADS, FILE_METADATA);

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);
        this.jobId = UUID.fromString(args.getId());
        this.bucketName = args.getBucket();
        this.numberOfThreads = args.getNumberOfThreads();

        this.directory = args.getDirectory();
        if (Guard.isStringNullOrEmpty(this.directory) || directory.equals(".")) {
            this.outputPath = FileSystems.getDefault().getPath(".");
        } else {
            final Path dirPath = FileSystems.getDefault().getPath(directory);
            this.outputPath = FileSystems.getDefault().getPath(".").resolve(dirPath);
        }
        LOG.info("Output Path = {}", this.outputPath);

        final String[] prefix = args.getOptionValues(PREFIXES.getOpt());
        if(prefix != null && prefix.length > 0) {
            this.prefixes = ImmutableList.copyOf(prefix);
        }

        this.restoreMetadata = args.doFileMetadata();

        return this;
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
    public DefaultResult call() throws Exception {
        final Ds3ClientHelpers.ObjectChannelBuilder getter = new FileObjectGetter(this.outputPath);

        if (!Guard.isNotNullAndNotEmpty(prefixes)) {
            LOG.info("Getting all objects from {}", this.bucketName);
        } else {
            LOG.info("Getting only those objects that start with {}", Joiner.on(" ").join(this.prefixes));
        }
        return new DefaultResult(this.restore(getter));
    }


    protected String restore(final Ds3ClientHelpers.ObjectChannelBuilder getter) throws JobRecoveryException, IOException, XmlProcessingException {
        final Ds3ClientHelpers helper = getClientHelpers();
        final Ds3ClientHelpers.Job job = helper.recoverReadJob(this.jobId);
        job.withMaxParallelRequests(this.numberOfThreads);
        final LoggingFileObjectGetter loggingFileObjectGetter = new LoggingFileObjectGetter(getter);

        if (restoreMetadata) {
            job.attachMetadataReceivedListener((fileOrObjectName, metadata) -> Main.metadataUtils().restoreMetadataValues(fileOrObjectName, metadata, Paths.get(outputPath.toString(), fileOrObjectName)));
        }

        // start transfer
        job.transfer(loggingFileObjectGetter);

        // clean up recovery file on success of job.transfer()
        RecoveryFileManager.deleteRecoveryCommand(job.getJobId());

        // Success -- build the response
        final StringBuilder response = new StringBuilder("SUCCESS: Wrote");
        response.append(Guard.isNullOrEmpty(this.prefixes) ? " all the objects"
                : " all the objects that start with '" + Joiner.on(" ").join(this.prefixes) + "'");
        response.append(" from ");
        response.append(this.bucketName);
        response.append(" to ");
        response.append(this.outputPath.toString());

        return response.toString();
    }

}
