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
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3cli.util.Guard;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.MasterObjectList;
import com.spectralogic.ds3client.models.Objects;

import java.util.ArrayList;
import java.util.Iterator;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Guard.nullGuard;
import static com.spectralogic.ds3cli.util.Guard.nullGuardFromDate;
import static com.spectralogic.ds3cli.util.Guard.nullGuardToString;

public class GetJobView extends TableView<GetJobResult> {

    private Iterator<Objects> objectsIterator;

    @Override
    public String render(final GetJobResult obj) {

        final MasterObjectList mol = obj.getResult();

        /// DATE_FORMAT is used before initTable() -- Set UTC
        final String returnString = String.format(
                "JobId: %s | Name: %s | Status: %s | Bucket: %s | Type: %s | Priority: %s | User Name: %s | Creation Date: %s | Total Size: %s | Total Transferred: %s",
                nullGuardToString(mol.getJobId()), nullGuardToString(mol.getName()), nullGuardToString(mol.getStatus()), nullGuard(mol.getBucketName()),
                nullGuardToString(mol.getRequestType()), nullGuardToString(mol.getPriority()), nullGuardToString(mol.getUserName()),
                nullGuardFromDate(mol.getStartDate(), DATE_FORMAT), nullGuardToString(mol.getOriginalSizeInBytes()),
                nullGuardToString(mol.getCompletedSizeInBytes()));

        if (mol.getObjects() == null || mol.getObjects().isEmpty()) {
            return returnString;
        }
        this.objectsIterator = mol.getObjects().iterator();

        initTable(ImmutableList.of("File Name", "Size", "In Cache", "Chunk Number", "Chunk ID"));

        return returnString + "\n" + ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final ArrayList<String[]> contents = new ArrayList<>();

        while (this.objectsIterator.hasNext()) {
            final Objects chunk = this.objectsIterator.next();

            for (final BulkObject obj : chunk.getObjects()) {
                final String[] arrayEntry = new String[this.columnCount];
                arrayEntry[0] = nullGuard(obj.getName());
                arrayEntry[1] = nullGuardToString(obj.getLength());
                arrayEntry[2] = nullGuardToString(obj.getInCache());
                arrayEntry[3] = nullGuardToString(chunk.getChunkNumber());
                arrayEntry[4] = nullGuardToString(chunk.getChunkId());
                contents.add(arrayEntry);
            }
        }
        return contents.toArray(new String[contents.size()][]);
    }
}
