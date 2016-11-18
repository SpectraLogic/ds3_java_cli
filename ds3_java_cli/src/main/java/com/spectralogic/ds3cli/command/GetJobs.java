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
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.GetJobsResult;
import com.spectralogic.ds3cli.views.cli.GetJobsView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.GetJobsSpectraS3Request;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.ArgumentFactory.COMPLETED;

public class GetJobs extends CliCommand<GetJobsResult> {

    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(COMPLETED);

    private boolean completed;

    public GetJobs() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(EMPTY_LIST, optionalArgs, args);

        this.completed = args.isCompleted();
        return this;
    }

    @Override
    public GetJobsResult call() throws Exception {
        final GetJobsSpectraS3Request request = new GetJobsSpectraS3Request();
        if(completed) {
            request.withFullDetails(completed);
        }

        return new GetJobsResult(getClient().getJobsSpectraS3(request).getJobListResult());
    }

     @Override
    public View<GetJobsResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetJobsView();
    }
}
