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
import com.spectralogic.ds3cli.api.Arguments;
import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.api.ViewType;
import com.spectralogic.ds3cli.api.exceptions.CommandException;
import com.spectralogic.ds3cli.jsonview.DataView;
import com.spectralogic.ds3cli.models.GetUsersResult;
import com.spectralogic.ds3cli.views.cli.GetUsersView;
import com.spectralogic.ds3client.commands.spectrads3.GetUserSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetUserSpectraS3Response;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.Option;

import java.io.IOException;

import static com.spectralogic.ds3cli.api.ArgumentFactory.ID;

public class GetUser extends BaseCliCommand<GetUsersResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID);

    // name or uuid
    private String userId;

    public GetUser() {
    }

    @Override
    public BaseCliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);

        this.userId = args.getId();
        return  this;
    }

    @Override
    public GetUsersResult call() throws CommandException, IOException {
        try {
            final GetUserSpectraS3Response response = getClient().getUserSpectraS3(new GetUserSpectraS3Request(this.userId));

            return new GetUsersResult(response.getSpectraUserResult());
        } catch (final FailedRequestException e) {
            if (e.getStatusCode() == 404) {
                throw new CommandException("Unknown user '" + this.userId +"'", e);
            }
            throw e;
        }
    }

    @Override
    public View<GetUsersResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetUsersView();
    }
}
