/*
 * ******************************************************************************
 *   Copyright 2014 - 2016 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.models.VerifyBulkJobResult;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.Objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class VerifyBulkJobView extends TableView<VerifyBulkJobResult> {

    protected Iterator<Objects> objectsIterator;

    @Override
    public String render(final VerifyBulkJobResult verifyResult) {
        if ((null == verifyResult.getObjIterator()) || !verifyResult.getObjIterator().hasNext()) {
            return "No objects were reported in tape '" + verifyResult.getBucketId() + "'";
        }

        this.objectsIterator = verifyResult.getObjIterator();

        initTable(ImmutableList.of("Chunk", "Name", "Size", "Version"));
        setTableDataAlignment(ImmutableList.of(ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT));
        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final List<String[]> contents = new ArrayList<>();

        while(this.objectsIterator.hasNext()) {
            final Objects content = this.objectsIterator.next();
            final int chunk = content.getChunkNumber();
            final List<BulkObject> bulkObjectList = content.getObjects();
            for (final BulkObject bulkObject : bulkObjectList) {
                final String[] arrayEntry = new String[this.columnCount];
                arrayEntry[0] = nullGuardToString(chunk);
                arrayEntry[1] = nullGuard(bulkObject.getName());
                arrayEntry[2] = nullGuardToString(bulkObject.getLength());
                arrayEntry[3] = nullGuardToString(bulkObject.getVersion());
                contents.add(arrayEntry);
            }
        }
        return contents.toArray(new String[contents.size()][]);
    }
}
