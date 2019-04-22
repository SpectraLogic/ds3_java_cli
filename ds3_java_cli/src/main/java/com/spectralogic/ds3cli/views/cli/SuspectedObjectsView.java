/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.models.SuspectedObjectResult;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.Pool;
import com.spectralogic.ds3client.models.Tape;
import com.spectralogic.ds3client.utils.Guard;

import java.util.List;

import static com.spectralogic.ds3cli.util.Guard.nullGuard;
import static com.spectralogic.ds3cli.util.Guard.nullGuardToString;

public class SuspectedObjectsView extends TableView<SuspectedObjectResult> {
    private ImmutableList<BulkObject> suspectBlobTapes;

    @Override
    public String render(final SuspectedObjectResult obj) {
        final ImmutableList<BulkObject> bulkObjects = obj.getResult();
        if (bulkObjects == null || Iterables.isEmpty(bulkObjects)) {
            return "No suspected blobs returned";
        }

        suspectBlobTapes = bulkObjects;
        initTable(ImmutableList.of("Name", "Bucket", "In Cache", "Offset", "Length"));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    @Override
    protected String[][] formatTableContents() {
        final String [][] formatArray = new String[suspectBlobTapes.size()][];
        int i = 0;
        for (final BulkObject suspectBlobTape : this.suspectBlobTapes) {
            final String [] bucketArray = new String[this.columnCount];
            bucketArray[0] = nullGuard(suspectBlobTape.getName());
            bucketArray[1] = nullGuard(getBucketFromBulkObject(suspectBlobTape));
            bucketArray[2] = nullGuardToString(suspectBlobTape.getInCache());
            bucketArray[3] = nullGuardToString(suspectBlobTape.getOffset());
            bucketArray[4] = nullGuardToString(suspectBlobTape.getLength());
            formatArray[i++] = bucketArray;
        }
        return formatArray;
    }

    /**
     * Return the bucket name, if is exists, if not search for a bucket Id to use across the tapes
     * and pools that an object exists in.
     */
    private String getBucketFromBulkObject(final BulkObject suspectBlobTape) {
        if (suspectBlobTape.getBucket() == null) {
            // get the bucket id if one exists

            final List<Tape> tapes = suspectBlobTape.getPhysicalPlacement().getTapes();
            if (!Guard.isNullOrEmpty(tapes)) {
                for (final Tape tape : tapes) {
                    if (tape.getBucketId() != null) return tape.getBucketId().toString();
                }
            }
            final List<Pool> pools = suspectBlobTape.getPhysicalPlacement().getPools();
            if (!Guard.isNullOrEmpty(pools)) {
                for (final Pool pool : pools) {
                    if (pool.getBucketId() != null) return pool.getBucketId().toString();
                }
            }
        }
        return suspectBlobTape.getBucket();
    }
}
