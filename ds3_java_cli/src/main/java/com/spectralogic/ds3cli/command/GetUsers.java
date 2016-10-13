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
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetUsersResult;
import com.spectralogic.ds3client.commands.spectrads3.GetUsersSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetUsersSpectraS3Response;
import com.spectralogic.ds3client.networking.FailedRequestException;

import java.io.IOException;

public class GetUsers extends CliCommand<GetUsersResult> {

    public GetUsers() {
    }


    @Override
    public GetUsersResult call() throws IOException, CommandException {
        try {
            final GetUsersSpectraS3Response response = getClient().getUsersSpectraS3(new GetUsersSpectraS3Request());

            return new GetUsersResult(response.getSpectraUserListResult());
        } catch (final FailedRequestException e) {
            throw new CommandException("Failed to Get Users", e);
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
