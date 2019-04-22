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
import com.spectralogic.ds3cli.models.SuspectBlobTapesResult;
import com.spectralogic.ds3client.models.SuspectBlobTape;
import com.spectralogic.ds3client.utils.Guard;

import java.util.List;

import static com.spectralogic.ds3cli.util.Guard.nullGuardToString;

public class GetSuspectBlobTapesView extends TableView<SuspectBlobTapesResult> {

    private List<SuspectBlobTape> suspectBlobTapeList;

    @Override
    public String render(final SuspectBlobTapesResult obj) {
        final List<SuspectBlobTape> suspectBlobTapes = obj.getResult();
        if (Guard.isNullOrEmpty(suspectBlobTapes)) {
            return "No Suspect Blob Tapes reported";
        }
        suspectBlobTapeList = suspectBlobTapes;
        initTable(ImmutableList.of("ID", "Blob ID", "Tape ID", "Order Index"));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    @Override
    protected String[][] formatTableContents() {

        final int numSuspectTapeBlobs = this.suspectBlobTapeList.size();
        final String[][] suspectTable = new String[numSuspectTapeBlobs][];
        SuspectBlobTape suspectBlobTape;
        for (int i = 0; i < numSuspectTapeBlobs; i++) {
            suspectBlobTape = this.suspectBlobTapeList.get(i);
            final String [] suspectRow = new String[this.columnCount];
            suspectRow[0] = nullGuardToString(suspectBlobTape.getId());
            suspectRow[1] = nullGuardToString(suspectBlobTape.getBlobId());
            suspectRow[2] = nullGuardToString(suspectBlobTape.getTapeId());
            suspectRow[3] = nullGuardToString(suspectBlobTape.getOrderIndex());
            suspectTable[i] = suspectRow;
        }

        return suspectTable;
    }
}
