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

package com.spectralogic.ds3cli.models;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3client.models.Pool;
import com.spectralogic.ds3client.models.PoolList;

public class GetPoolsResult implements Result<PoolList> {
    private final PoolList poolList;

    // constructor from PoolList for GetPools
    public GetPoolsResult(final PoolList poolList) {
        this.poolList = poolList;
    }

    // constructor for Pool for GetPool
    public GetPoolsResult(final Pool pool) {
        this.poolList = new PoolList();
        this.poolList.setPools(ImmutableList.of(pool));
    }

    @Override
    public PoolList getResult() {
        return poolList;
    }
}
