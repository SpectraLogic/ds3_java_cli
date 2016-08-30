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
import com.spectralogic.ds3cli.models.VerifyAllTapesResult;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.VerifyAllTapesSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.VerifyAllTapesSpectraS3Response;
import com.spectralogic.ds3client.models.Priority;

public class VerifyAllTapes extends CliCommand<VerifyAllTapesResult> {

    private Priority priority;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.priority = args.getPriority();
        return this;
    }

    @Override
    public VerifyAllTapesResult call() throws Exception {
        final Ds3Client client = getClient();

        final VerifyAllTapesSpectraS3Request request = new VerifyAllTapesSpectraS3Request();
        if (priority != null) {
            request.withTaskPriority(priority);
        }

        final VerifyAllTapesSpectraS3Response verifyResponse = client.verifyAllTapesSpectraS3(request);

        return new VerifyAllTapesResult(verifyResponse.getTapeFailureListResult());
    }

    @Override
    public View<VerifyAllTapesResult> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.VerifyAllTapesView();
        }
        return new com.spectralogic.ds3cli.views.cli.VerifyAllTapesView();
    }
}
