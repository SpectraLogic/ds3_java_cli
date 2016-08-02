/*
 * ******************************************************************************
 *   Copyright 2016 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetCapacitySummaryResult;
import com.spectralogic.ds3client.commands.spectrads3.GetCacheStateSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetCacheStateSpectraS3Response;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemCapacitySummarySpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemCapacitySummarySpectraS3Response;
import com.spectralogic.ds3client.models.CacheInformation;
import com.spectralogic.ds3client.models.CapacitySummaryContainer;
import com.spectralogic.ds3client.models.StorageDomainCapacitySummary;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.utils.SSLSetupException;

import java.io.IOException;
import java.security.SignatureException;

public class GetCapacitySummary extends CliCommand<GetCapacitySummaryResult> {

    public GetCapacitySummary() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public GetCapacitySummaryResult call() throws IOException, SignatureException, SSLSetupException, CommandException {
        try {
            final GetSystemCapacitySummarySpectraS3Response systemCapacitySummaryResponse =
                    getClient().getSystemCapacitySummarySpectraS3(new GetSystemCapacitySummarySpectraS3Request());
            final CapacitySummaryContainer capacitySummaryContainer =
                    systemCapacitySummaryResponse.getCapacitySummaryContainerResult();
            return new GetCapacitySummaryResult(capacitySummaryContainer);
        } catch (final FailedRequestException e) {
            throw new CommandException("Failed Get Data Policies", e);
        }
    }

    @Override
    public View<GetCapacitySummaryResult> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetCapacitySummaryView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetCapacitySummaryView();
    }
}

