/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.GetTapeResult;
import com.spectralogic.ds3cli.views.cli.GetTapeView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.CancelVerifyTapeSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.CancelVerifyTapeSpectraS3Response;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.ArgumentFactory.*;


public class CancelVerifyTape extends CliCommand<GetTapeResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID);

    private String id;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);
        args.parseCommandLine();
        this.id = args.getId();
        this.viewType = args.getOutputFormat();
        return this;
    }

    @Override
    public GetTapeResult call() throws Exception {
        final CancelVerifyTapeSpectraS3Response response
                = getClient().cancelVerifyTapeSpectraS3(new CancelVerifyTapeSpectraS3Request(this.id));
        return new GetTapeResult(response.getTapeResult());
    }

    @Override
    public View<GetTapeResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetTapeView();
    }

}
