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

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3cli.views.csv.CsvView;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.DetailedS3Object;
import com.spectralogic.ds3client.models.Tape;
import com.spectralogic.ds3client.utils.Guard;

import java.util.ArrayList;

import static com.spectralogic.ds3cli.util.Utils.*;

public class DetailedObjectsPhysicalView extends TableView<GetDetailedObjectsResult> {
    private ImmutableList<DetailedS3Object> objects;

    @Override
    public String render(final GetDetailedObjectsResult obj) {
        if (obj == null || (obj.getObjIterator() == null) || !obj.getObjIterator().hasNext()) {
            return "No objects returned";
        }

        objects = ImmutableList.copyOf(obj.getObjIterator());
        initTable(ImmutableList.of("Name", "Bucket", "Owner", "Size", "Type", "Creation Date", "Barcode", "State"));

        return renderTable();
    }

    @Override
    protected String[][] formatTableContents() {
        final ArrayList<String[]> formatArray = new ArrayList<String[]>();
        int lineCount = 0;
        for (final DetailedS3Object detailedObject : this.objects) {
            // one line for each instance on tape -- mine down to get Physical Placement
            if((detailedObject.getBlobs() != null) && !Guard.isNullOrEmpty(detailedObject.getBlobs().getObjects())) {
                for (final BulkObject object : detailedObject.getBlobs().getObjects()) {
                    if (object.getPhysicalPlacement() != null) {
                        if (!Guard.isNullOrEmpty(object.getPhysicalPlacement().getTapes())) {
                            for (final Tape tape : object.getPhysicalPlacement().getTapes()) {
                                final String[] bucketArray = new String[this.columnCount];
                                bucketArray[0] = nullGuard(detailedObject.getName());
                                bucketArray[1] = nullGuardToString(detailedObject.getBucketId());
                                bucketArray[2] = nullGuardToString(detailedObject.getOwner());
                                bucketArray[3] = nullGuardToString(detailedObject.getSize());
                                bucketArray[4] = nullGuardToString(detailedObject.getType());
                                bucketArray[5] = nullGuardToDate(detailedObject.getCreationDate(), DATE_FORMAT);
                                bucketArray[6] = nullGuard(tape.getBarCode());
                                bucketArray[7] = nullGuardToString(tape.getState());
                                formatArray.add(bucketArray);
                                lineCount++;
                            } // for tapes
                        } // if tapes
                    } // if physical placement
                } // for objects
            } // if
        } // for DetailedS3Object
        final String[][] ret = new String[lineCount][this.columnCount];
        return formatArray.toArray(ret);
    }

}
