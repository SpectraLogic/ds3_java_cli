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
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3cli.views.cli.GetJobView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;

import java.io.IOException;

import static com.spectralogic.ds3cli.ArgumentFactory.ID;
import static com.spectralogic.ds3cli.ArgumentFactory.PRIORITY;

public class ModifyJob extends CliCommand<GetJobResult> {

    public static final Option JOB_NAME = Option.builder()
            .longOpt("job-name")
            .hasArg(true).argName("jobName")
            .desc("The name to be assigned to the active job").build();

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(PRIORITY, JOB_NAME);

    // name or uuid
    private String jobId;
    private Priority jobPriority;
    private String jobName;

    public ModifyJob() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.jobId = args.getId();
        this.jobPriority = args.getPriority();
        this.jobName = args.getOptionValue(JOB_NAME.getLongOpt());

        return this;
    }

    @Override
    public GetJobResult call() throws IOException, CommandException {
        final ModifyJobSpectraS3Request request = new ModifyJobSpectraS3Request(this.jobId);
        if (!Guard.isStringNullOrEmpty(jobName)) {
            request.withName(jobName);
        }
        if (jobPriority != null) {
            request.withPriority(jobPriority);
        }
        final ModifyJobSpectraS3Response response = getClient().modifyJobSpectraS3(request);

        return new GetJobResult(response.getMasterObjectListResult());
    }

    @Override
    public View<GetJobResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetJobView();
    }
}
