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
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3cli.views.cli.DetailedObjectsView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.helpers.pagination.GetObjectsFullDetailsLoaderFactory;
import com.spectralogic.ds3client.models.DetailedS3Object;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.collections.LazyIterable;
import org.apache.commons.cli.Option;


import java.text.ParseException;
import java.util.Date;
import javax.annotation.Nullable;

import static com.spectralogic.ds3cli.ArgumentFactory.BUCKET;
import static com.spectralogic.ds3cli.ArgumentFactory.FILTER_PARAMS;

public class GetDetailedObjects extends CliCommand<GetDetailedObjectsResult> {

    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(FILTER_PARAMS, BUCKET);

    // valid filter params
    private final static String CONTAINS = "contains";
    private final static String OWNER = "owner";
    private final static String NEWERTHAN = "newerthan";
    private final static String OLDERTHAN = "olderthan";
    private final static String BEFORE = "before";
    private final static String AFTER = "after";
    private final static String LARGERTHAN = "largerthan";
    private final static String SMALLERTHAN = "smallerthan";
    private final static long MILLIS_PER_SECOND = 1000L;

    private ImmutableMap<String, String> filterParams;
    private String bucketName;
    private String prefix;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(EMPTY_LIST, optionalArgs, args);

        this.filterParams = args.getFilterParams();
        checkFilterParams();
        this.bucketName = args.getBucket();

        this.prefix = null; // doesn't work
        return this;
    }

    private void checkFilterParams() throws BadArgumentException {
        // if filter-params are specified, ascertain that all are supported
        if (Guard.isMapNullOrEmpty(this.filterParams)) {
            return;
        }
        final ImmutableList<String> legalParams
                = ImmutableList.of(CONTAINS, OWNER, NEWERTHAN,  OLDERTHAN,  LARGERTHAN,  SMALLERTHAN, BEFORE, AFTER);
        for (final String paramName : this.filterParams.keySet()) {
            if (!legalParams.contains(paramName)) {
                throw new BadArgumentException("Unknown filter parameter: " + paramName);
            }
        }
    }

    @Override
    public GetDetailedObjectsResult call() throws Exception {

        final FluentIterable<DetailedS3Object> detailedObjects = FluentIterable.from(new LazyIterable<>(
                new GetObjectsFullDetailsLoaderFactory(getClient(), this.bucketName, this.prefix, 100, 5, true)))
                .filter(Predicates.and(getDatePredicate(), getSizePredicate(), getNamePredicate(), getOwnerPredicate()));

        return new GetDetailedObjectsResult(detailedObjects);
    }

    private Predicate<DetailedS3Object> getSizePredicate() {
        if (Guard.isMapNullOrEmpty(this.filterParams)) {
            return Predicates.notNull();
        }

        final String larger = this.filterParams.get(LARGERTHAN);
        final String smaller = this.filterParams.get(SMALLERTHAN);
        if (Guard.isStringNullOrEmpty(larger) && Guard.isStringNullOrEmpty(smaller)) {
            return Predicates.notNull();
        }
        // if one is specified, use default value for other
        final long largerthan = Guard.isStringNullOrEmpty(larger) ? 0L : Long.parseLong(larger);
        final long smallerthan = Guard.isStringNullOrEmpty(smaller) ? Long.MAX_VALUE : Long.parseLong(smaller);

        return new Predicate<DetailedS3Object>() {
            @Override
            public boolean apply(@Nullable final DetailedS3Object input) {
                return input.getSize() > largerthan
                        && input.getSize() < smallerthan;
            }
        };
    }

    private Predicate<DetailedS3Object> getDatePredicate() throws ParseException {
        if (Guard.isMapNullOrEmpty(this.filterParams)) {
            return Predicates.notNull();
        }

        // newerthan and olderthan are relative
        final String newer = this.filterParams.get(NEWERTHAN);
        final String older = this.filterParams.get(OLDERTHAN);
        // before and after are actual dates
        final String after = this.filterParams.get(AFTER);
        final String before = this.filterParams.get(BEFORE);

        // if any of these are set, create a range for the predicate
        if (!Guard.isStringNullOrEmpty(newer) && !Guard.isStringNullOrEmpty(older)
                && !Guard.isStringNullOrEmpty(after) && !Guard.isStringNullOrEmpty(before)) {
            return Predicates.notNull();
        }

        Date newerThan = new Date(0L);
        Date olderThan = new Date(Long.MAX_VALUE);

        if (!Guard.isStringNullOrEmpty(newer)) {
            newerThan = new Date(new Date().getTime() - Utils.dateDiffToSeconds(newer) * MILLIS_PER_SECOND);
        }
        if (!Guard.isStringNullOrEmpty(older)) {
            olderThan = new Date(new Date().getTime() - Utils.dateDiffToSeconds(older) * MILLIS_PER_SECOND);
        }
        if (!Guard.isStringNullOrEmpty(after)) {
            newerThan = Utils.parseParamDate(after);
        }
        if (!Guard.isStringNullOrEmpty(before)) {
            olderThan = Utils.parseParamDate(before);
        }
        // set final to pass iknto predicate
        final Date newerThanDate = newerThan;
        final Date olderThanDate = olderThan;

        return new Predicate<DetailedS3Object>() {
            @Override
            public boolean apply(@Nullable final DetailedS3Object input) {
                return input.getCreationDate().after(newerThanDate)
                        && input.getCreationDate().before(olderThanDate);
            }
        };
    }

    private Predicate<DetailedS3Object> getOwnerPredicate() {
        if (Guard.isMapNullOrEmpty(this.filterParams)) {
            return Predicates.notNull();
        }
        final String owner = paramLookup(OWNER);
        if (Guard.isStringNullOrEmpty(owner)) {
            return Predicates.notNull();
        }
        return new Predicate<DetailedS3Object>() {
            @Override
            public boolean apply(@Nullable final DetailedS3Object input) {
                return input.getOwner().equals(owner);
            }
        };
    }

    private Predicate<DetailedS3Object> getNamePredicate() {
        if (Guard.isMapNullOrEmpty(this.filterParams)) {
            return Predicates.notNull();
        }
        final String contains = paramLookup(CONTAINS);
        if (Guard.isStringNullOrEmpty(contains)) {
            return Predicates.notNull();
        }
        return new Predicate<DetailedS3Object>() {
            @Override
            public boolean apply(@Nullable final DetailedS3Object input) {
                return input.getName().contains(contains);
            }
        };
    }

    private String paramLookup(final String key) {
        return this.filterParams.get(key);
    }

    @Override
    public View<GetDetailedObjectsResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        } else if (viewType == ViewType.CSV) {
            return new com.spectralogic.ds3cli.views.csv.DetailedObjectsView();
        } else {
            return new DetailedObjectsView();
        }
    }

}
