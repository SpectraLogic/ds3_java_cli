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
import com.spectralogic.ds3client.commands.spectrads3.VerifyAllPoolsSpectraS3Request;
import com.spectralogic.ds3client.models.Priority;

public class VerifyAllPools extends CliCommand<DefaultResult> {
    private Priority priority;

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
