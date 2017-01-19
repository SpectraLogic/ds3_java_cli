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

package com.spectralogic.ds3cli.command;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.api.Arguments;
import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.api.ViewType;
import com.spectralogic.ds3cli.api.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetStorageDomainsResult;
import com.spectralogic.ds3client.commands.spectrads3.GetStorageDomainSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetStorageDomainSpectraS3Response;
import com.spectralogic.ds3client.commands.spectrads3.GetStorageDomainsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetStorageDomainsSpectraS3Response;
import com.spectralogic.ds3client.models.WriteOptimization;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.Option;

import java.io.IOException;

import static com.spectralogic.ds3cli.api.ArgumentFactory.ID;
import static com.spectralogic.ds3cli.api.ArgumentFactory.WRITE_OPTIMIZATION;

public class GetStorageDomains extends BaseCliCommand<GetStorageDomainsResult> {

    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(ID, WRITE_OPTIMIZATION);

    private String id;
    private WriteOptimization writeOptimization;

    public GetStorageDomains() {
    }

    @Override
    public BaseCliCommand init(final Arguments args) throws Exception {
        processCommandOptions(EMPTY_LIST, optionalArgs, args);

        this.id = args.getId();
        this.writeOptimization = args.getWriteOptimization();
        return this;
    }

    @Override
    public GetStorageDomainsResult call() throws IOException, CommandException {
        if (Guard.isStringNullOrEmpty(this.id)) {
            final GetStorageDomainsSpectraS3Response response
                    = getClient().getStorageDomainsSpectraS3(
                    new GetStorageDomainsSpectraS3Request().withWriteOptimization(writeOptimization));

            return new GetStorageDomainsResult(response.getStorageDomainListResult());
        } else {
            // if -i is specified, call get single domain
            final GetStorageDomainSpectraS3Response response
                    = getClient().getStorageDomainSpectraS3(
                    new GetStorageDomainSpectraS3Request(id));

            return new GetStorageDomainsResult(response.getStorageDomainResult());
        }
    }

    @Override
    public View<GetStorageDomainsResult> getView() {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetStorageDomainsView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetStorageDomainsView();
    }
}
