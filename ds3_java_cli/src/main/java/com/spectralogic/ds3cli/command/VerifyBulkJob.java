/*
 * ******************************************************************************
 *   Copyright 2014 - 2016 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.VerifyBulkJobResult;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.commands.spectrads3.VerifyBulkJobSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.VerifyBulkJobSpectraS3Response;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingOptionException;

import java.util.ArrayList;
import java.util.List;

public class VerifyBulkJob extends CliCommand<VerifyBulkJobResult> {

    private String bucketName;
    private String prefix;

    public VerifyBulkJob() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        if (Guard.isStringNullOrEmpty(this.bucketName)) {
            throw new MissingOptionException("The verify get command requires '-b' to be set.");
        }
        if (!Guard.isStringNullOrEmpty(args.getObjectName())) {
            System.out.println("Warning: '-o' is not used with verify and is ignored.");
        }
        this.prefix = args.getPrefix();
        // for java SDK7 unit tests
        if (this.prefix == null) {
            this.prefix = "";
        }

        return this;
    }

    @Override
    public VerifyBulkJobResult call() throws Exception {
        try{
            // Generate the list of Ds3Objects to verify
            final Ds3ClientHelpers helper = getClientHelpers();
            final Iterable<Contents> bulkContents = helper.listObjects(this.bucketName, this.prefix);
            if (bulkContents == null || !bulkContents.iterator().hasNext()) {
                throw new CommandException("No objects in bucket '" + this.bucketName + "' with prefix '" + this.prefix + "'");
            }

            // copy Contents list into a Ds3Objects list
            final List<Ds3Object> objectList = new ArrayList<>();
            for (final Contents contents : bulkContents) {
                objectList.add(new Ds3Object(contents.getKey(), contents.getSize()));
            }

            // Make verify call to API
            final VerifyBulkJobSpectraS3Response verifyResponse
                    = getClient().verifyBulkJobSpectraS3(new VerifyBulkJobSpectraS3Request(this.bucketName, objectList ));

            return new VerifyBulkJobResult(this.bucketName, verifyResponse.getMasterObjectListResult().getObjects().iterator());
        } catch (final FailedRequestException e) {
            if (e.getStatusCode() == 500) {
                throw new CommandException("Error: Cannot communicate with the remote DS3 appliance.", e);
            }
            else if (e.getStatusCode() == 404) {
                throw new CommandException("Cannot locate bucket: " + this.bucketName, e);
            }
            else {
                throw new CommandException("Encountered an unknown error of ("+ e.getStatusCode() +") while accessing the remote DS3 appliance.", e);
            }
        }
    }

    @Override
    public View<VerifyBulkJobResult> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.VerifyBulkJobView();
        }
        return new com.spectralogic.ds3cli.views.cli.VerifyBulkJobView();
    }
}
    
