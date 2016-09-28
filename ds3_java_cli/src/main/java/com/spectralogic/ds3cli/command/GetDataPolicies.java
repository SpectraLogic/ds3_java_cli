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
import com.spectralogic.ds3cli.models.GetDataPoliciesResult;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPoliciesSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPoliciesSpectraS3Response;
import com.spectralogic.ds3client.networking.FailedRequestException;

import java.io.IOException;

public class GetDataPolicies extends CliCommand<GetDataPoliciesResult> {

    public GetDataPolicies() {
    }

    @Override
    public GetDataPoliciesResult call() throws IOException, CommandException {
        try {
            final GetDataPoliciesSpectraS3Response response = getClient().getDataPoliciesSpectraS3(new GetDataPoliciesSpectraS3Request());

            return new GetDataPoliciesResult(response.getDataPolicyListResult());
        } catch (final FailedRequestException e) {
            throw new CommandException("Failed Get Data Policies", e);
        }
    }

    @Override
    public View<GetDataPoliciesResult> getView() {
        if (this.viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetDataPoliciesView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetDataPoliciesView();
    }
}

