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

package com.spectralogic.ds3cli.models;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3client.models.StorageDomain;
import com.spectralogic.ds3client.models.StorageDomainList;

public class GetStorageDomainsResult implements Result<StorageDomainList> {

    private final StorageDomainList result;

    // instantiate with single domain from GetStorageDomainSpectraS3Response
    public GetStorageDomainsResult(final StorageDomain result) {
        this.result = new StorageDomainList();
        this.result.setStorageDomains(ImmutableList.of(result));
    }

    // instantiate with domain list from GetStorageDomainsSpectraS3Response
    public GetStorageDomainsResult(final StorageDomainList result) {
        this.result = result;
    }

    @Override
    public StorageDomainList getResult() {
        return this.result;
    }
}
