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
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetBucketResult;
import com.spectralogic.ds3client.models.Bucket;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Guard.nullGuard;
import static com.spectralogic.ds3cli.util.Guard.nullGuardToString;
import static com.spectralogic.ds3cli.util.Guard.nullGuardFromDate;

public class GetBucketDetailsView implements View<GetBucketResult> {

    @Override
    public String render(final GetBucketResult result) {
        final Bucket bucket = result.getBucket();
        if (null == bucket) {
            return "No details for bucket.";
        }
        final ImmutableList<String> headers = ImmutableList.of("Name", "Id", "User Id", "Creation Date", "Data Policy Id", "Used Capacity");

        try {
            final StringWriter writer = new StringWriter();
            final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
            csvPrinter.printRecord(headers);
            csvPrinter.printRecord(formatRow(bucket));
            return writer.toString();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to write csv", e);
        }
    }

    private Iterable<String> formatRow(final Bucket bucket) {
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add(nullGuard(bucket.getName()));
        builder.add(nullGuardToString(bucket.getId()));
        builder.add(nullGuardToString(bucket.getUserId()));
        builder.add(nullGuardFromDate(bucket.getCreationDate(), DATE_FORMAT));
        builder.add(nullGuardToString(bucket.getDataPolicyId()));
        builder.add(nullGuardToString(bucket.getLogicalUsedCapacity()));
        return builder.build();
    }

}
