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
import com.spectralogic.ds3cli.models.GetDataPathBackendResult;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPathBackendSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPathBackendSpectraS3Response;
import com.spectralogic.ds3client.models.DataPathBackend;
import com.spectralogic.ds3client.networking.FailedRequestException;

import java.io.IOException;

public class GetDataPathBackend extends CliCommand<GetDataPathBackendResult> {

    public GetDataPathBackend() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public GetDataPathBackendResult call() throws IOException, CommandException {
        try {
            final GetDataPathBackendSpectraS3Response response
                    = getClient().getDataPathBackendSpectraS3(new GetDataPathBackendSpectraS3Request());
            final DataPathBackend dataPathBackend = response.getDataPathBackendResult();
            return new GetDataPathBackendResult(dataPathBackend);
        } catch (final FailedRequestException e) {
            throw new CommandException("Failed Get Data Policies", e);
        }
    }

    @Override
    public View<GetDataPathBackendResult> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetDataPathBackendView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetDataPathBackendView();
    }
}

