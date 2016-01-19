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

package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.command.PutBulk;
import com.spectralogic.ds3cli.models.PutBulkResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class PutBulkView implements View<PutBulkResult> {
    @Override
    public String render(final PutBulkResult result) {
        if (result.getIgnoredFiles() == null || result.getIgnoredFiles().isEmpty()) {
            return result.getResult();
        }

        return String.format("%s\n%s",
                result.getResult(),
                ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(result.getIgnoredFiles().iterator())));
    }

    private String[][] formatBucketList(final Iterator<PutBulk.IgnoreFile> iterator) {
        final List<String[]> contents = new ArrayList<>();

        while(iterator.hasNext()) {

            final PutBulk.IgnoreFile ignoreFile = iterator.next();
            final String[] arrayEntry = new String[2];
            arrayEntry[0] = nullGuard(ignoreFile.getPath());
            arrayEntry[1] = nullGuard(ignoreFile.getErrorMessage());
            contents.add(arrayEntry);
        }

        return contents.toArray(new String[contents.size()][]);
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Ignored File", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Reason", ASCIITable.ALIGN_LEFT)};
    }
}
