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

import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.api.ViewType;
import com.spectralogic.ds3cli.api.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetCacheStateResult;
import com.spectralogic.ds3client.commands.spectrads3.GetCacheStateSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetCacheStateSpectraS3Response;
import com.spectralogic.ds3client.models.CacheInformation;

import java.io.IOException;

public class GetCacheState extends BaseCliCommand<GetCacheStateResult> {

    public GetCacheState() {
    }

    @Override
    public GetCacheStateResult call() throws IOException, CommandException {
        final GetCacheStateSpectraS3Response cacheStateResponse
                = getClient().getCacheStateSpectraS3(new GetCacheStateSpectraS3Request());
        final CacheInformation response = cacheStateResponse.getCacheInformationResult();
        return new GetCacheStateResult(response.getFilesystems());
    }

    @Override
    public View<GetCacheStateResult> getView() {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetCacheStateView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetCacheStateView();
    }
}

