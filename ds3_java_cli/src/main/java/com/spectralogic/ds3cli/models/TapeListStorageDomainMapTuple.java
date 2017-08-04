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

import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3client.models.TapeList;

import java.util.UUID;

public class TapeListStorageDomainMapTuple {
    private final TapeList tapeList;
    private final ImmutableMap<UUID, String> storageDomainIdNameMap;

    TapeListStorageDomainMapTuple(final TapeList tapeList, final ImmutableMap<UUID, String> storageDomainIdNameMap) {
        this.tapeList = tapeList;
        this.storageDomainIdNameMap = storageDomainIdNameMap;
    }

    public TapeList getTapeList() {
        return tapeList;
    }

    public ImmutableMap<UUID, String> getStorageDomainIdNameMap() {
        return storageDomainIdNameMap;
    }
}
