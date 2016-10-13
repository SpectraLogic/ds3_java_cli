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
import com.google.common.collect.ImmutableMap;
import static com.spectralogic.ds3cli.ArgumentFactory.*;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetUsersResult;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;

import java.io.IOException;

public class ModifyUser extends CliCommand<GetUsersResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID, MODIFY_PARAMS);

    // name or uuid
    private String userId;
    private ImmutableMap<String, String> modifyParams;

    public ModifyUser() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        args.parseCommandLine();

        this.userId = args.getId();
        this.viewType = args.getOutputFormat();
        this.modifyParams = args.getModifyParams();
        return this;
    }

    @Override
    public GetUsersResult call() throws IOException, CommandException {
        try {
            // apply changes from metadata
            final ModifyUserSpectraS3Request modifyRequest = new ModifyUserSpectraS3Request(this.userId);
            for (final String paramChange : this.modifyParams.keySet()) {
                final String paramNewValue = this.modifyParams.get(paramChange);
                if("default_data_policy_id".equalsIgnoreCase(paramChange)) {
                    modifyRequest.withDefaultDataPolicyId(paramNewValue);
                }
                // currently internal only
                else if("name".equalsIgnoreCase(paramChange)) {
                    throw new CommandException("Modify user name currently not supported." );

                }
                else {
                    throw new CommandException("Unrecognized user parameter: " + paramChange);
                }
            } // for

            // Apply changes
            final ModifyUserSpectraS3Response modifyResponse = getClient().modifyUserSpectraS3(modifyRequest);

            return new GetUsersResult(modifyResponse.getSpectraUserResult());

        } catch (final FailedRequestException e) {
            throw new CommandException("Failed Modify User: " + e.getMessage(), e);
        }
    }

    @Override
    public View<GetUsersResult> getView() {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetUsersView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetUsersView();
    }
}
