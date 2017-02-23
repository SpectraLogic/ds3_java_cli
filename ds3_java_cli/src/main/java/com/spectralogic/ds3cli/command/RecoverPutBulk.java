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
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.PutBulkResult;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.JobRecoveryException;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class RecoverPutBulk extends PutBulk {

    private final static Logger LOG = LoggerFactory.getLogger(RecoverPutBulk.class);

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, ID);
    private final static ImmutableList<Option> optionalArgs
            = ImmutableList.of(PREFIX, NUMBER_OF_THREADS, WRITE_OPTIMIZATION,
            FOLLOW_SYMLINKS, PRIORITY, CHECKSUM,
            SYNC, FORCE, NUMBER_OF_THREADS, IGNORE_ERRORS,
            IGNORE_NAMING_CONFLICTS, DIRECTORY);

    private UUID jobId;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        // keep a copy of argumnets to reconstruct command line for recovery
        this.arguments = args;
        // PutBulk gathers all but ID
        processCommandOptions(requiredArgs, optionalArgs, args);
        this.jobId = UUID.fromString(args.getId());
        return populateFromArguments(args);
    }

    @Override
    protected PutBulkResult transfer(final Ds3ClientHelpers helpers, final Iterable<Ds3Object> ds3Objects, final ImmutableList<IgnoreFile> ds3IgnoredObjects)
            throws JobRecoveryException, IOException, XmlProcessingException {
        final Ds3ClientHelpers.Job job = helpers.recoverWriteJob(this.jobId);
        job.withMaxParallelRequests(this.numberOfThreads);
        printRecoveryCommand(job.getJobId());

        if (this.checksum) {
            throw new RuntimeException("Checksum calculation is not currently supported."); //TODO
//            Logging.log("Performing bulk put with checksum computation enabled");
//            job.withRequestModifier(new ComputedChecksumModifier());
        }
        LOG.info("Created recover bulk put job {}, starting transfer", job.getJobId().toString());

        String resultMessage;
        if (this.pipe) {

            final PipeFileObjectPutter pipeFileObjectPutter = new PipeFileObjectPutter(this.mapNormalizedObjectNameToObjectName);
            job.withMetadata(pipeFileObjectPutter).transfer(pipeFileObjectPutter);
            resultMessage = String.format("SUCCESS: Wrote all piped files to bucket %s", this.bucketName);
        } else {
            final PrefixedFileObjectPutter prefixedFileObjectPutter = new PrefixedFileObjectPutter(this.inputDirectory, this.prefix);
            job.withMetadata(prefixedFileObjectPutter).transfer(prefixedFileObjectPutter);
            resultMessage = String.format("SUCCESS: Wrote all the files in %s to bucket %s", this.inputDirectory.toString(), this.bucketName);
        }

        if (this.ignoreErrors && !ds3IgnoredObjects.isEmpty()) {
            resultMessage = String.format("WARN: Not all of the files were written to bucket %s", this.bucketName);
        }
        return new PutBulkResult(resultMessage, ds3IgnoredObjects);
    }

}
