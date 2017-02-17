/*
 * ******************************************************************************
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
 * ****************************************************************************
 */

package com.spectralogic.ds3cli.certification;

import com.spectralogic.ds3client.commands.spectrads3.ModifyCacheFilesystemSpectraS3Request;

public class CertificationModifyCacheFilesystemSpectraS3Request extends ModifyCacheFilesystemSpectraS3Request{

    public CertificationModifyCacheFilesystemSpectraS3Request(final String cacheFilesystem) {
        super(cacheFilesystem);
    }

    // Ds3 JavaSDK AbstractRequest::updateQueryParam() doesn't allow for null / empty value, so this is a workaround.
    public ModifyCacheFilesystemSpectraS3Request withUnsetMaxCapacityInBytes() {
        this.updateQueryParam("max_capacity_in_bytes", "");
        return this;
    }
}
