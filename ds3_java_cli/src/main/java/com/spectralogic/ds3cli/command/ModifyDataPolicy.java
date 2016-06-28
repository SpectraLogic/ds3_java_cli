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
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.DataPolicy;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.VersioningLevel;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.SSLSetupException;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Map;

public class ModifyDataPolicy extends CliCommand<GetDataPoliciesResult> {

    // name or guid
    private String policyId;
    private ImmutableMap<String, String> metadata;

    private boolean blobbingEnabled;

    private ChecksumType.Type checksumType;

    private Long defaultBlobSize;

    private Priority defaultGetJobPriority;

    private Priority defaultPutJobPriority;

    private Priority defaultVerifyJobPriority;

    private boolean endToEndCrcRequired;

    private String name;

    private Priority rebuildPriority;

    private VersioningLevel versioning;

    public ModifyDataPolicy(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.policyId = args.getId();
        if (this.policyId == null) {
            throw new MissingOptionException("The modify policy command requires '-i' to be set with the policy name or Id");
        }
        this.metadata = args.getModifyParams();
        if (Guard.isMapNullOrEmpty(this.metadata)) {
            throw new MissingOptionException("The modify_policy command requires '--modify-params' to be set with at least one key:value default_data_policy_id:a85aa599-7a58-4141-adbe-79bfd1d42e48,key2:value2");
        }
        return this;
    }

    @Override
    public GetDataPoliciesResult call() throws IOException, SignatureException, SSLSetupException, CommandException {
        try {
            // get the target policy
            final GetDataPolicySpectraS3Response response = getClient().getDataPolicySpectraS3(new GetDataPolicySpectraS3Request(this.policyId));
            response.checkStatusCode(200);
            DataPolicy policy = response.getDataPolicyResult();

            // update this.policyId to the UUID in case we used name to select it and change the name
            this.policyId = policy.getId().toString();

            // apply changes from metadata
            ModifyDataPolicySpectraS3Request modifyRequest = new ModifyDataPolicySpectraS3Request(this.policyId);
            for (String paramChange : this.metadata.keySet() ) {
                String paramNewValue = this.metadata.get(paramChange);
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

        } catch (final FailedRequestException e) {
            throw new CommandException("Failed Modify Data Policies: " + e.getMessage(), e);
        }
    }
}
