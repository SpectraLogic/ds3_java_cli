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
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetCapacitySummaryResult;
import com.spectralogic.ds3client.models.CapacitySummaryContainer;
import com.spectralogic.ds3client.models.StorageDomainCapacitySummary;

import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class GetCapacitySummaryView extends TableView<GetCapacitySummaryResult> {

    private StorageDomainCapacitySummary poolCapacity;
    private StorageDomainCapacitySummary tapeCapacity;

    @Override
    public String render(final GetCapacitySummaryResult obj) {
        final CapacitySummaryContainer result = obj.getResult();
        if (result == null){
            return "No Capacity Summary result treturned.";
        }
        poolCapacity = result.getPool();
        tapeCapacity = result.getTape();

        initTable(ImmutableList.of(" Container ", "Allocated", "Used", "Free"));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        // Pool and Tape
        final String [][] formatArray = new String[2][];
        final String [] poolArray = new String[this.columnCount];
        poolArray[0] = "Pool";
        if(poolCapacity != null) {
            poolArray[1] = nullGuardToString(poolCapacity.getPhysicalAllocated());
            poolArray[2] = nullGuardToString(poolCapacity.getPhysicalUsed());
            poolArray[3] = nullGuardToString(poolCapacity.getPhysicalFree());
        } else {
            poolArray[1] = "N/A";
            poolArray[2] = "N/A";
            poolArray[3] = "N/A";
        }
        formatArray[0] = poolArray;

        final String [] tapeArray = new String[this.columnCount];
        tapeArray[0] = "Tape";
        if(tapeCapacity != null) {
            tapeArray[1] = nullGuardToString(tapeCapacity.getPhysicalAllocated());
            tapeArray[2] = nullGuardToString(tapeCapacity.getPhysicalUsed());
            tapeArray[3] = nullGuardToString(tapeCapacity.getPhysicalFree());
        } else {
            tapeArray[1] = "N/A";
            tapeArray[2] = "N/A";
            tapeArray[3] = "N/A";
        }
        formatArray[1] = tapeArray;

        return formatArray;
    }
}

