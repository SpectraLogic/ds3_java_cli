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

package com.spectralogic.ds3cli.helpers;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.models.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import javax.annotation.Nullable;

import static com.spectralogic.ds3cli.helpers.ABMTestHelper.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

/**
 * This is a testing utility designed for creating a temporary data policy, storage domain,
 * and partition for use in the integration tests to avoid error if the BP does not currently
 * have a partition available for running the unit tests.
 */
public class TempStorageUtil {

    private static final String DATA_POLICY_NAME = "_dp";
    private static final String STORAGE_DOMAIN_NAME = "_sd";
    private static final String POOL_PARTITION_NAME = "_pp";
    private static final String PREFERRED_DATA_POLICY_NAME = "Single Copy on Tape";

    /**
     * Sets up a temporary data policy with a temporary storage domain and partition
     * for use in integration tests where the user may not currently have access to
     * a partition.
     * @param testSetName The unique name of tests being run under this domain to
     *                    prevent race conditions between test setup and teardown
     */
    public static TempStorageIds setup(
            final String testSetName,
            final UUID dataPolicyId,
            final Ds3Client client) throws IOException {
        //Create storage domain
        final PutStorageDomainSpectraS3Response storageDomainResponse = createStorageDomain(
                testSetName + STORAGE_DOMAIN_NAME,
                client);

        //Create pool partition
        final PutPoolPartitionSpectraS3Response poolPartitionResponse = createPoolPartition(
                testSetName + POOL_PARTITION_NAME,
                PoolType.ONLINE,
                client);

        //Create storage domain member linking pool partition to storage domain
        final PutPoolStorageDomainMemberSpectraS3Response memberResponse = createPoolStorageDomainMember(
                storageDomainResponse.getStorageDomainResult().getId(),
                poolPartitionResponse.getPoolPartitionResult().getId(),
                client);
        final UUID storageDomainMemberId = memberResponse.getStorageDomainMemberResult().getId();

        //create data persistence rule
        final PutDataPersistenceRuleSpectraS3Response dataPersistenceResponse = createDataPersistenceRule(
                dataPolicyId,
                storageDomainResponse.getStorageDomainResult().getId(),
                client);
        final UUID dataPersistenceRuleId = dataPersistenceResponse.getDataPersistenceRuleResult().getDataPolicyId();

        return new TempStorageIds(storageDomainMemberId, dataPersistenceRuleId);
    }

    /**
     * Tears down the temporary test environment
     */
    public static void teardown(
            final String testSetName,
            final TempStorageIds ids,
            final Ds3Client client) throws IOException {
        deleteDataPersistenceRule(ids.getDataPersistenceRuleId(), client);
        deleteDataPolicy(testSetName + DATA_POLICY_NAME, client);
        deleteStorageDomainMember(ids.getStorageDomainMemberId(), client);
        deleteStorageDomain(testSetName + STORAGE_DOMAIN_NAME, client);
        deletePoolPartition(testSetName + POOL_PARTITION_NAME, client);
    }

    /**
     * Creates a Data Policy with the specified options, then set to the default "spectra" user Data Policy.
     */
    public static UUID setupDataPolicy(
            final String testSetName,
            final boolean withEndToEndCrcRequired,
            final ChecksumType.Type checksumType,
            final VersioningLevel versioningLevel,
            final Ds3Client client) throws IOException {
        final PutDataPolicySpectraS3Response dataPolicyResponse = createDataPolicy(
                testSetName + DATA_POLICY_NAME,
                versioningLevel,
                checksumType,
                withEndToEndCrcRequired,
                false,
                client);
        client.modifyUserSpectraS3(new ModifyUserSpectraS3Request("Administrator")
                .withDefaultDataPolicyId(dataPolicyResponse.getDataPolicyResult().getId()));
        return dataPolicyResponse.getDataPolicyResult().getId();
    }

    /**
     * Verifies that a valid TapePartition is available, then set to the default "spectra" user Data Policy.
     */
    public static UUID verifyAvailableTapePartition(final Ds3Client client) throws IOException {
        final GetTapePartitionsWithFullDetailsSpectraS3Response getTapePartitionsResponse = client.getTapePartitionsWithFullDetailsSpectraS3(new GetTapePartitionsWithFullDetailsSpectraS3Request());
        assumeThat(getTapePartitionsResponse.getNamedDetailedTapePartitionListResult().getNamedDetailedTapePartitions().size(), is(greaterThan(0)));

        final GetStorageDomainMembersSpectraS3Response getStorageDomainMembersResponse = client.getStorageDomainMembersSpectraS3(new GetStorageDomainMembersSpectraS3Request());
        assumeThat(getStorageDomainMembersResponse.getStorageDomainMemberListResult().getStorageDomainMembers().size(), is(greaterThan(0)));

        final GetDataPoliciesSpectraS3Response getDataPoliciesResponse = client.getDataPoliciesSpectraS3(new GetDataPoliciesSpectraS3Request());
        final Iterable<DataPolicy> preferredPolicies =
                Iterables.filter(getDataPoliciesResponse.getDataPolicyListResult().getDataPolicies(), new Predicate<DataPolicy>() {
                @Override
                public boolean apply(@Nullable DataPolicy input) {
                    return input.getName().equals(PREFERRED_DATA_POLICY_NAME);
                };});
        final Iterator<DataPolicy> policies = preferredPolicies.iterator();
        assumeTrue(policies.hasNext());
        final DataPolicy singleTapeDp = policies.next();

        client.modifyUserSpectraS3(new ModifyUserSpectraS3Request("Administrator")
                .withDefaultDataPolicyId(singleTapeDp.getId()));

        return singleTapeDp.getId();
    }
}

