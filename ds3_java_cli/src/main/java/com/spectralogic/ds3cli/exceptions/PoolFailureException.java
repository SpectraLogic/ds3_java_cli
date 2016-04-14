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

package com.spectralogic.ds3cli.exceptions;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.spectralogic.ds3client.models.DetailedTapeFailure;
import com.spectralogic.ds3client.models.PoolFailure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class PoolFailureException extends Exception {

    public PoolFailureException(final Iterator<PoolFailure> poolFailures) {
        super(BuildFailureMessage(poolFailures));
    }

    private static String BuildFailureMessage(final Iterator<PoolFailure> poolFailures) {
        return String.format("There are pool failures found in BlackPearl\n%sTo ignore this error use --force",
                ASCIITable.getInstance().getTable(getHeaders(), formatFailureList(poolFailures)));
    }

    private static ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Pool ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Date", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Error Type", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Error Message", ASCIITable.ALIGN_LEFT),
        };
    }

    private static String[][] formatFailureList(final Iterator<PoolFailure> iterator) {
        final List<String[]> contents = new ArrayList<>();

        while (iterator.hasNext()) {

            final PoolFailure poolFailures = iterator.next();
            final String[] arrayEntry = new String[5];
            arrayEntry[0] = nullGuard(poolFailures.getId().toString());
            arrayEntry[1] = nullGuard(poolFailures.getPoolId().toString());
            arrayEntry[2] = nullGuard(poolFailures.getDate().toString());
            arrayEntry[3] = nullGuard(poolFailures.getType().toString());
            arrayEntry[4] = nullGuard(poolFailures.getErrorMessage());

            contents.add(arrayEntry);
        }

        return contents.toArray(new String[contents.size()][]);
    }
}
