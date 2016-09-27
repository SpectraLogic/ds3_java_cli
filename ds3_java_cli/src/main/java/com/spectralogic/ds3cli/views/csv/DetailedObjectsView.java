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

import com.google.common.base.Joiner;
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

public class DetailedObjectsView implements View<GetDetailedObjectsResult> {

    private static final String TAPE_SEPARATOR = " | ";

    private Iterable<DetailedS3Object> objects;
    private static final List<String> HEADERS = ImmutableList.of("Name", "Bucket", "Owner", "Size", "Type", "Creation Date", "Tapes", "Pools");
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
        objects = obj.getObjIterator();
        return renderTable();
    }

    protected String[] formatTableRow(final DetailedS3Object detailedObject) {
        final String [] formatArray = new String[COLUMN_COUNT];
        formatArray[0] = nullGuard(detailedObject.getName());
        formatArray[1] = nullGuardToString(detailedObject.getBucketId());
        formatArray[2] = nullGuardToString(detailedObject.getOwner());
        formatArray[3] = nullGuardToString(detailedObject.getSize());
        formatArray[4] = nullGuardToString(detailedObject.getType());
        formatArray[5] = nullGuardToDate(detailedObject.getCreationDate(), DATE_FORMAT);
        formatArray[6] = concatenateTapes(detailedObject.getBlobs());
        formatArray[7] = concatenatePools(detailedObject.getBlobs());
        return formatArray;
    }

    public String renderTable()  {
        final Appendable outs = new StringWriter();
        try {
            final CSVPrinter csv = new CSVPrinter(outs, CSVFormat.EXCEL);
            csv.printRecord(HEADERS);
            for (final DetailedS3Object line : objects) {
                csv.printRecord((Object[])formatTableRow(line));
            }
            csv.flush();
            csv.close();
        } catch (final IOException e) {
            return "ERROR: Failed to create CSV output";
        }
        return outs.toString();
    }

    private String concatenateTapes(final BulkObjectList objects) {
        if(Guard.isNullOrEmpty(objects.getObjects())) {
            return "No Physical Placement";
        }
        final ArrayList<String> tapes = new ArrayList<String>();
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
        final ArrayList<String> pools = new ArrayList<String>();
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
