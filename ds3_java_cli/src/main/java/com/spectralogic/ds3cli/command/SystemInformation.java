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

import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Response;
import com.spectralogic.ds3client.models.BuildInformation;

public class SystemInformation extends CliCommand<DefaultResult> {
    public SystemInformation() {
    }

    @Override
    public DefaultResult call() throws Exception {
        final GetSystemInformationSpectraS3Response response = getClient().getSystemInformationSpectraS3(new GetSystemInformationSpectraS3Request());

        final com.spectralogic.ds3client.models.SystemInformation sysInfo = response.getSystemInformationResult();
        final BuildInformation buildInfo = sysInfo.getBuildInformation();
        return new DefaultResult(String.format("Build Number: %s.%s, API Version: %s, Serial Number: %s", buildInfo.getVersion(), buildInfo.getRevision(), sysInfo.getApiVersion(), sysInfo.getSerialNumber()));
    }
}
