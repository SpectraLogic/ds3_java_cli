/*
 * ******************************************************************************
 *   Copyright 2016 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetDataPoliciesResult;
import com.spectralogic.ds3cli.models.GetUsersResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPoliciesSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPoliciesSpectraS3Response;
import com.spectralogic.ds3client.commands.spectrads3.GetUsersSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetUsersSpectraS3Response;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.utils.SSLSetupException;

import java.io.IOException;
import java.security.SignatureException;

public class GetUsers extends CliCommand<GetUsersResult> {

    public GetUsers(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public GetUsersResult call() throws IOException, SignatureException, SSLSetupException, CommandException {
        try {
            final GetUsersSpectraS3Response response = getClient().getUsersSpectraS3(new GetUsersSpectraS3Request());

            return new GetUsersResult(response.getSpectraUserListResult());
        } catch (final FailedRequestException e) {
            throw new CommandException("Failed to Get Users", e);
        }
    }
}
