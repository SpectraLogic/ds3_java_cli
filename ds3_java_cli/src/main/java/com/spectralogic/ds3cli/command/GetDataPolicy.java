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
import com.spectralogic.ds3cli.api.Arguments;
import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.api.ViewType;
import com.spectralogic.ds3cli.api.exceptions.CommandException;
import com.spectralogic.ds3cli.jsonview.DataView;
import com.spectralogic.ds3cli.models.GetDataPoliciesResult;
import com.spectralogic.ds3cli.views.cli.GetDataPoliciesView;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPolicySpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetDataPolicySpectraS3Response;
import org.apache.commons.cli.Option;

import java.io.IOException;

import static com.spectralogic.ds3cli.api.ArgumentFactory.ID;

public class GetDataPolicy extends BaseCliCommand<GetDataPoliciesResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID);

    // name or uuid
    private String policyId;

    public GetDataPolicy() {
    }

    @Override
    public BaseCliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        args.parseCommandLine();

        this.policyId = args.getId();
        this.viewType = args.getOutputFormat();
        return this;
    }

    @Override
    public GetDataPoliciesResult call() throws IOException, CommandException {
        final GetDataPolicySpectraS3Response response = getClient().getDataPolicySpectraS3(new GetDataPolicySpectraS3Request(this.policyId));
        return new GetDataPoliciesResult(response.getDataPolicyResult());
    }

    @Override
    public View<GetDataPoliciesResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetDataPoliciesView();
    }
}
