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
import com.spectralogic.ds3cli.api.Arguments;
import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.api.ViewType;
import com.spectralogic.ds3cli.api.exceptions.CommandException;
import com.spectralogic.ds3cli.jsonview.DataView;
import com.spectralogic.ds3cli.models.GetUsersResult;
import com.spectralogic.ds3cli.views.cli.GetUsersView;
import com.spectralogic.ds3client.commands.spectrads3.ModifyUserSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.ModifyUserSpectraS3Response;
import org.apache.commons.cli.Option;

import java.io.IOException;

import static com.spectralogic.ds3cli.api.ArgumentFactory.ID;
import static com.spectralogic.ds3cli.api.ArgumentFactory.MODIFY_PARAMS;

public class ModifyUser extends BaseCliCommand<GetUsersResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID, MODIFY_PARAMS);

    // name or uuid
    private String userId;
    private ImmutableMap<String, String> modifyParams;

    public ModifyUser() {
    }

    @Override
    public BaseCliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);

        this.userId = args.getId();
        this.modifyParams = args.getModifyParams();
        return this;
    }

    @Override
    public GetUsersResult call() throws IOException, CommandException {
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
    }

    @Override
    public View<GetUsersResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetUsersView();
    }
}
