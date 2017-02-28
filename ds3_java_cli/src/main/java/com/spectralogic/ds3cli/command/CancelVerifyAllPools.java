/*
 * *****************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3client.commands.spectrads3.CancelVerifyOnAllPoolsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.CancelVerifyOnAllPoolsSpectraS3Response;

public class CancelVerifyAllPools extends CliCommand<DefaultResult> {

    @Override
    public DefaultResult call() throws Exception {
        final CancelVerifyOnAllPoolsSpectraS3Response response
                = getClient().cancelVerifyOnAllPoolsSpectraS3(new CancelVerifyOnAllPoolsSpectraS3Request());
        return new DefaultResult("Verify Pool cancelled on all pools.");
    }
}
