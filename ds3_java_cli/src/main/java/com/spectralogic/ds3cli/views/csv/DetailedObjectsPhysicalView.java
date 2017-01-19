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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.DetailedS3Object;
import com.spectralogic.ds3client.models.Tape;
import com.spectralogic.ds3client.utils.Guard;

import javax.annotation.Nullable;

import static com.spectralogic.ds3cli.utils.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.utils.Guard.nullGuard;
import static com.spectralogic.ds3cli.utils.Guard.nullGuardFromDate;
import static com.spectralogic.ds3cli.utils.Guard.nullGuardToString;

public class DetailedObjectsPhysicalView implements View<GetDetailedObjectsResult> {

    @Override
    public String render(final GetDetailedObjectsResult obj) {
        final Iterable<DetailedS3Object> detailedS3Objects = obj.getResult();
        if (detailedS3Objects == null || Iterables.isEmpty(detailedS3Objects)) {
            return "No objects returned";
        }

        final ImmutableList<String> headers = ImmutableList.of("Name", "Bucket", "Owner", "Size", "Type", "Creation Date", "Barcode", "State");

        final FluentIterable<DetailedTapeInfo> objects = FluentIterable.from(detailedS3Objects)
                .filter(new Predicate<DetailedS3Object>() {
                    @Override
                    public boolean apply(@Nullable final DetailedS3Object input) {
                        return input.getBlobs() != null && !Guard.isNullOrEmpty(input.getBlobs().getObjects());
                    }
                }).transformAndConcat(new DetailedDs3ObjectMapper());

        return new CsvOutput<>(headers, objects, new CsvOutput.ContentFormatter<DetailedTapeInfo>() {
            @Override
            public Iterable<String> format(final DetailedTapeInfo content) {

                final ImmutableList.Builder<String> csvRow = ImmutableList.builder();
                final DetailedS3Object detailedObject = content.getDetailedS3Object();
                final Tape tape = content.getTape();

                csvRow.add(nullGuard(detailedObject.getName()));
                csvRow.add(nullGuardToString(detailedObject.getBucketId()));
                csvRow.add(nullGuardToString(detailedObject.getOwner()));
                csvRow.add(nullGuardToString(detailedObject.getSize()));
                csvRow.add(nullGuardToString(detailedObject.getType()));
                csvRow.add(nullGuardFromDate(detailedObject.getCreationDate(), DATE_FORMAT));
                csvRow.add(nullGuard(tape.getBarCode()));
                csvRow.add(nullGuardToString(tape.getState()));
                return csvRow.build();
            }
        }).toString();
    }

    /**
     * Takes a DetailedS3Object and converts it to an Iterable of DetailedTapeInfo objects
     */
    private class DetailedDs3ObjectMapper implements Function<DetailedS3Object, Iterable<DetailedTapeInfo>> {
        @Nullable
        @Override
        public Iterable<DetailedTapeInfo> apply(@Nullable final DetailedS3Object detailedS3Object) {

            return FluentIterable.from(detailedS3Object.getBlobs().getObjects()).filter(new Predicate<BulkObject>() {
                @Override
                public boolean apply(@Nullable final BulkObject input) {
                    return input.getPhysicalPlacement() != null && !Guard.isNullOrEmpty(input.getPhysicalPlacement().getTapes());
                }
            }).transformAndConcat(new ToDetailedTapeInfo(detailedS3Object));
        }
    }

    /**
     * Takes a DetailedS3Object and Tape, and returns an Iterable of DetailedTapeInfo objects
     */
    private class ToDetailedTapeInfo implements Function<BulkObject, Iterable<DetailedTapeInfo>> {

        private final DetailedS3Object detailedS3Object;

        private ToDetailedTapeInfo(final DetailedS3Object detailedS3Object) {
            this.detailedS3Object = detailedS3Object;
        }

        @Nullable
        @Override
        public Iterable<DetailedTapeInfo> apply(@Nullable final BulkObject bulkObject) {
            return FluentIterable.from(bulkObject.getPhysicalPlacement().getTapes()).transform(new Function<Tape, DetailedTapeInfo>() {
                @Nullable
                @Override
                public DetailedTapeInfo apply(@Nullable final Tape tape) {
                    return new DetailedTapeInfo(tape, detailedS3Object);
                }
            });
        }
    }

    private class DetailedTapeInfo {
        private final Tape tape;
        private final DetailedS3Object detailedS3Object;

        private DetailedTapeInfo(final Tape tape, final DetailedS3Object detailedS3Object) {
            this.tape = tape;
            this.detailedS3Object = detailedS3Object;
        }

        public Tape getTape() {
            return tape;
        }

        public DetailedS3Object getDetailedS3Object() {
            return detailedS3Object;
        }
    }
}
