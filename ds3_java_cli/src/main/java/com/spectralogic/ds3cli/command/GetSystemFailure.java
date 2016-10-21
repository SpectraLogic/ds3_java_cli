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
import com.spectralogic.ds3cli.models.GetSystemFailureResult;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemFailuresSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemFailuresSpectraS3Response;
import com.spectralogic.ds3client.models.SystemFailureList;

public class GetSystemFailure extends CliCommand<GetSystemFailureResult> {

    public GetSystemFailure() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public GetSystemFailureResult call() throws Exception {
        final GetSystemFailuresSpectraS3Response sysFailuresResponse
                = getClient().getSystemFailuresSpectraS3(new GetSystemFailuresSpectraS3Request());
        final SystemFailureList sysFailures = sysFailuresResponse.getSystemFailureListResult();
        return new GetSystemFailureResult( sysFailures );
    }

    @Override
    public View<GetSystemFailureResult> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetSystemFailureView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetSystemFailureView();
    }
}
