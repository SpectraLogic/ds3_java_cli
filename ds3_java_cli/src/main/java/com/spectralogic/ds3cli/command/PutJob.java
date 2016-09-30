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
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.ModifyJobSpectraS3Request;
import com.spectralogic.ds3client.models.Priority;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.util.UUID;

public class PutJob extends CliCommand<DefaultResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ArgumentFactory.ID);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(ArgumentFactory.PRIORITY);

    private UUID jobId;
    private Priority priority;

    public PutJob() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        addOptionalArguments(optionalArgs, args);
        args.parseCommandLine();

        this.jobId = UUID.fromString(args.getId());
        this.priority = args.getPriority();
        this.viewType = args.getOutputFormat();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        try {
            final ModifyJobSpectraS3Request request = new ModifyJobSpectraS3Request(jobId);
            if (priority != null) {
                request.withPriority(priority);
            }
            getClient().modifyJobSpectraS3(request);
        }
        catch (final IOException e) {
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }
        String result = "Success: Modified job with job id '" + jobId.toString() + "'";
        if (priority != null) {
            result = result.concat(" with priority " + priority.toString());
        }
        return new DefaultResult(result + ".");
    }
}
