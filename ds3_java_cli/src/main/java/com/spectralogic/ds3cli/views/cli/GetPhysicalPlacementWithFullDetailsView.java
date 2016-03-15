/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetPhysicalPlacementWithFullDetailsResult;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.BulkObjectList;
import com.spectralogic.ds3client.models.Pool;
import com.spectralogic.ds3client.models.Tape;

import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class GetPhysicalPlacementWithFullDetailsView  implements View<GetPhysicalPlacementWithFullDetailsResult> {

    @Override
    public String render(final GetPhysicalPlacementWithFullDetailsResult obj) throws JsonProcessingException {
        final BulkObjectList result = obj.getPhysicalPlacementWithDetails();
        if ((result == null) || (null == result.getObjects())){
            return "Object not found.";
        }


        return renderBulkObjectList(result);
    }

    private String renderBulkObjectList(final BulkObjectList bulkObjectList) {
        String output = "";

        for (int index = 0; index < bulkObjectList.getObjects().size(); index++) {
            output = output.concat(ASCIITable.getInstance().getTable(getBulkObjectHeaders(),
                    formatBulkObjectList(bulkObjectList.getObjects().get(index))));

            if (bulkObjectList.getObjects().get(index).getPhysicalPlacement().getPools() != null) {
                output = output.concat(ASCIITable.getInstance().getTable(getPoolsPlacementHeaders(),
                        formatPoolsPlacement(bulkObjectList.getObjects().get(index).getPhysicalPlacement().getPools())));
            }

            if (bulkObjectList.getObjects().get(index).getPhysicalPlacement().getTapes() != null) {
                output = output.concat(ASCIITable.getInstance().getTable(getTapesPlacementHeaders(),
                        formatTapesPlacement(bulkObjectList.getObjects().get(index).getPhysicalPlacement().getTapes())));
            }
        }

        return output;
    }

    private String[][] formatBulkObjectList(final BulkObject obj) {
        final String [][] formatArray = new String[1][];

            final String[] bulkObjectArray = new String[7];
            bulkObjectArray[0] = nullGuard(obj.getName());
            bulkObjectArray[1] = nullGuard(obj.getId() != null ? obj.getId().toString() : "");
            bulkObjectArray[2] = nullGuard(obj.getInCache() != null ? obj.getInCache().toString() : "Unknown");
            bulkObjectArray[3] = nullGuard(Long.toString(obj.getLength()));
            bulkObjectArray[4] = nullGuard(Long.toString(obj.getOffset()));
            bulkObjectArray[5] = nullGuard(Boolean.toString(obj.getLatest()));
            bulkObjectArray[6] = nullGuard(Long.toString(obj.getVersion()));
            formatArray[0] = bulkObjectArray;

        return formatArray;
    }

    private ASCIITableHeader[] getBulkObjectHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Object Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("In Cache", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Length", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Offset", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Latest", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Version", ASCIITable.ALIGN_LEFT)
        };
    }

    private String[][] formatPoolsPlacement(final List<Pool> poolsList) {
        final String [][] formatArray = new String[poolsList.size()][];

        for (int i = 0; i < poolsList.size(); i ++) {
            final Pool pool = poolsList.get(i);
            final String[] poolPlacementArray = new String[7];
            poolPlacementArray[0] = nullGuard(pool.getName());
            poolPlacementArray[1] = nullGuard(pool.getId().toString());
            poolPlacementArray[2] = nullGuard(pool.getBucketId() != null ? pool.getBucketId().toString() : "");
            poolPlacementArray[3] = nullGuard(pool.getState().toString());
            poolPlacementArray[4] = nullGuard(pool.getHealth().toString());
            poolPlacementArray[5] = nullGuard(pool.getType().toString());
            poolPlacementArray[6] = nullGuard(pool.getPartitionId() != null ? pool.getPartitionId().toString() : "");
            formatArray[i] = poolPlacementArray;
        }

        return formatArray;
    }

    private ASCIITableHeader[] getPoolsPlacementHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Pool Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Bucket ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("State", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Health", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Type", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Partition ID", ASCIITable.ALIGN_LEFT)
        };
    }

    private String[][] formatTapesPlacement(final List<Tape> tapesList) {
        final String [][] formatArray = new String[tapesList.size()][];

        for (int i = 0; i < tapesList.size(); i ++) {
            final Tape tape = tapesList.get(i);
            final String[] tapePlacementArray = new String[6];
            tapePlacementArray[0] = nullGuard(tape.getBarCode());
            tapePlacementArray[1] = nullGuard(tape.getState().toString());
            tapePlacementArray[2] = nullGuard(tape.getType().toString());
            tapePlacementArray[3] = nullGuard(tape.getDescriptionForIdentification());
            tapePlacementArray[4] = nullGuard(tape.getEjectLabel());
            tapePlacementArray[5] = nullGuard(tape.getEjectLocation());

            formatArray[i] = tapePlacementArray;
        }

        return formatArray;
    }

    private ASCIITableHeader[] getTapesPlacementHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Tape Bar Code", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("State", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Type", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Description", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Eject Label", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Eject Location", ASCIITable.ALIGN_LEFT)
        };
    }
}
