/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetTapesWithFullDetailsResult;
import com.spectralogic.ds3client.models.NamedDetailedTape;
import com.spectralogic.ds3client.models.NamedDetailedTapeList;
import com.spectralogic.ds3client.utils.Guard;

import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class GetTapesWithFullDetailsView extends TableView<GetTapesWithFullDetailsResult> {

    protected List<NamedDetailedTape> tapeList;

    @Override
    public String render(final GetTapesWithFullDetailsResult obj) {
        final NamedDetailedTapeList result = obj.getTapesWithDetails();
        if ((result == null) || (Guard.isNullOrEmpty(result.getNamedDetailedTapes())) ){
            return "You do not have any tapes";
        }
        this.tapeList = result.getNamedDetailedTapes();

        initTable(ImmutableList.of("Bar Code", "ID", "State", "Last Modified", "Available Raw Capacity", "Most Recent Failure" ));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final String [][] formatArray = new String[this.tapeList.size()][];
        int i = 0;
        for (final NamedDetailedTape tape : this.tapeList) {
            final String [] bucketArray = new String[this.columnCount];
            bucketArray[0] = nullGuard(tape.getBarCode());
            bucketArray[1] = nullGuardToString(tape.getId());
            bucketArray[2] = nullGuardToString(tape.getState());
            bucketArray[3] = nullGuardToString(tape.getLastModified(), "---");
            bucketArray[4] = nullGuardToString(tape.getAvailableRawCapacity());
            bucketArray[5] = nullGuardToString(tape.getMostRecentFailure());
            formatArray[i++] = (bucketArray);
        }
        return formatArray;
    }
}

