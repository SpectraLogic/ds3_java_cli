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

import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.logging.Logging;
import com.spectralogic.ds3cli.models.GetObjectResult;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetObjectRequest;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectGetter;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;

import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.List;

public class GetObject extends CliCommand<GetObjectResult> {

    private String bucketName;
    private String objectName;
    private String prefix;
    private GetObjectRequest.Range byteRange;

    public GetObject(final Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The get object command requires '-b' to be set.");
        }

        objectName = args.getObjectName();
        if (objectName == null) {
            throw new MissingOptionException("The get object command requires '-o' to be set.");
        }

        prefix = args.getDirectory();
        if (prefix == null) {
            prefix = ".";
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public GetObjectResult call() throws Exception {
        try {
            final Path filePath = Paths.get(prefix, objectName);
            Logging.log("Output path: " + filePath.toString());

            Files.createDirectories(filePath.getParent());

            final Ds3ClientHelpers helpers = Ds3ClientHelpers.wrap(getClient());
            final List<Ds3Object> ds3ObjectList = Lists.newArrayList(new Ds3Object(objectName));

            final Ds3ClientHelpers.Job job = helpers.startReadJob(bucketName, ds3ObjectList);

            job.transfer(new FileObjectGetter(Paths.get(prefix)));

            return new GetObjectResult("SUCCESS: Finished downloading object.  The object was written out to: " + filePath);
        }
        catch(final FailedRequestException e) {
            if(e.getStatusCode() == 500) {
                Logging.log(e.getMessage());
                if (Logging.isVerbose()) {
                    e.printStackTrace();
                }
                throw new CommandException( "Error: Cannot communicate with the remote DS3 appliance.", e);
            }
            else if(e.getStatusCode() == 404) {
                if (Logging.isVerbose()) {
                    e.printStackTrace();
                }
                throw new CommandException( "Error: " + e.getMessage(), e);
            }
            else {
                if (Logging.isVerbose()) {
                    e.printStackTrace();
                }
                throw new CommandException( "Error: Encountered an unknown error of ("+ e.getStatusCode() +") while accessing the remote DS3 appliance.", e);
            }
        }
    }
}
