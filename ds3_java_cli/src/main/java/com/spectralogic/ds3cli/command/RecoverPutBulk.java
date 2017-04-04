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

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.models.PutBulkResult;
import com.spectralogic.ds3cli.models.RecoveryJob;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.JobRecoveryException;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class RecoverPutBulk extends CliCommand<PutBulkResult> implements RecoverableCommand {

    private final static Logger LOG = LoggerFactory.getLogger(RecoverPutBulk.class);

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, ID, DIRECTORY);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(PREFIX, NUMBER_OF_THREADS);

    private UUID jobId;
    private String bucketName;
    private Path inputDirectory;
    private String prefix;
    private int numberOfThreads;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);
        this.jobId = UUID.fromString(args.getId());
        this.bucketName = args.getBucket();
        final String srcDir = args.getDirectory();
        this.inputDirectory = Paths.get(srcDir);
        this.prefix = args.getPrefix();
        this.numberOfThreads = args.getNumberOfThreads();

        return this;
    }

    // init from recovery job file
    public CliCommand init(final RecoveryJob job) throws Exception {
        this.bucketName = job.getBucketName();
        this.numberOfThreads = job.getNumberOfThreads();
        this.jobId = job.getId();
        if (Guard.isStringNullOrEmpty(job.getDirectory()) || job.getDirectory().equals(".")) {
            this.inputDirectory = FileSystems.getDefault().getPath(".");
        } else {
            this.inputDirectory = Paths.get(job.getDirectory());
        }
        LOG.info("Input Path = {}", this.inputDirectory);

        final List<String> prefix = job.getPrefixes();
        // only one prefix on put
        if (prefix != null) {
            if (prefix.size() == 1) {
                this.prefix = prefix.get(0);
            } else {
                // this really shouldn't happen, but . . .
                throw new BadArgumentException("Only one prefix allowed on put_bulk");
            }
        }
        return this;
    }

    @Override
    public PutBulkResult call() throws Exception {
        /* Ensure the bucket exists and if not create it */
        final Ds3ClientHelpers helpers = getClientHelpers();
        helpers.ensureBucketExists(this.bucketName);
        return this.transfer(helpers);
    }

    protected PutBulkResult transfer(final Ds3ClientHelpers helpers)
            throws JobRecoveryException, IOException, XmlProcessingException {
        final Ds3ClientHelpers.Job job = helpers.recoverWriteJob(this.jobId);
        job.withMaxParallelRequests(this.numberOfThreads);

        final PrefixedFileObjectPutter prefixedFileObjectPutter = new PrefixedFileObjectPutter(this.inputDirectory, this.prefix);
        job.withMetadata(prefixedFileObjectPutter).transfer(prefixedFileObjectPutter);
        // clean up recovery job on success
        RecoveryFileManager.deleteRecoveryCommand(job.getJobId());
        return new PutBulkResult(String.format("SUCCESS: Wrote all the files in %s to bucket %s", this.inputDirectory.toString(), this.bucketName), null);
    }

}
