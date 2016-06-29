/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3cli.models.GetJobsResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.GetJobsSpectraS3Request;

public class GetJobs extends CliCommand<GetJobsResult> {

    private boolean completed;

    protected com.spectralogic.ds3cli.View<GetJobsResult> cliView = new com.spectralogic.ds3cli.views.cli.GetJobsView();
    protected com.spectralogic.ds3cli.View<GetJobsResult> jsonView = new com.spectralogic.ds3cli.views.json.GetJobsView();

    public GetJobs(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
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
    public View getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return this.jsonView;
        }
        return this.cliView;
    }
}
