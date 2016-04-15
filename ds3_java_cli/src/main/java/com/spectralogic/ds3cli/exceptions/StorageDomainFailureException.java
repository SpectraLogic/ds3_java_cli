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
import com.spectralogic.ds3client.models.StorageDomainFailure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class StorageDomainFailureException extends Exception {

    public StorageDomainFailureException(final Iterator<StorageDomainFailure> storageFailures) {
        super(BuildFailureMessage(storageFailures));
    }

    private static String BuildFailureMessage(final Iterator<StorageDomainFailure> storageFailures) {
        return String.format("There are storage domain failures found in BlackPearl\n%sTo ignore this error use --force",
                ASCIITable.getInstance().getTable(getHeaders(), formatFailureList(storageFailures)));
    }

    private static ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Storage Domain ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Tape ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Date", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Error Type", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Error Message", ASCIITable.ALIGN_LEFT),
        };
    }

    private static String[][] formatFailureList(final Iterator<StorageDomainFailure> iterator) {
        final List<String[]> contents = new ArrayList<>();

        while (iterator.hasNext()) {

            final StorageDomainFailure storageFailures = iterator.next();
            final String[] arrayEntry = new String[5];
            arrayEntry[0] = nullGuard(storageFailures.getId().toString());
            arrayEntry[1] = nullGuard(storageFailures.getStorageDomainId().toString());
            arrayEntry[2] = nullGuard(storageFailures.getDate().toString());
            arrayEntry[3] = nullGuard(storageFailures.getType().toString());
            arrayEntry[4] = nullGuard(storageFailures.getErrorMessage());

            contents.add(arrayEntry);
        }

        return contents.toArray(new String[contents.size()][]);
    }
}
