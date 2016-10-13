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
import static com.spectralogic.ds3cli.ArgumentFactory.*;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.CancelJobSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.TruncateActiveJobSpectraS3Request;
import org.apache.commons.cli.Option;

import java.util.UUID;

public class DeleteJob extends CliCommand<DefaultResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(FORCE);

    private UUID id;
    private boolean force;

    public DeleteJob() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        addOptionalArguments(optionalArgs, args);
        args.parseCommandLine();

        this.id = UUID.fromString(args.getId());
        this.force = args.isForce();
        this.viewType = args.getOutputFormat();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        if (this.force) {
            final CancelJobSpectraS3Request request = new CancelJobSpectraS3Request(id);
            getClient().cancelJobSpectraS3(request);
            return new DefaultResult("SUCCESS: Forcibly canceled job '" + this.id.toString() + "'");
        } else {
            getClient().truncateActiveJobSpectraS3(new TruncateActiveJobSpectraS3Request(id));
            return new DefaultResult("SUCCESS: Canceled job '" + this.id.toString() + "'");
        }
    }
}
