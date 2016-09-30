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
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.VerifyPoolSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.VerifyPoolSpectraS3Response;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;

public class VerifyPool extends CliCommand<DefaultResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ArgumentFactory.ID);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(ArgumentFactory.PRIORITY);

    private String id;
    private Priority priority;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        addOptionalArguments(optionalArgs, args);
        args.parseCommandLine();
        this.viewType = args.getOutputFormat();
        this.priority = args.getPriority();
        this.id = args.getId();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final VerifyPoolSpectraS3Request verifyPoolSpectraS3Request = new VerifyPoolSpectraS3Request(id);

        if (priority != null) {
            verifyPoolSpectraS3Request.withPriority(priority);
        }

        final VerifyPoolSpectraS3Response verifyPoolSpectraS3Response = getClient().verifyPoolSpectraS3(verifyPoolSpectraS3Request);

        return new DefaultResult("Successfully scheduled a verify for pool " + verifyPoolSpectraS3Response.getPoolResult().getName());
    }
}
