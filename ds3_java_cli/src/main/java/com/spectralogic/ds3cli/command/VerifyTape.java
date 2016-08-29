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
import com.spectralogic.ds3cli.models.VerifyTapeResult;
import com.spectralogic.ds3client.commands.spectrads3.VerifyTapeSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.VerifyTapeSpectraS3Response;
import com.spectralogic.ds3client.models.Tape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VerifyTape extends CliCommand<VerifyTapeResult> {

    private String id;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.id = args.getId();
        return this;
    }

    @Override
    public VerifyTapeResult call() throws Exception {
        final VerifyTapeSpectraS3Response verifyTapeSpectraS3Response = getClient().verifyTapeSpectraS3(new VerifyTapeSpectraS3Request(id));

        final Tape tapeResult = verifyTapeSpectraS3Response.getTapeResult();

        return new VerifyTapeResult(tapeResult);
    }

    @Override
    public View<VerifyTapeResult> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.VerifyTapeView();
        } else {
            return new com.spectralogic.ds3cli.views.cli.VerifyTapeView();
        }
    }
}
