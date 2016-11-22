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
import com.spectralogic.ds3cli.models.TapeResult;
import com.spectralogic.ds3cli.views.cli.GetTapeView;
import com.spectralogic.ds3client.commands.spectrads3.GetTapeSpectraS3Request;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.api.ArgumentFactory.ID;

public class GetTape extends BaseCliCommand<TapeResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID);

    private String jobId;

    @Override
    public BaseCliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);

        this.jobId = args.getId();
        return this;
    }

    @Override
    public TapeResult call() throws Exception {
        return new TapeResult(getClient().getTapeSpectraS3(new GetTapeSpectraS3Request(jobId)).getTapeResult());
    }

    @Override
    public View<TapeResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetTapeView();
    }
}
