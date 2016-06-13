/*
 * ******************************************************************************
 *   Copyright 2016 Spectra Logic Corporation. All Rights Reserved.
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

import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetDataPoliciesResult;
import com.spectralogic.ds3cli.models.GetUsersResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.utils.SSLSetupException;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;
import java.security.SignatureException;

public class ModifyUser extends CliCommand<GetUsersResult> {

    // name or guid
    private String userId;
    private String defaultPolicyId;
    private ImmutableMap<String, String> modifyParams;


    public ModifyUser(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.userId = args.getId();
        if (this.userId == null) {
            throw new MissingOptionException("The alter policy command requires '-i' to be set with the username or Id");
        }
        this.modifyParams = args.getModifyParams();
        if ((this.modifyParams == null) || (this.modifyParams.isEmpty())) {
            throw new MissingOptionException("The modify_user command requires '--modify-params' to be set with at least one key:value default_data_policy_id:a85aa599-7a58-4141-adbe-79bfd1d42e48,key2:value2");
        }
        return this;
    }

    @Override
    public GetUsersResult call() throws IOException, SignatureException, SSLSetupException, CommandException {
        try {
            // apply changes from metadata
            ModifyUserSpectraS3Request modifyRequest = new ModifyUserSpectraS3Request(this.userId);
            for (String paramChange : this.modifyParams.keySet() ) {
                String paramNewValue = this.modifyParams.get(paramChange);
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
}
