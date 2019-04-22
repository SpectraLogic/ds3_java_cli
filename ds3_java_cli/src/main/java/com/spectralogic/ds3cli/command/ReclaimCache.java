/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3client.commands.spectrads3.ForceFullCacheReclaimSpectraS3Request;

public class ReclaimCache extends CliCommand<DefaultResult> {
    public ReclaimCache() {
    }

    @Override
    public DefaultResult call() throws Exception {
        this.getClient().forceFullCacheReclaimSpectraS3(new ForceFullCacheReclaimSpectraS3Request());
        return new DefaultResult("Success: Forced Reclaim of Cache");
    }
}
