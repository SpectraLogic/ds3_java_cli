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

package com.spectralogic.ds3cli.views.csv;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetBucketResult;
import com.spectralogic.ds3client.models.Contents;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Guard.nullGuard;
import static com.spectralogic.ds3cli.util.Guard.nullGuardFromDate;

public class GetBucketView implements View<GetBucketResult> {

    @Override
    public String render(final GetBucketResult result) {
        // Bucket details
        final String bucketDesc = new GetBucketDetailsView().render(result);

        String bucketContents;
        if (null == result.getResult() || Iterables.isEmpty(result.getResult())) {
            bucketContents =  "No objects were reported in bucket.";
        } else {

            final ImmutableList<String> headers = ImmutableList.of("File Name", "Size", "Owner", "Last Modified", "ETag");

            bucketContents = new CsvOutput<>(headers, result.getResult(), new CsvOutput.ContentFormatter<Contents>() {
                @Override
                public Iterable<String> format(final Contents content) {
                    final ImmutableList.Builder<String> builder = ImmutableList.builder();
                    builder.add(nullGuard(content.getKey()));
                    builder.add(nullGuard(Long.toString(content.getSize())));
                    builder.add((content.getOwner() != null) ? content.getOwner().getDisplayName() : "N/A");
                    builder.add(nullGuardFromDate(content.getLastModified(), DATE_FORMAT));
                    builder.add(nullGuard(content.getETag()));
                    return builder.build();
                }
            }).toString();
        }
        return String.format("%s\r\n%s", bucketDesc, bucketContents);

    }

}
