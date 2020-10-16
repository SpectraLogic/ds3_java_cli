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
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetBucketResult;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.GetBucketRequest;
import com.spectralogic.ds3client.commands.GetBucketResponse;
import com.spectralogic.ds3client.commands.spectrads3.GetBucketSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetBucketSpectraS3Response;
import com.spectralogic.ds3client.models.Bucket;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.Option;
import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.ArgumentFactory.BUCKET;
import static com.spectralogic.ds3cli.ArgumentFactory.PREFIX;
import static com.spectralogic.ds3cli.ArgumentFactory.SHOW_VERSIONS;

public class GetBucket extends CliCommand<GetBucketResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(PREFIX, SHOW_VERSIONS);
    private final static int maxKeys = 1000;

    private String bucket;
    private String prefix;
    private boolean showVersion;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.bucket = args.getBucket();
        this.prefix = args.getPrefix();
        this.showVersion = args.isShowVersions();
        return this;
    }

    @Override
    public GetBucketResult call() throws Exception {
        try {
            final List<Contents> contents = new ArrayList<>();
            // GetBucketDetail to get both name and id
            GetBucketRequest getBucketRequest = new GetBucketRequest(bucket)
                    .withVersions(showVersion)
                    .withPrefix(prefix)
                    .withMaxKeys(maxKeys);

            GetBucketResponse bucketResponse = getClient().getBucket(getBucketRequest);

            if (showVersion) {
                contents.addAll(bucketResponse.getListBucketResult().getVersionedObjects());
            } else {
                contents.addAll(bucketResponse.getListBucketResult().getObjects());
            }

            String marker = bucketResponse.getListBucketResult().getNextMarker();
            while (marker != null) {
                getBucketRequest = new GetBucketRequest(this.bucket)
                        .withVersions(showVersion)
                        .withPrefix(prefix)
                        .withMaxKeys(maxKeys)
                        .withMarker(marker);

                bucketResponse = getClient().getBucket(getBucketRequest);

                if (showVersion) {
                    contents.addAll(bucketResponse.getListBucketResult().getVersionedObjects());
                } else {
                    contents.addAll(bucketResponse.getListBucketResult().getObjects());
                }
                marker = bucketResponse.getListBucketResult().getNextMarker();
            }

            final GetBucketSpectraS3Request getBucketSpectraS3Request = new GetBucketSpectraS3Request(this.bucket);
            final GetBucketSpectraS3Response response = getClient().getBucketSpectraS3(getBucketSpectraS3Request);
            final Bucket bucketDetails = response.getBucketResult();
            return new GetBucketResult(bucketDetails, contents);
        } catch (final FailedRequestException e) {
            if (e.getStatusCode() == 404) {
                throw new CommandException("Error: Unknown bucket.", e);
            }
            throw e;
        }
    }

    @Override
    public View<GetBucketResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        if (showVersion) {
            return new com.spectralogic.ds3cli.views.cli.GetVersionedBucketView();
        } else {
            return new com.spectralogic.ds3cli.views.cli.GetBucketView();
        }
    }
}
