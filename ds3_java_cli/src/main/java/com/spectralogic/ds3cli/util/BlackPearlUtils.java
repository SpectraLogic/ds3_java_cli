/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.util;

import com.spectralogic.ds3cli.exceptions.*;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.utils.Guard;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import java.util.UUID;

public final class BlackPearlUtils {

    /**
     * Check System, and all Storage Domains adn TapePools before writing to bucket
     * @param client
     * @param bucketname
     * @throws IOException
     * @throws SignatureException
     * @throws CommandException
     */
    public static void clearToWrite(final Ds3Client client, final String bucketname) throws IOException, SignatureException, CommandException {
        checkBlackPearlForSystemFailure(client);
        // checkBlackPearlForTapeFailure(client);

        // get info about the bucket to query for failures
        // bucket -> data policy -> persistence rules -> storage domain -> partitions -> pools
        final GetBucketSpectraS3Request bucketRequest = new GetBucketSpectraS3Request(bucketname);
        final GetBucketSpectraS3Response bucketResponse = client.getBucketSpectraS3(bucketRequest);
        if ((bucketResponse == null) || (bucketResponse.getBucketResult() == null))  {
            throw new CommandException(String.format("BucketName %s not found on remote appliance", bucketname ));
        }
        final Bucket bucketInfo = bucketResponse.getBucketResult();
        final UUID dataPolicyId = bucketInfo.getDataPolicyId();

        final GetDataPersistenceRulesSpectraS3Request dataPersistenceRulesRequest = new GetDataPersistenceRulesSpectraS3Request().withDataPolicyId(dataPolicyId);
        final GetDataPersistenceRulesSpectraS3Response persistenceRulesResponse = client.getDataPersistenceRulesSpectraS3(dataPersistenceRulesRequest);
        if ((persistenceRulesResponse == null) || (persistenceRulesResponse.getDataPersistenceRuleListResult() == null))   {
            throw new CommandException(String.format("BucketName %s has no Data Persistence Rules on remote appliance", bucketname ));
        }
        final DataPersistenceRuleList rulelist = persistenceRulesResponse.getDataPersistenceRuleListResult();
        final List<DataPersistenceRule> rules = rulelist.getDataPersistenceRules();
        for (DataPersistenceRule rule : rules) {
            checkBlackPearlForStorageDomainFailure(client, rule.getStorageDomainId());
            final GetStorageDomainMembersSpectraS3Response domainMembersResponse = client.getStorageDomainMembersSpectraS3(new GetStorageDomainMembersSpectraS3Request().withStorageDomainId(rule.getStorageDomainId()) );
            if ((domainMembersResponse != null) && (domainMembersResponse.getStorageDomainMemberListResult() != null )) {
                for (final StorageDomainMember member : domainMembersResponse.getStorageDomainMemberListResult().getStorageDomainMembers()) {
                    if (member.getTapePartitionId() != null) {
                        checkBlackPearlForPartitionFailure(client, member.getTapePartitionId());
                    }
                }
            }
        }

        // check disk pools
        GetPoolsSpectraS3Request poolsrequest = new GetPoolsSpectraS3Request().withBucketId(bucketname);
        GetPoolsSpectraS3Response poolsResponse = client.getPoolsSpectraS3(poolsrequest);
        List<Pool> pools  = poolsResponse.getPoolListResult().getPools();
        for (final Pool pool : pools) {
            checkBlackPearlForPoolFailure(client, pool.getId());
            checkBlackPearlForStorageDomainFailure(client, pool.getStorageDomainId());
            checkBlackPearlForPartitionFailure(client, pool.getPartitionId());
        }
    }

    public static void checkBlackPearlForSystemFailure(final Ds3Client client) throws IOException, SignatureException, CommandException {
        final List<SystemFailure> sysFailures = client.getSystemFailuresSpectraS3(new GetSystemFailuresSpectraS3Request()).getSystemFailureListResult().getSystemFailures();

        if (Guard.isNotNullAndNotEmpty(sysFailures)) {
            throw new CommandException(new SystemFailureException(sysFailures.iterator()));
        }
    }

    public static void checkBlackPearlForTapeFailure(final Ds3Client client) throws IOException, SignatureException, CommandException {
        final GetTapeFailuresSpectraS3Request request = new GetTapeFailuresSpectraS3Request();
        final List<DetailedTapeFailure> tapeFailures = client.getTapeFailuresSpectraS3(request).getDetailedTapeFailureListResult().getDetailedTapeFailures();

        if (Guard.isNotNullAndNotEmpty(tapeFailures)) {
            throw new CommandException(new TapeFailureException(tapeFailures.iterator()));
        }
    }

    public static void checkBlackPearlForPartitionFailure(final Ds3Client client, UUID partitionId) throws IOException, SignatureException, CommandException {
        final GetTapePartitionFailuresSpectraS3Request partitionRequest = new GetTapePartitionFailuresSpectraS3Request().withPartitionId(partitionId);
        final TapePartitionFailureList failureList = client.getTapePartitionFailuresSpectraS3(new GetTapePartitionFailuresSpectraS3Request()).getTapePartitionFailureListResult();
        final List<TapePartitionFailure> partitionFailures = failureList.getTapePartitionFailures();

        if (Guard.isNotNullAndNotEmpty(partitionFailures)) {
            throw new CommandException(new TapePartitionFailureException(partitionFailures.iterator()));
        }
    }

    public static void checkBlackPearlForPoolFailure(final Ds3Client client, UUID poolId) throws IOException, SignatureException, CommandException {
        final GetPoolFailuresSpectraS3Request PoolFailureRequest = new GetPoolFailuresSpectraS3Request().   withPoolId(poolId);
        final PoolFailureList failureList = client.getPoolFailuresSpectraS3(PoolFailureRequest).getPoolFailureListResult();
        final List<PoolFailure> poolFailures = failureList.getPoolFailures();

        if (Guard.isNotNullAndNotEmpty(poolFailures)) {
            throw new CommandException(new PoolFailureException(poolFailures.iterator()));
        }
    }

    public static void checkBlackPearlForStorageDomainFailure(final Ds3Client client, UUID storageDomainId) throws IOException, SignatureException, CommandException {
        final GetStorageDomainFailuresSpectraS3Request storageRequest = new GetStorageDomainFailuresSpectraS3Request().withStorageDomainId(storageDomainId);
        final StorageDomainFailureList failureList = client.getStorageDomainFailuresSpectraS3(storageRequest).getStorageDomainFailureListResult();
        final List<StorageDomainFailure> domainFailures = failureList.getStorageDomainFailures();

        if (Guard.isNotNullAndNotEmpty(domainFailures)) {
            StringBuffer errorListString = new StringBuffer();
            throw new CommandException(new StorageDomainFailureException(domainFailures.iterator()));
        }
    }

}
