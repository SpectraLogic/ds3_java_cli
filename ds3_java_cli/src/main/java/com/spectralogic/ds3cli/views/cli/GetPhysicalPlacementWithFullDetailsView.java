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

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetPhysicalPlacementWithFullDetailsResult;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.utils.Guard;

import java.util.List;

import static com.spectralogic.ds3cli.util.Guard.nullGuard;
import static com.spectralogic.ds3cli.util.Guard.nullGuardToString;

public class GetPhysicalPlacementWithFullDetailsView  implements View<GetPhysicalPlacementWithFullDetailsResult> {

    @Override
    public String render(final GetPhysicalPlacementWithFullDetailsResult obj) throws JsonProcessingException {
        final BulkObjectList result = obj.getResult();
        if (result == null || null == result.getObjects()) {
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

            if (!Guard.isNullOrEmpty(bulkObjectList.getObjects().get(index).getPhysicalPlacement().getAzureTargets())) {
                output = output.concat(ASCIITable.getInstance().getTable(getAzureTargetPlacementHeaders(),
                        formatAzureTargetPlacement(bulkObjectList.getObjects().get(index).getPhysicalPlacement().getAzureTargets())));
            }

            if (!Guard.isNullOrEmpty(bulkObjectList.getObjects().get(index).getPhysicalPlacement().getS3Targets())) {
                output = output.concat(ASCIITable.getInstance().getTable(getS3TargetPlacementHeaders(),
                        formatS3TargetPlacement(bulkObjectList.getObjects().get(index).getPhysicalPlacement().getS3Targets())));
            }
        }

        return output;
    }

    private String[][] formatBulkObjectList(final BulkObject obj) {
        final String [][] formatArray = new String[1][];

            final String[] bulkObjectArray = new String[getBulkObjectHeaders().length];
            bulkObjectArray[0] = nullGuard(obj.getName());
            bulkObjectArray[1] = nullGuardToString(obj.getId(), "");
            bulkObjectArray[2] = nullGuardToString(obj.getInCache(), "Unknown");
            bulkObjectArray[3] = nullGuardToString(obj.getLength());
            bulkObjectArray[4] = nullGuardToString(obj.getOffset());
            bulkObjectArray[5] = nullGuardToString(obj.getLatest());
            bulkObjectArray[6] = nullGuardToString(obj.getVersion());
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
            final String[] poolPlacementArray = new String[getPoolsPlacementHeaders().length];
            poolPlacementArray[0] = nullGuard(pool.getName());
            poolPlacementArray[1] = nullGuardToString(pool.getId());
            poolPlacementArray[2] = nullGuardToString(pool.getBucketId(), "");
            poolPlacementArray[3] = nullGuardToString(pool.getState());
            poolPlacementArray[4] = nullGuardToString(pool.getHealth());
            poolPlacementArray[5] = nullGuardToString(pool.getType());
            poolPlacementArray[6] = nullGuardToString(pool.getPartitionId(), "");
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
            final String[] tapePlacementArray = new String[getS3TargetPlacementHeaders().length];
            tapePlacementArray[0] = nullGuard(tape.getBarCode());
            tapePlacementArray[1] = nullGuardToString(tape.getState());
            tapePlacementArray[2] = nullGuardToString(tape.getType());
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

    private String[][] formatS3TargetPlacement(final List<S3Target> s3TargetList) {
        final String [][] formatArray = new String[s3TargetList.size()][];

        for (int i = 0; i < s3TargetList.size(); i ++) {
            final S3Target target = s3TargetList.get(i);
            final String[] tapePlacementArray = new String[getS3TargetPlacementHeaders().length];
            tapePlacementArray[0] = nullGuard(target.getName());
            tapePlacementArray[1] = nullGuardToString(target.getState());
            tapePlacementArray[2] = nullGuardToString(target.getId());
            tapePlacementArray[3] = nullGuardToString(target.getQuiesced());
            tapePlacementArray[4] = nullGuardToString(target.getRegion());
            tapePlacementArray[5] = nullGuard(target.getDataPathEndPoint());

            formatArray[i] = tapePlacementArray;
        }
        return formatArray;
    }

    private ASCIITableHeader[] getS3TargetPlacementHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("AWS S3 Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("State", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Id", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Quiesced", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Region", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Data Path Endpoint", ASCIITable.ALIGN_LEFT)
        };
    }

    private String[][] formatAzureTargetPlacement(final List<AzureTarget> azureTargetList) {
        final String [][] formatArray = new String[azureTargetList.size()][];

        for (int i = 0; i < azureTargetList.size(); i ++) {
            final AzureTarget target = azureTargetList.get(i);
            final String[] tapePlacementArray = new String[getAzureTargetPlacementHeaders().length];
            tapePlacementArray[0] = nullGuard(target.getName());
            tapePlacementArray[1] = nullGuardToString(target.getState());
            tapePlacementArray[2] = nullGuardToString(target.getId());
            tapePlacementArray[3] = nullGuardToString(target.getQuiesced());

            formatArray[i] = tapePlacementArray;
        }
        return formatArray;
    }

    private ASCIITableHeader[] getAzureTargetPlacementHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Azure Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("State", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Id", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Quiesced", ASCIITable.ALIGN_LEFT),
        };
    }

}
