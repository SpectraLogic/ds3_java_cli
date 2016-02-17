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
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetTapesResult;
import com.spectralogic.ds3client.models.Tape;
import com.spectralogic.ds3client.models.TapeList;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class GetTapesView implements View<GetTapesResult> {
    @Override
    public String render(final GetTapesResult obj) throws JsonProcessingException {
        final TapeList result = obj.getTapes();
        if ((result == null) || (null == result.getTape())) {
            return "You do not have any tapes";
        }

        return ASCIITable.getInstance().getTable(getHeaders(), formatTapeList(result));
    }

    private String[][] formatTapeList(final TapeList tapeList) {
        final String [][] formatArray = new String[tapeList.getTape().size()][];
        for (int i = 0; i < tapeList.getTape().size(); i ++) {
            final Tape tape = tapeList.getTape().get(i);
            final String [] bucketArray = new String[5];
            bucketArray[0] = nullGuard(tape.getBarCode());
            bucketArray[1] = nullGuard(tape.getId().toString());
            bucketArray[2] = nullGuard(tape.getState().toString());
            bucketArray[3] = nullGuard(tape.getLastModified().toString());
            bucketArray[4] = nullGuard(Long.toString(tape.getAvailableRawCapacity()));
            formatArray[i] = bucketArray;
        }
        return formatArray;
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Bar Code", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("State", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Last Modified", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Available Raw Capacity", ASCIITable.ALIGN_LEFT),
        };
    }
}
