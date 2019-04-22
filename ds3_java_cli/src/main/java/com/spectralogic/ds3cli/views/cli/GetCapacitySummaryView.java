/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.util.Guard;
import com.spectralogic.ds3client.models.CapacitySummaryContainer;
import com.spectralogic.ds3client.models.StorageDomainCapacitySummary;

public class GetCapacitySummaryView extends TableView<GetCapacitySummaryResult> {

    private StorageDomainCapacitySummary poolCapacity;
    private StorageDomainCapacitySummary tapeCapacity;

    @Override
    public String render(final GetCapacitySummaryResult obj) {
        final CapacitySummaryContainer result = obj.getResult();
        if (result == null){
            return "No Capacity Summary result returned.";
        }
        poolCapacity = result.getPool();
        tapeCapacity = result.getTape();

        initTable(ImmutableList.of(" Container ", "Allocated", "Used", "Free"));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        // Pool and Tape
        final String [][] formatArray = new String[2][];

        formatArray[0] = createDomainCapacitySummaryEntry(poolCapacity, "Pool");
        formatArray[1] = createDomainCapacitySummaryEntry(tapeCapacity, "Tape");

        return formatArray;
    }

    private String[] createDomainCapacitySummaryEntry(final StorageDomainCapacitySummary capacitySummary, final String name) {
        final String [] capacitySummaryArray = new String[this.columnCount];
        capacitySummaryArray[0] = name;
        if(capacitySummary != null) {
            capacitySummaryArray[1] = Guard.nullGuardToString(capacitySummary.getPhysicalAllocated());
            capacitySummaryArray[2] = Guard.nullGuardToString(capacitySummary.getPhysicalUsed());
            capacitySummaryArray[3] = Guard.nullGuardToString(capacitySummary.getPhysicalFree());
        } else {
            capacitySummaryArray[1] = "N/A";
            capacitySummaryArray[2] = "N/A";
            capacitySummaryArray[3] = "N/A";
        }
        return capacitySummaryArray;
    }
}

