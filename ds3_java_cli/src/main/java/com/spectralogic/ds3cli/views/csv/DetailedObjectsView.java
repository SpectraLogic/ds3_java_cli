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
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.utils.Guard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.*;
import static com.spectralogic.ds3cli.views.cli.TableView.DATE_FORMAT;

public class DetailedObjectsView implements View<GetDetailedObjectsResult> {

    private static final String TAPE_SEPARATOR = " | ";

    @Override
    public String render(final GetDetailedObjectsResult obj) {
        if (obj == null || obj.getObjIterator() == null || Iterables.isEmpty(obj.getObjIterator())) {
            return "No objects returned";
        }

        final ImmutableList<String> headers = ImmutableList.of("Name", "Bucket", "Owner", "Size", "Type", "Creation Date", "Tapes", "Pools");

        return new CsvOutput<>(headers, obj.getObjIterator(), new CsvOutput.ContentFormatter<DetailedS3Object>() {
            @Override
            public Iterable<String> format(final DetailedS3Object content) {

                final ImmutableList.Builder<String> builder = ImmutableList.builder();
                builder.add(nullGuard(content.getName()));
                builder.add(nullGuardToString(content.getBucketId()));
                builder.add(nullGuardToString(content.getOwner()));
                builder.add(nullGuardToString(content.getSize()));
                builder.add(nullGuardToString(content.getType()));
                builder.add(nullGuardToDate(content.getCreationDate(), DATE_FORMAT));
                builder.add(concatenateTapes(content.getBlobs()));
                builder.add(concatenatePools(content.getBlobs()));
                return builder.build();
            }
        }).toString();
    }

    private static String concatenateTapes(final BulkObjectList objects) {
        if(Guard.isNullOrEmpty(objects.getObjects())) {
            return "No Physical Placement";
        }

        final FluentIterable<String> iterable = FluentIterable.from(objects.getObjects()).filter(new Predicate<BulkObject>() {
            @Override
            public boolean apply(@Nullable final BulkObject bulkObject) {
                return bulkObject.getPhysicalPlacement() != null && !Guard.isNullOrEmpty(bulkObject.getPhysicalPlacement().getTapes());
            }
        }).transformAndConcat(new Function<BulkObject, Iterable<String>>() {
            @Nullable
            @Override
            public Iterable<String> apply(@Nullable final BulkObject bulkObject) {
                return FluentIterable.from(bulkObject.getPhysicalPlacement().getTapes()).transform(new Function<Tape, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable final Tape tape) {
                        return tape.getBarCode();
                    }
                });
            }
        });

        return Joiner.on(TAPE_SEPARATOR).join(iterable);
    }

    private static String concatenatePools(final BulkObjectList objects) {
        if(Guard.isNullOrEmpty(objects.getObjects())) {
            return "No Physical Placement";
        }

        final FluentIterable<String> iterable = FluentIterable.from(objects.getObjects()).filter(new Predicate<BulkObject>() {
            @Override
            public boolean apply(@Nullable final BulkObject bulkObject) {
                return bulkObject.getPhysicalPlacement() != null && !Guard.isNullOrEmpty(bulkObject.getPhysicalPlacement().getPools());
            }
        }).transformAndConcat(new Function<BulkObject, Iterable<String>>() {
            @Nullable
            @Override
            public Iterable<String> apply(@Nullable final BulkObject bulkObject) {
                return FluentIterable.from(bulkObject.getPhysicalPlacement().getPools()).transform(new Function<Pool, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable final Pool pool) {
                        return pool.getName();
                    }
                });
            }
        });

        return Joiner.on(TAPE_SEPARATOR).join(iterable);
    }
}
