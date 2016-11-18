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
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.VerifyBulkJobResult;
import com.spectralogic.ds3cli.views.cli.VerifyBulkJobView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.VerifyBulkJobSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.VerifyBulkJobSpectraS3Response;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.Option;

import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class VerifyBulkJob extends CliCommand<VerifyBulkJobResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(PREFIX, PRIORITY);

    private String bucketName;
    private String prefix;
    private Priority priority;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.priority = args.getPriority();
        this.bucketName = args.getBucket();
        this.prefix = args.getPrefix();
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
            final VerifyBulkJobSpectraS3Request request = new VerifyBulkJobSpectraS3Request(this.bucketName, objectList);
            if (this.priority != null) {
                request.withPriority(priority);
            }

            final VerifyBulkJobSpectraS3Response verifyResponse
                    = getClient().verifyBulkJobSpectraS3(request);

            return new VerifyBulkJobResult(this.bucketName, verifyResponse.getMasterObjectListResult().getObjects().iterator());
        } catch (final FailedRequestException e) {
            if  (e.getStatusCode() == 404) {
                throw new CommandException("Cannot locate bucket: " + this.bucketName, e);
            }
            throw e;
        }
    }

    @Override
    public View<VerifyBulkJobResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new VerifyBulkJobView();
    }
}
