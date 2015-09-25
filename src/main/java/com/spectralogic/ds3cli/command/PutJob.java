/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.models.PutJobResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.ModifyJobRequest;
import com.spectralogic.ds3client.models.bulk.Priority;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;
import java.util.UUID;

public class PutJob extends CliCommand<PutJobResult> {

    private UUID jobId;
    private Priority priority;

    public PutJob(final Ds3Provider provider, final FileUtils fileUtils) { super(provider, fileUtils); }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        if (args.getId() == null) {
            throw new MissingOptionException("The put job command requires '-i' to be set.");
        }
        jobId = UUID.fromString(args.getId());
        priority = args.getPriority();
        return this;
    }

    @Override
    public PutJobResult call() throws Exception {
        try {
            final ModifyJobRequest request = new ModifyJobRequest(jobId);
            if (priority != null) {
                request.withPriority(priority);
            }
            getClient().modifyJob(request);
        }
        catch (IOException e) {
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }
        String result = "Success: Modified job with job id '" + jobId.toString() + "'";
        if (priority != null) {
            result = result.concat(" with priority " + priority.toString());
        }
        return new PutJobResult(result + ".");
    }
}
