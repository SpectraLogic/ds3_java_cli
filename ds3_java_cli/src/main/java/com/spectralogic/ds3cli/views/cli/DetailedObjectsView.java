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

package com.spectralogic.ds3cli.views.cli;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.utils.Guard;

import java.util.ArrayList;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Utils.*;

public class DetailedObjectsView extends TableView<GetDetailedObjectsResult> {

    private static final String TAPE_SEPARATOR = " | ";

    private ImmutableList<DetailedS3Object> objects;

    @Override
    public String render(final GetDetailedObjectsResult obj) {
        if (obj == null || obj.getDetailedObjects() == null || Iterables.isEmpty(obj.getDetailedObjects())) {
            return "No objects returned";
        }

        objects = ImmutableList.copyOf(obj.getDetailedObjects());
        initTable(ImmutableList.of("Name", "Bucket", "Owner", "Size", "Type", "Creation Date", "Tapes", "Pools"));

        return renderTable();
    }

    @Override
    protected String[][] formatTableContents() {
        final ArrayList<String[]> formatArray = new ArrayList<>();
        int lineCount = 0;
        for (final DetailedS3Object detailedObject : this.objects) {
            final String [] bucketArray = new String[this.columnCount];
            bucketArray[0] = nullGuard(detailedObject.getName());
            bucketArray[1] = nullGuardToString(detailedObject.getBucketId());
            bucketArray[2] = nullGuardToString(detailedObject.getOwner());
            bucketArray[3] = nullGuardToString(detailedObject.getSize());
            bucketArray[4] = nullGuardToString(detailedObject.getType());
            bucketArray[5] = nullGuardToDate(detailedObject.getCreationDate(), DATE_FORMAT);
            bucketArray[6] = concatenateTapes(detailedObject.getBlobs());
            bucketArray[7] = concatenatePools(detailedObject.getBlobs());
            formatArray.add(bucketArray);
            lineCount++;
        }
        final String[][] ret = new String[lineCount][this.columnCount];
        return formatArray.toArray(ret);
    }

    private String concatenateTapes(final BulkObjectList objects) {
        if(Guard.isNullOrEmpty(objects.getObjects())) {
            return "No Physical Placement";
        }
        final ArrayList<String> tapes = new ArrayList<>();
        for (final BulkObject object : objects.getObjects()) {
            // hang on tight, we're mining for the items we want.
            if (object.getPhysicalPlacement() != null) {
                if (!Guard.isNullOrEmpty(object.getPhysicalPlacement().getTapes())) {
                    for (final Tape tape : object.getPhysicalPlacement().getTapes()) {
                        tapes.add(tape.getBarCode());
                    }
                }
            }
        }
        return Joiner.on(TAPE_SEPARATOR).join(tapes);
    }

    private String concatenatePools(final BulkObjectList objects) {
        if(Guard.isNullOrEmpty(objects.getObjects())) {
            return "No Physical Placement";
        }
        final ArrayList<String> pools = new ArrayList<>();
        for (final BulkObject object : objects.getObjects()) {
            // hang on tight, we're mining for the items we want.
            if (object.getPhysicalPlacement() != null) {
                if (!Guard.isNullOrEmpty(object.getPhysicalPlacement().getPools())) {
                    for (final Pool pool : object.getPhysicalPlacement().getPools()) {
                        pools.add(pool.getName());
                    }
                }
            }
        }
        return Joiner.on(TAPE_SEPARATOR).join(pools);
    }

}
