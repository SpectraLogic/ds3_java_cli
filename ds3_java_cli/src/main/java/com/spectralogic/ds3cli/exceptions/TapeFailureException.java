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

package com.spectralogic.ds3cli.exceptions;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.spectralogic.ds3client.models.DetailedTapeFailure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class TapeFailureException extends Exception {

    public TapeFailureException(final Iterator<DetailedTapeFailure> tapeFailures) {
        super(BuildTapeFailureMessage(tapeFailures));
    }

    private static String BuildTapeFailureMessage(final Iterator<DetailedTapeFailure> tapeFailures) {
        return String.format("There are tape failures found in BlackPearl\n%sTo ignore this error use --FORCE",
                ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(tapeFailures)));
    }

    private static ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Tape Drive ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Tape ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Date", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Error Type", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Error Message", ASCIITable.ALIGN_LEFT),
        };
    }

    private static String[][] formatBucketList(final Iterator<DetailedTapeFailure> iterator) {
        final List<String[]> contents = new ArrayList<>();

        while (iterator.hasNext()) {

            final DetailedTapeFailure tapeFailure = iterator.next();
            final String[] arrayEntry = new String[6];
            arrayEntry[0] = nullGuard(tapeFailure.getId().toString());
            arrayEntry[1] = nullGuard(tapeFailure.getTapeDriveId().toString());
            arrayEntry[2] = nullGuard(tapeFailure.getTapeId().toString());
            arrayEntry[3] = nullGuard(tapeFailure.getDate().toString());
            arrayEntry[4] = nullGuard(tapeFailure.getType().toString());
            arrayEntry[5] = nullGuard(tapeFailure.getErrorMessage());

            contents.add(arrayEntry);
        }

        return contents.toArray(new String[contents.size()][]);
    }
}
