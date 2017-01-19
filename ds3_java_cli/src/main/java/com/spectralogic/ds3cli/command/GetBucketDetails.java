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
import com.spectralogic.ds3cli.api.CliCommand;
import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.jsonview.DataView;
import com.spectralogic.ds3cli.models.GetBucketResult;
import com.spectralogic.ds3client.commands.spectrads3.GetBucketSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetBucketSpectraS3Response;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.api.ArgumentFactory.BUCKET;


public class GetBucketDetails extends BaseCliCommand<GetBucketResult> {


    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET);

    private String bucket;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);

        this.bucket = args.getBucket();
        return this;
    }


    @Override
    public GetBucketResult call() throws Exception {
        final GetBucketSpectraS3Response response = getClient().getBucketSpectraS3(new GetBucketSpectraS3Request(bucket));
        return new GetBucketResult(response.getBucketResult(), null);
    }

    @Override
    public View<GetBucketResult> getView() {
        switch (viewType) {
            case JSON:
                return new DataView<>();
            default:
                return new com.spectralogic.ds3cli.views.cli.GetBucketDetailsView();
        }
    }
}
