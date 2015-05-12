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

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.models.PutBucketResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.PutBucketRequest;
import com.spectralogic.ds3client.models.bulk.Priority;
import com.spectralogic.ds3client.models.bulk.WriteOptimization;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PutBucket extends CliCommand<PutBucketResult> {

    private static final Logger LOG = LoggerFactory.getLogger(PutBucket.class);

    private String bucketName;
    private Priority defaultPutPriority;
    private Priority defaultGetPriority;
    private WriteOptimization defaultWriteOptimization;

    public PutBucket(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The put bucket command requires '-b' to be set.");
        }

        this.defaultGetPriority = args.getDefaultGetPriority();
        this.defaultPutPriority = args.getDefaultPutPriority();
        this.defaultWriteOptimization = args.getDefaultWriteOptimization();

        return this;
    }

    @Override
    public PutBucketResult call() throws Exception {
        try {
            final PutBucketRequest request = new PutBucketRequest(bucketName);

            if (this.defaultGetPriority != null) {
                LOG.info("Adding a default get priority (" + this.defaultGetPriority.toString() + ") to the create bucket");
                request.withDefaultGetJobPriority(defaultGetPriority);
            }
            if (this.defaultPutPriority != null) {
                LOG.info("Adding a default put priority (" + this.defaultPutPriority.toString() + ") to the create bucket");
                request.withDefaultPutJobPriority(defaultPutPriority);
            }
            if (this.defaultWriteOptimization != null) {
                LOG.info("Adding a default write optimization (" + this.defaultWriteOptimization.toString() + ") to the create bucket");
                request.withDefaultWriteOptimization(defaultWriteOptimization);
            }

            getClient().putBucket(request);
            return new PutBucketResult("Success: created bucket " + bucketName + ".");
        }
        catch(final FailedRequestException e) {
            if (e.getStatusCode() == 409) {
                throw new CommandException("Bucket " + bucketName + " already exists", e);
            }
            throw new CommandException("Encountered a DS3 Error", e);
        }
        catch (final IOException e) {
             throw new CommandException("Encountered an error when communicating with ds3 endpoint", e);
        }
    }
}
