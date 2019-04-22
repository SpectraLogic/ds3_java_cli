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
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.VerifyAllPoolsSpectraS3Request;
import com.spectralogic.ds3client.models.Priority;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.ArgumentFactory.PRIORITY;

public class VerifyAllPools extends CliCommand<DefaultResult> {

    private final static ImmutableList<Option> OPTIONAL_ARGS = ImmutableList.of(PRIORITY);

    private Priority priority;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(EMPTY_LIST, OPTIONAL_ARGS, args);

        this.priority = args.getPriority();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {

        final VerifyAllPoolsSpectraS3Request request = new VerifyAllPoolsSpectraS3Request();
        if (priority != null) {
            request.withPriority(priority);
        }

        getClient().verifyAllPoolsSpectraS3(request);

        return new DefaultResult("Verify tasks have been scheduled for all pools");
    }
}
