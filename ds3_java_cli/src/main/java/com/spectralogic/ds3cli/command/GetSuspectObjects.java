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
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.SuspectedObjectResult;
import com.spectralogic.ds3cli.views.cli.SuspectedObjectsView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.GetSuspectObjectsWithFullDetailsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetSuspectObjectsWithFullDetailsSpectraS3Response;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.BulkObjectList;
import org.apache.commons.cli.Option;

import javax.annotation.Nullable;

import static com.spectralogic.ds3cli.ArgumentFactory.IN_CACHE;

public class GetSuspectObjects extends CliCommand<SuspectedObjectResult> {

    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(IN_CACHE);

    private boolean inCache;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(EMPTY_LIST, optionalArgs, args);

        this.inCache = args.isInCache();
        return this;
    }

    @Override
    public SuspectedObjectResult call() throws Exception {

        final GetSuspectObjectsWithFullDetailsSpectraS3Response suspectObjectsWithFullDetailsSpectraS3 = getClient().getSuspectObjectsWithFullDetailsSpectraS3(new GetSuspectObjectsWithFullDetailsSpectraS3Request());

        final BulkObjectList bulkObjectListResult = suspectObjectsWithFullDetailsSpectraS3.getBulkObjectListResult();

        final ImmutableList<BulkObject> suspectBulkObjects;

        if (inCache) {
            if (bulkObjectListResult.getObjects() == null) {
                suspectBulkObjects = ImmutableList.of();
            } else {
                suspectBulkObjects = ImmutableList.copyOf(FluentIterable.from(bulkObjectListResult.getObjects()).filter(new Predicate<BulkObject>() {
                    @Override
                    public boolean apply(@Nullable final BulkObject input) {
                        if (input == null) {
                            return false;
                        }
                        return input.getInCache();
                    }
                }));
            }
        } else {
            suspectBulkObjects = ImmutableList.copyOf(bulkObjectListResult.getObjects());
        }

        return new SuspectedObjectResult(suspectBulkObjects);
    }

    @Override
    public View<SuspectedObjectResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        } else {
            return new SuspectedObjectsView();
        }
    }
}
