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

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.api.Arguments;
import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.api.ViewType;
import com.spectralogic.ds3cli.jsonview.DataView;
import com.spectralogic.ds3cli.models.VerifyTapeResult;
import com.spectralogic.ds3cli.views.cli.VerifyTapeView;
import com.spectralogic.ds3client.commands.spectrads3.VerifyTapeSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.VerifyTapeSpectraS3Response;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.Tape;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.api.ArgumentFactory.ID;
import static com.spectralogic.ds3cli.api.ArgumentFactory.PRIORITY;


public class VerifyTape extends BaseCliCommand<VerifyTapeResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(PRIORITY);

    private String id;
    private Priority priority;

    @Override
    public BaseCliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.priority = args.getPriority();
        this.id = args.getId();
        return this;
    }

    @Override
    public VerifyTapeResult call() throws Exception {
        final VerifyTapeSpectraS3Request request = new VerifyTapeSpectraS3Request(id);

        if (priority != null) {
            request.withTaskPriority(priority);
        }

        final VerifyTapeSpectraS3Response verifyTapeSpectraS3Response = getClient().verifyTapeSpectraS3(request);

        final Tape tapeResult = verifyTapeSpectraS3Response.getTapeResult();

        return new VerifyTapeResult(tapeResult);
    }

    @Override
    public View<VerifyTapeResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        } else {
            return new VerifyTapeView();
        }
    }
}
