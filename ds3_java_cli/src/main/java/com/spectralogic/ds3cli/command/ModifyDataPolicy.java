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
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetDataPoliciesResult;
import com.spectralogic.ds3cli.views.cli.GetDataPoliciesView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPolicySpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPolicySpectraS3Response;
import com.spectralogic.ds3client.commands.spectrads3.ModifyDataPolicySpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.ModifyDataPolicySpectraS3Response;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.DataPolicy;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.VersioningLevel;
import org.apache.commons.cli.Option;

import java.io.IOException;

import static com.spectralogic.ds3cli.ArgumentFactory.ID;
import static com.spectralogic.ds3cli.ArgumentFactory.MODIFY_PARAMS;

public class ModifyDataPolicy extends CliCommand<GetDataPoliciesResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID, MODIFY_PARAMS);

    // name or uuid
    private String policyId;
    private ImmutableMap<String, String> policyParams;

    public ModifyDataPolicy() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);

        this.policyId = args.getId();
        this.policyParams = args.getModifyParams();
        return this;
    }

    @Override
    public GetDataPoliciesResult call() throws IOException, CommandException {
        // get the target policy
        final GetDataPolicySpectraS3Response response = getClient().getDataPolicySpectraS3(new GetDataPolicySpectraS3Request(this.policyId));
        // response.checkStatusCode(200);
        final DataPolicy policy = response.getDataPolicyResult();

        // update this.policyId to the UUID in case we used name to select it and change the name
        this.policyId = policy.getId().toString();

        // apply changes from metadata
        final ModifyDataPolicySpectraS3Request modifyRequest = new ModifyDataPolicySpectraS3Request(this.policyId);
        for (final String paramChange : this.policyParams.keySet() ) {
            final String paramNewValue = this.policyParams.get(paramChange);
            if("blobbing_enabled".equalsIgnoreCase(paramChange)) {
                modifyRequest.withBlobbingEnabled(Boolean.parseBoolean(paramNewValue));
            }
            else if("name".equalsIgnoreCase(paramChange)) {
                modifyRequest.withName(paramNewValue);
            }
            else if("checksum_type".equalsIgnoreCase(paramChange)) {
                modifyRequest.withChecksumType(ChecksumType.Type.valueOf(paramNewValue));
            }
            else if("default_blob_size".equalsIgnoreCase(paramChange)) {
                modifyRequest.withDefaultBlobSize(Long.getLong(paramNewValue));
            }
            else if("default_get_job_priority".equalsIgnoreCase(paramChange)) {
                modifyRequest.withDefaultGetJobPriority(Priority.valueOf(paramNewValue));
            }
            else if("default_put_job_priority".equalsIgnoreCase(paramChange)) {
                modifyRequest.withDefaultPutJobPriority(Priority.valueOf(paramNewValue));
            }
            else if("default_verify_job_priority".equalsIgnoreCase(paramChange)) {
                modifyRequest.withDefaultVerifyJobPriority(Priority.valueOf(paramNewValue));
            }
            else if("rebuild_priority".equalsIgnoreCase(paramChange)) {
                modifyRequest.withRebuildPriority(Priority.valueOf(paramNewValue));
            }
            else if("end_to_end_crc_required".equalsIgnoreCase(paramChange)) {
                modifyRequest.withEndToEndCrcRequired(Boolean.parseBoolean(paramNewValue));
            }
            else if("versioning".equalsIgnoreCase(paramChange)) {
                modifyRequest.withVersioning(VersioningLevel.valueOf(paramNewValue));
            }
            else {
                throw new CommandException("Unrecognized Data Policy parameter: " + paramChange);
            }
        } // for

        // Apply changes
        final ModifyDataPolicySpectraS3Response modifyResponse = getClient().modifyDataPolicySpectraS3(modifyRequest);
        return new GetDataPoliciesResult(modifyResponse.getDataPolicyResult());
    }

    @Override
    public View<GetDataPoliciesResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetDataPoliciesView();
    }
}
