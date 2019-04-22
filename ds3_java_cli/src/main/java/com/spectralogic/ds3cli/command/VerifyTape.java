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
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetTapeResult;
import com.spectralogic.ds3cli.views.cli.GetTapeView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.VerifyTapeSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.VerifyTapeSpectraS3Response;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.ArgumentFactory.ID;
import static com.spectralogic.ds3cli.ArgumentFactory.PRIORITY;


public class VerifyTape extends CliCommand<GetTapeResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(PRIORITY);

    private String id;
    private Priority priority;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.priority = args.getPriority();
        this.id = args.getId();
        return this;
    }

    @Override
    public GetTapeResult call() throws Exception {
        try {
            final VerifyTapeSpectraS3Request request = new VerifyTapeSpectraS3Request(id);
            if (priority != null) {
                request.withTaskPriority(priority);
            }

            final VerifyTapeSpectraS3Response verifyTapeSpectraS3Response = getClient().verifyTapeSpectraS3(request);
            return new GetTapeResult(verifyTapeSpectraS3Response.getTapeResult());
        } catch(final FailedRequestException e) {
            if (e.getStatusCode() == 409) {
                throw new CommandException("Conflict (409) tape " + id + " cannot be verified.", e);
            }
            throw e;
        }
    }

    @Override
    public View<GetTapeResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        } else {
            return new GetTapeView();
        }
    }
}
