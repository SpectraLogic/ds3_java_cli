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
import com.spectralogic.ds3cli.models.GetTapesResult;
import com.spectralogic.ds3client.models.Tape;
import com.spectralogic.ds3client.models.TapeList;
import com.spectralogic.ds3client.utils.Guard;

import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class GetTapesView extends TableView<GetTapesResult> {

    protected List<Tape> tapeList;

    @Override
    public String render(final GetTapesResult obj) {
        final TapeList result = obj.getTapes();
        if ((result == null) || (Guard.isNullOrEmpty(result.getTapes())) ){
            return "You do not have any tapes";
        }
        this.tapeList = result.getTapes();

        initTable(ImmutableList.of("Bar Code", "ID", "State", "Last Modified", "Available Raw Capacity", "BucketID", "Assigned to Storage Domain" ));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final String [][] formatArray = new String[this.tapeList.size()][];
        int i = 0;
        for (final Tape tape : this.tapeList) {
            final String [] bucketArray = new String[this.columnCount];
            bucketArray[0] = nullGuard(tape.getBarCode());
            bucketArray[1] = nullGuardToString(tape.getId());
            bucketArray[2] = nullGuardToString(tape.getState());
            bucketArray[3] = nullGuardToDate(tape.getLastModified(),DATE_FORMAT);
            bucketArray[4] = nullGuardToString(tape.getAvailableRawCapacity());
            bucketArray[5] = nullGuardToString(tape.getBucketId());
            bucketArray[6] = nullGuardToString(tape.getAssignedToStorageDomain());
            formatArray[i++] = (bucketArray);
        }
        return formatArray;
    }
}

