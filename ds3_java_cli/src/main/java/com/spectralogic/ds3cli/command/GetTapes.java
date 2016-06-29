/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.models.GetTapesWithFullDetailsResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.GetTapesWithFullDetailsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetTapesWithFullDetailsSpectraS3Response;
import com.spectralogic.ds3client.networking.FailedRequestException;

public class GetTapes extends CliCommand<GetTapesWithFullDetailsResult> {
    public GetTapes(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    protected final View<GetTapesWithFullDetailsResult> cliView = new com.spectralogic.ds3cli.views.cli.GetTapesWithFullDetailsView();
    protected final View<GetTapesWithFullDetailsResult> jsonView = new com.spectralogic.ds3cli.views.json.GetTapesWithFullDetailsView();

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public GetTapesWithFullDetailsResult call() throws Exception {try {
            final GetTapesWithFullDetailsSpectraS3Response response = getClient().getTapesWithFullDetailsSpectraS3(new GetTapesWithFullDetailsSpectraS3Request());

            return new GetTapesWithFullDetailsResult(response.getNamedDetailedTapeListResult());
        } catch (final FailedRequestException e) {
            throw new CommandException("Failed Get Tapes", e);
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
