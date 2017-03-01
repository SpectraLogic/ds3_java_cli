/*
 * *****************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetPoolsResult;
import com.spectralogic.ds3cli.views.cli.GetPoolsView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.CancelVerifyPoolSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.CancelVerifyPoolSpectraS3Response;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.ArgumentFactory.ID;


public class CancelVerifyPool extends CliCommand<GetPoolsResult> {

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
    public GetPoolsResult call() throws Exception {
        try {
            final CancelVerifyPoolSpectraS3Response response
                    = getClient().cancelVerifyPoolSpectraS3(new CancelVerifyPoolSpectraS3Request(this.id));

        return new GetPoolsResult(response.getPoolResult());
        } catch (final FailedRequestException e) {
            if (e.getStatusCode() == 409) {
                throw new CommandException("Conflict (409) verify " + id + " cannot be cancelled.", e);
            }
            throw e;
        }
    }

    @Override
    public View<GetPoolsResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetPoolsView();
    }

}
