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
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.GetTapesResult;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3cli.views.cli.GetTapesView;
import com.spectralogic.ds3client.commands.spectrads3.GetTapesSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetTapesSpectraS3Response;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.ArgumentFactory.BUCKET;

public class GetTapes extends CliCommand<GetTapesResult> {
    final GetTapesSpectraS3Request getTapesSpectraS3Request = new GetTapesSpectraS3Request();
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(BUCKET);

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(EMPTY_LIST, optionalArgs, args);

        final String bucketId = args.getBucket();
        if ( ! Guard.isStringNullOrEmpty(bucketId)) {
            getTapesSpectraS3Request.withBucketId(bucketId);
        }

        return this;
    }

    @Override
    public GetTapesResult call() throws Exception {
        final GetTapesSpectraS3Response response = getClient().getTapesSpectraS3(getTapesSpectraS3Request);

        return new GetTapesResult(response.getTapeListResult());
    }

    @Override
    public View<GetTapesResult> getView() {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetTapesView();
        }
        return new GetTapesView();
    }
}
