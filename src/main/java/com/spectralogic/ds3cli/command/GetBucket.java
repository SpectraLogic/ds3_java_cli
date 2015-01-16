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
import com.spectralogic.ds3cli.models.BucketResult;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;

import java.util.Iterator;

public class GetBucket extends CliCommand<BucketResult> {
    private String bucketName;
    public GetBucket(final Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The get bucket command requires '-b' to be set.");
        }
        return this;
    }

    @Override
    public BucketResult call() throws Exception {

        try {
            final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(getClient());

            final Iterable<Contents> objects = helper.listObjects(bucketName);
            final Iterator<Contents> objIterator = objects.iterator();

            if(!objIterator.hasNext()) {
                throw new CommandException("No objects were reported in the bucket '" + bucketName + "'" );
            }

            return new BucketResult( bucketName, objIterator);

        }
        catch(final FailedRequestException e) {
            if(e.getStatusCode() == 500) {
                throw new CommandException("Error: Cannot communicate with the remote DS3 appliance.", e);
            }
            else if(e.getStatusCode() == 404) {
                throw new CommandException("Error: Unknown bucket.", e);
            }
            else {
                throw new CommandException("Error: Encountered an unknown error of ("+ e.getStatusCode() +") while accessing the remote DS3 appliance.", e);
            }
        }
    }
}
