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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.GetTapesResult;
import com.spectralogic.ds3client.commands.spectrads3.GetStorageDomainsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetStorageDomainsSpectraS3Response;
import com.spectralogic.ds3client.models.StorageDomain;
import com.spectralogic.ds3client.models.StorageDomainList;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3cli.views.cli.GetTapesView;
import com.spectralogic.ds3client.commands.spectrads3.GetTapesSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetTapesSpectraS3Response;
import org.apache.commons.cli.Option;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.spectralogic.ds3cli.ArgumentFactory.BUCKET;

public class GetTapes extends CliCommand<GetTapesResult> {
    private final static Logger LOG = LoggerFactory.getLogger(GetTapes.class);
    private final static ImmutableList<Option> OPTIONAL_ARGS = ImmutableList.of(BUCKET);

    private final GetTapesSpectraS3Request getTapesSpectraS3Request = new GetTapesSpectraS3Request();


    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(EMPTY_LIST, OPTIONAL_ARGS, args);

        final String bucketId = args.getBucket();
        if ( ! Guard.isStringNullOrEmpty(bucketId)) {
            getTapesSpectraS3Request.withBucketId(bucketId);
        }

        return this;
    }

    @Override
    public GetTapesResult call() throws Exception {
        final GetTapesSpectraS3Response response = getClient().getTapesSpectraS3(getTapesSpectraS3Request);

        return new GetTapesResult(response.getTapeListResult(), populateStorageDomainIdNameMap());
    }

    private ImmutableMap<UUID, String> populateStorageDomainIdNameMap() {
        final ImmutableMap.Builder<UUID, String> storageDomainIdNameMapBuilder = ImmutableMap.builder();

        try {
            final GetStorageDomainsSpectraS3Response getStorageDomainsSpectraS3Response = getClient().getStorageDomainsSpectraS3(new GetStorageDomainsSpectraS3Request());
            processStorageDomainList(getStorageDomainsSpectraS3Response.getStorageDomainListResult(), storageDomainIdNameMapBuilder);
        } catch (final Throwable t) {
            LOG.debug("Error populating storage domain info.", t);
        }

        return storageDomainIdNameMapBuilder.build();
    }

    private void processStorageDomainList(final StorageDomainList storageDomainList, final ImmutableMap.Builder<UUID, String> storageDomainIdNameMapBuilder) {
        if (storageDomainList != null) {
            processStorageDomains(storageDomainList.getStorageDomains(), storageDomainIdNameMapBuilder);
        }
    }

    private void processStorageDomains(final List<StorageDomain> storageDomains, final ImmutableMap.Builder<UUID, String> storageDomainIdNameMapBuilder) {
        if (storageDomains != null) {
            for (final StorageDomain storageDomain : storageDomains) {
                storageDomainIdNameMapBuilder.put(storageDomain.getId(), storageDomain.getName());
            }
        }
    }

    @Override
    public View<GetTapesResult> getView() {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetTapesView();
        }
        return new GetTapesView();
    }
}
