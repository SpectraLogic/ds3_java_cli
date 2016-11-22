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
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.DetailedS3Object;
import com.spectralogic.ds3client.models.Tape;
import com.spectralogic.ds3client.utils.Guard;

import java.util.ArrayList;

import static com.spectralogic.ds3cli.utils.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.utils.Guard.nullGuard;
import static com.spectralogic.ds3cli.utils.Guard.nullGuardFromDate;
import static com.spectralogic.ds3cli.utils.Guard.nullGuardToString;

public class DetailedObjectsPhysicalView extends TableView<GetDetailedObjectsResult> {
    private Iterable<DetailedS3Object> objects;

    @Override
    public String render(final GetDetailedObjectsResult obj) {
        final Iterable<DetailedS3Object> detailedS3Objects = obj.getResult();
        if (detailedS3Objects == null || Iterables.isEmpty(detailedS3Objects)) {
            return "No objects returned";
        }

        objects = detailedS3Objects;
        initTable(ImmutableList.of("Name", "Bucket", "Owner", "Size", "Type", "Creation Date", "Barcode", "State"));

        return renderTable();
    }

    @Override
    protected String[][] formatTableContents() {
        final ArrayList<String[]> formatArray = new ArrayList<>();
        int lineCount = 0;
        for (final DetailedS3Object detailedObject : this.objects) {
            // one line for each instance on tape -- mine down to get Physical Placement
            if(detailedObject.getBlobs() != null && !Guard.isNullOrEmpty(detailedObject.getBlobs().getObjects())) {
                for (final BulkObject object : detailedObject.getBlobs().getObjects()) {
                    if (object.getPhysicalPlacement() != null) {
                        if (!Guard.isNullOrEmpty(object.getPhysicalPlacement().getTapes())) {
                            for (final Tape tape : object.getPhysicalPlacement().getTapes()) {
                                formatArray.add(writeLine(tape, detailedObject));
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

    private String[] writeLine(final Tape tape, final DetailedS3Object detailedObject) {
        final String[] tapeArray = new String[this.columnCount];
        tapeArray[0] = nullGuard(detailedObject.getName());
        tapeArray[1] = nullGuardToString(detailedObject.getBucketId());
        tapeArray[2] = nullGuardToString(detailedObject.getOwner());
        tapeArray[3] = nullGuardToString(detailedObject.getSize());
        tapeArray[4] = nullGuardToString(detailedObject.getType());
        tapeArray[5] = nullGuardFromDate(detailedObject.getCreationDate(), DATE_FORMAT);
        tapeArray[6] = nullGuard(tape.getBarCode());
        tapeArray[7] = nullGuardToString(tape.getState());
        return tapeArray;
    }

}
