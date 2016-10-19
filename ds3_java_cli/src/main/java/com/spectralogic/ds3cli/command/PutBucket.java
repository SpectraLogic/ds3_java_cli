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

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.PutBucketRequest;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;

public class PutBucket extends CliCommand<DefaultResult> {

    private String bucketName;

    public PutBucket() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The put bucket command requires '-b' to be set.");
        }

        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        try {
            final PutBucketRequest request = new PutBucketRequest(bucketName);
            getClient().putBucket(request);
            return new DefaultResult("Success: created bucket " + bucketName + ".");
        }
        catch(final FailedRequestException e) {
            if (e.getStatusCode() == 409) {
                throw new CommandException("Bucket " + bucketName + " already exists", e);
            }
            throw e;
        }
    }
}
