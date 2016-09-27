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
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.spectralogic.ds3cli.util.Utils.*;

public class DetailedObjectsPhysicalView implements View<GetDetailedObjectsResult> {
    private ImmutableList<DetailedS3Object> objects;

    private static final List HEADERS = ImmutableList.of("Name", "Bucket", "Owner", "Size", "Type", "Creation Date", "Barcode", "State");
    private static final int COLUMN_COUNT = HEADERS.size();
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    @Override
    public String render(final GetDetailedObjectsResult obj) {
        if (obj == null || (obj.getObjIterator() == null) || !obj.getObjIterator().iterator().hasNext()) {
            return "No objects returned";
        }
        objects = ImmutableList.copyOf(obj.getObjIterator());
        return renderTable();
    }

    public String renderTable()  {
        final Appendable outs = new StringWriter();
        try {
            final CSVPrinter csv = new CSVPrinter(outs, CSVFormat.EXCEL);
            csv.printRecord(HEADERS);
            for (final DetailedS3Object object : objects) {
                for(String[] line : formatTableRows(object)) {
                    csv.printRecord((Object[])line);
                }
            }
            csv.flush();
            csv.close();
        } catch (final IOException e) {
            return "ERROR: Failed to create CSV output";
        }
        return outs.toString();
    }

    protected String[][] formatTableRows(final DetailedS3Object detailedObject) {
        // one line for each instance on tape -- mine down to get Physical Placement
        final List<String[]> formatArray = new  ArrayList<String[]>();
        int lineCount = 0;
        if((detailedObject.getBlobs() != null) && !Guard.isNullOrEmpty(detailedObject.getBlobs().getObjects())) {
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
        final String[][] ret = new String[lineCount][COLUMN_COUNT];
        return formatArray.toArray(ret);
    }

    private String[] writeLine(final Tape tape, final DetailedS3Object detailedObject) {
        final String[] tapeArray = new String[COLUMN_COUNT];
        tapeArray[0] = nullGuard(detailedObject.getName());
        tapeArray[1] = nullGuardToString(detailedObject.getBucketId());
        tapeArray[2] = nullGuardToString(detailedObject.getOwner());
        tapeArray[3] = nullGuardToString(detailedObject.getSize());
        tapeArray[4] = nullGuardToString(detailedObject.getType());
        tapeArray[5] = nullGuardToDate(detailedObject.getCreationDate(), DATE_FORMAT);
        tapeArray[6] = nullGuard(tape.getBarCode());
        tapeArray[7] = nullGuardToString(tape.getState());
        return tapeArray;
    }
}
