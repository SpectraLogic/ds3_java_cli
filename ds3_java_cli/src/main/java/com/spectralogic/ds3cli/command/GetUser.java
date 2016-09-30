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
import com.spectralogic.ds3cli.ArgumentFactory;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetUsersResult;
import com.spectralogic.ds3client.commands.spectrads3.GetUserSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetUserSpectraS3Response;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;

import java.io.IOException;

public class GetUser extends CliCommand<GetUsersResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ArgumentFactory.ID);

    // name or uuid
    private String userId;

    public GetUser() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        args.parseCommandLine();

        this.userId = args.getId();
        this.viewType = args.getOutputFormat();
        return  this;
    }

    @Override
    public GetUsersResult call() throws IOException, CommandException {
        try {
            final GetUserSpectraS3Response response = getClient().getUserSpectraS3(new GetUserSpectraS3Request(this.userId));

            return new GetUsersResult(response.getSpectraUserResult());
        } catch (final FailedRequestException e) {
            if (e.getStatusCode() == 500) {
                throw new CommandException("Error: Cannot communicate with the remote DS3 appliance.", e);
            } else if (e.getStatusCode() == 404) {
                throw new CommandException("Unknown user: " + this.userId, e);
            } else {
                throw new CommandException("Encountered an unknown error of (" + e.getStatusCode() + ") while accessing the remote DS3 appliance.", e);
            }
        }
    }

    @Override
    public View<GetUsersResult> getView() {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetUsersView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetUsersView();
    }
}
