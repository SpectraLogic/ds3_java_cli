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
import com.spectralogic.ds3cli.ArgumentFactory;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3client.commands.spectrads3.GetJobSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetJobSpectraS3Response;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;

import java.util.UUID;

public class GetJob extends CliCommand<GetJobResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ArgumentFactory.ID);

    private UUID jobId;

    public GetJob() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        args.parseCommandLine();

        this.jobId = UUID.fromString(args.getId());
        this.viewType = args.getOutputFormat();
        return this;
    }

    @Override
    public GetJobResult call() throws Exception {
        final GetJobSpectraS3Response response = getClient().getJobSpectraS3(new GetJobSpectraS3Request(this.jobId));

        return new GetJobResult(response.getMasterObjectListResult());
    }

    @Override
    public View<GetJobResult> getView() {
        if (this.viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetJobView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetJobView();
    }
}
    
