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
import com.spectralogic.ds3cli.logging.Logging;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.PutBucketRequest;
import com.spectralogic.ds3client.models.bulk.Priority;
import com.spectralogic.ds3client.models.bulk.WriteOptimization;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;

public class PutBucket extends CliCommand {

    private String bucketName;
    private Priority defaultPutPriority;
    private Priority defaultGetPriority;
    private WriteOptimization defaultWriteOptimization;

    public PutBucket(final Ds3Client client) {
        super(client);
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
    public String call() throws Exception {
        try {
            final PutBucketRequest request = new PutBucketRequest(bucketName);
            if (this.defaultGetPriority != null) {
                Logging.logf("Adding a default get priority (%s) to the create bucket", this.defaultGetPriority.toString());
                request.withDefaultGetJobPriority(defaultGetPriority);
            }
            if (this.defaultPutPriority != null) {
                Logging.logf("Adding a default put priority (%s) to the create bucket", this.defaultPutPriority.toString());
                request.withDefaultPutJobPriority(defaultPutPriority);
            }
            if (this.defaultWriteOptimization != null) {
                Logging.logf("Adding a default write optimization (%s) to the create bucket", this.defaultWriteOptimization.toString());
                request.withDefaultWriteOptimization(defaultWriteOptimization);
            }
            getClient().putBucket(request);
        }
        catch (final IOException e) {
             return "Error: Request failed with the following error: " + e.getMessage();
        }
        return "Success: Created bucket '"+ bucketName+"'.";
    }
}
