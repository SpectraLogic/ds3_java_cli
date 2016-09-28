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
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3cli.views.cli.DetailedObjectsView;
import com.spectralogic.ds3client.helpers.pagination.GetObjectsFullDetailsLoaderFactory;
import com.spectralogic.ds3client.models.DetailedS3Object;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.collections.LazyIterable;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.LoggerFactory;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class GetDetailedObjects extends CliCommand<GetDetailedObjectsResult> {
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(GetDetailedObjects.class);

    private final static String NEWERTHAN = "newerthan";
    private final static String OLDERTHAN = "olderthan";
    private final static String LARGERTHAN = "largerthan";
    private final static String SMALLERTHAN = "smallerthan";

    private ImmutableMap<String, String> filterParams;
    private String bucketName;
    private String prefix;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.filterParams = args.getFilterParams();
        this.bucketName = args.getBucket();

        if (Guard.isStringNullOrEmpty(this.bucketName)) {
            throw new MissingOptionException("The get detailed objects command requires '-b' to be set.");
        }

        if (!Guard.isStringNullOrEmpty(args.getPrefix())) {
            LOG.warn("'-p' prefix is not supported.");
        }
        this.prefix = null;
        return this;
    }

    @Override
    public GetDetailedObjectsResult call() throws Exception {

        final FluentIterable<DetailedS3Object> suspectBulkObjects;
        final Predicate<DetailedS3Object> filterPredicate = getPredicate();

        // get filtered list using pagination
        suspectBulkObjects = FluentIterable.from(new LazyIterable<DetailedS3Object>(
                        new GetObjectsFullDetailsLoaderFactory(getClient(), this.bucketName, this.prefix, 100, 5, true)))
                .filter(Predicates.notNull());

        if (filterPredicate != null) {
            return new GetDetailedObjectsResult(suspectBulkObjects.filter(filterPredicate));
        }

        return new GetDetailedObjectsResult(suspectBulkObjects);
    }

    protected Predicate<DetailedS3Object> getPredicate() throws CommandException {

        if (Guard.isMapNullOrEmpty(this.filterParams)) {
            return null;
        }

        // else build a predicate from search-params
        final Map<String, String> ranges = parseMeta();
        final long largerthan = Long.parseLong(ranges.get(LARGERTHAN));
        final long smallerthan = Long.parseLong(ranges.get(SMALLERTHAN));
        final Date newerthan = new Date(Long.parseLong(ranges.get(NEWERTHAN)));
        final Date olderthan = new Date(Long.parseLong(ranges.get(OLDERTHAN)));

        return new Predicate<DetailedS3Object>() {
            @Override
            public boolean apply(@Nullable final DetailedS3Object input) {
                return input.getSize() > largerthan
                        && input.getSize() < smallerthan
                        && input.getCreationDate().after(newerthan)
                        && input.getCreationDate().before(olderthan);
            }
        };
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

    private Map<String, String> parseMeta() throws CommandException {
        // load defaults and define legal values
        final Map<String, String> ranges = new HashMap<String, String>();
        ranges.put(NEWERTHAN, "0");
        ranges.put(OLDERTHAN, Long.toString(Long.MAX_VALUE));
        ranges.put(LARGERTHAN, "0");
        ranges.put(SMALLERTHAN, Long.toString(Long.MAX_VALUE));

        for (final String paramChange : this.filterParams.keySet()) {
            final String paramNewValue = this.filterParams.get(paramChange);
            if (ranges.containsKey(paramChange)) {
                if(paramChange.equals(NEWERTHAN) || paramChange.equals(OLDERTHAN)){
                    final long relativeDate = new Date().getTime() - Utils.dateDiffToSeconds(paramNewValue) * 1000;
                    ranges.put(paramChange, Long.toString(relativeDate));
                } else {
                    ranges.put(paramChange, paramNewValue);
                }
            } else {
                throw new CommandException("Unrecognized filter parameter: " + paramChange);
            }
        } // for
        return ranges;
    }

}
