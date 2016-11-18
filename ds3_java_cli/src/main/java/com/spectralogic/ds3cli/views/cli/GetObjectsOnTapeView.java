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
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.models.GetObjectsOnTapeResult;
import com.spectralogic.ds3cli.util.Guard;
import com.spectralogic.ds3client.models.BulkObject;

import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.util.Guard.nullGuard;

public class GetObjectsOnTapeView extends TableView<GetObjectsOnTapeResult> {

    private Iterable<BulkObject> objectIterable;

    @Override
    public String render(final GetObjectsOnTapeResult blobsResult) {
        final Iterable<BulkObject> bulkObjects = blobsResult.getResult();
        if (null == bulkObjects || Iterables.isEmpty(bulkObjects)) {
            return "No objects were reported in tape '" + blobsResult.getTapeId() + "'";
        }
        this.objectIterable = bulkObjects;

        initTable(ImmutableList.of("Name", "Bucket", "Size", "Id"));
        setTableDataAlignment(ImmutableList.of(ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT));
        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final List<String[]> contents = new ArrayList<>();

        for (final BulkObject content : objectIterable) {
            final String[] arrayEntry = new String[this.columnCount];
            arrayEntry[0] = nullGuard(content.getName());
            arrayEntry[1] = nullGuard(content.getBucket());
            arrayEntry[2] = Guard.nullGuardToString(content.getLength());
            arrayEntry[3] = Guard.nullGuardToString(content.getId());
            contents.add(arrayEntry);
        }

        return contents.toArray(new String[contents.size()][]);
    }

}
