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
import com.spectralogic.ds3cli.models.SuspectBlobTapesResult;
import com.spectralogic.ds3client.commands.spectrads3.GetSuspectBlobTapesSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetSuspectBlobTapesSpectraS3Response;

public class GetSuspectBlobTapes extends CliCommand<SuspectBlobTapesResult> {
    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public SuspectBlobTapesResult call() throws Exception {
        final GetSuspectBlobTapesSpectraS3Response suspectBlobTapesSpectraS3 = getClient().getSuspectBlobTapesSpectraS3(new GetSuspectBlobTapesSpectraS3Request());
        return new SuspectBlobTapesResult(suspectBlobTapesSpectraS3.getSuspectBlobTapeListResult().getSuspectBlobTapes());
    }

    @Override
    public View<SuspectBlobTapesResult> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetSuspectBlobTapesView();
        } else {
            return new com.spectralogic.ds3cli.views.cli.GetSuspectBlobTapesView();
        }
    }
}
