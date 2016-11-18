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

import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.GetTapeFailureResult;
import com.spectralogic.ds3cli.views.cli.GetTapeFailureView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.GetTapeFailuresSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetTapeFailuresSpectraS3Response;

public class GetTapeFailure extends CliCommand<GetTapeFailureResult> {

    public GetTapeFailure() {
    }

    @Override
    public GetTapeFailureResult call() throws Exception {
        final GetTapeFailuresSpectraS3Response response
                = getClient().getTapeFailuresSpectraS3(new GetTapeFailuresSpectraS3Request());
        return new GetTapeFailureResult( response.getDetailedTapeFailureListResult() );
    }

    @Override
    public View<GetTapeFailureResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetTapeFailureView();
    }
}
