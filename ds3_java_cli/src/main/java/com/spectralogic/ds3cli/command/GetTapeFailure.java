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
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.GetTapeFailuresSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetTapeFailuresSpectraS3Response;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3cli.models.GetTapeFailureResult;

public class GetTapeFailure extends CliCommand<GetTapeFailureResult> {

    protected final View<GetTapeFailureResult> cliView = new com.spectralogic.ds3cli.views.cli.GetTapeFailureView();
    protected final View<GetTapeFailureResult> jsonView = new com.spectralogic.ds3cli.views.json.GetTapeFailureView();

    public GetTapeFailure(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public GetTapeFailureResult call() throws Exception {

        try {
            final GetTapeFailuresSpectraS3Response response = getClient().getTapeFailuresSpectraS3(new GetTapeFailuresSpectraS3Request());

            return new GetTapeFailureResult( response.getDetailedTapeFailureListResult() );
        }
        catch(final FailedRequestException e) {
            if(e.getStatusCode() == 500) {
                throw new CommandException("Error: Cannot communicate with the remote DS3 appliance.", e);
            }
            else {
                throw new CommandException("Error: Encountered an unknown error of ("+ e.getStatusCode() +") while accessing the remote DS3 appliance.", e);
            }
        }
    }

    @Override
    public View getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return this.jsonView;
        }
        return this.cliView;
    }
}
