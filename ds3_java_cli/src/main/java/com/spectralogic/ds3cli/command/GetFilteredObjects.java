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

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3cli.views.cli.DetailedObjectsView;
import com.spectralogic.ds3client.helpers.pagination.GetObjectsFullDetailsLoaderFactory;
import com.spectralogic.ds3client.models.DetailedS3Object;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.collections.LazyIterable;

import java.util.Date;
import javax.annotation.Nullable;

public class GetFilteredObjects extends CliCommand<GetDetailedObjectsResult> {

    private ImmutableMap<String, String> filterParams;
    private String bucketName;
    private String prefix;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.filterParams = args.getFilterParams();
        this.bucketName = args.getBucket();

        if (!Guard.isStringNullOrEmpty(args.getPrefix())) {
            throw new BadArgumentException("'-p' prefix is not supported.");
        }
        this.prefix = null;
        return this;
    }

    @Override
    public GetDetailedObjectsResult call() throws Exception {

        final ImmutableList<DetailedS3Object> suspectBulkObjects;
        Predicate<DetailedS3Object> filterPredicate;

        if (Guard.isMapNullOrEmpty(this.filterParams)) {
            // no filter params specified, run wide open
            filterPredicate = new Predicate<DetailedS3Object>() {
                @Override
                public boolean apply(@Nullable final DetailedS3Object input) {
                    return (input != null);
                }};
        } else {
            long larger = 0L;
            long smaller = Long.MAX_VALUE;
            Date newer = new Date(0);
            Date older = new Date(Long.MAX_VALUE);

            for (final String paramChange : this.filterParams.keySet()) {
                final String paramNewValue = this.filterParams.get(paramChange);
                if ("largerthan".equalsIgnoreCase(paramChange)) {
                    larger = Long.parseLong(paramNewValue);
                } else if ("smallerthan".equalsIgnoreCase(paramChange)) {
                    smaller = Long.parseLong(paramNewValue);
                } else if ("newerthan".equalsIgnoreCase(paramChange)) {
                    newer = new Date(new Date().getTime() - Utils.dateDiffToSeconds(paramNewValue) * 1000);
                } else if ("olderthan".equalsIgnoreCase(paramChange)) {
                    older = new Date(new Date().getTime() - Utils.dateDiffToSeconds(paramNewValue) * 1000);
                } else {
                    throw new CommandException("Unrecognized filter parameter: " + paramChange);
                }
            } // for

            // make final for inner class
            final long largerthan = larger;
            final long smallerthan = smaller;
            final Date newerthan = newer;
            final Date olderthan = older;

            filterPredicate = new Predicate<DetailedS3Object>() {
                @Override
                public boolean apply(@Nullable final DetailedS3Object input) {
                    if (input == null) {
                        return false;
                    }
                    return (input.getSize() > largerthan
                            && input.getSize() < smallerthan
                            && input.getCreationDate().after(newerthan)
                            && input.getCreationDate().before(olderthan)
                    );
                }
            };
        }

        // get filtered list using pagination
        suspectBulkObjects = ImmutableList.copyOf(
                FluentIterable.from(new LazyIterable<DetailedS3Object>(
                        new GetObjectsFullDetailsLoaderFactory(getClient(), this.bucketName, this.prefix, 100, 5, true))).filter(
                        filterPredicate));

        return new GetDetailedObjectsResult(suspectBulkObjects.iterator());
    }

    @Override
    public View<GetDetailedObjectsResult> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.DetailedObjectsView();
        } else if (viewType == ViewType.CSV) {
            return new com.spectralogic.ds3cli.views.csv.DetailedObjectsView();
        } else {
            return new DetailedObjectsView();
        }
    }

}
