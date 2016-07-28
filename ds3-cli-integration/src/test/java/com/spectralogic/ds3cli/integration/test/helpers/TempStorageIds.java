/*
 * ******************************************************************************
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
 * ****************************************************************************
 */

package com.spectralogic.ds3cli.integration.test.helpers;

import java.util.UUID;

/**
 * Used to store the UUID of the storage domain member and the
 * data persistence rule when setting up the testing environment.
 * These IDs are stored for teardown of testing environment.
 */
public class TempStorageIds {
    private UUID storageDomainMemberId;
    private UUID dataPersistenceRuleId;

    public TempStorageIds(final UUID storageDomainMemberId, final UUID dataPersistenceRuleId) {
        this.storageDomainMemberId = storageDomainMemberId;
        this.dataPersistenceRuleId = dataPersistenceRuleId;
    }

    public UUID getStorageDomainMemberId() {
        return storageDomainMemberId;
    }

    public UUID getDataPersistenceRuleId() {
        return dataPersistenceRuleId;
    }
}

