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

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.VerifySystemHealthSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.VerifySystemHealthSpectraS3Response;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class VerifySystemHealth extends CliCommand<DefaultResult> {
    public VerifySystemHealth() {
    }

    @Override
    public DefaultResult call() throws Exception {
        final VerifySystemHealthSpectraS3Response response = getClient().verifySystemHealthSpectraS3(new VerifySystemHealthSpectraS3Request());

        final com.spectralogic.ds3client.models.HealthVerificationResult sysInfo = response.getHealthVerificationResult();
        final String freeSpace = nullGuardToString(sysInfo.getDatabaseFilesystemFreeSpace());
        return new DefaultResult(String.format("Database filesystem free space: %s", freeSpace));
    }
}
