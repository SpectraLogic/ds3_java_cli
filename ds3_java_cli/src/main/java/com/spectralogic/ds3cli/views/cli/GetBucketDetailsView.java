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
import com.spectralogic.ds3cli.models.GetBucketResult;
import com.spectralogic.ds3client.models.Bucket;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Guard.nullGuard;
import static com.spectralogic.ds3cli.util.Guard.nullGuardToString;
import static com.spectralogic.ds3cli.util.Guard.nullGuardFromDate;

public class GetBucketDetailsView extends TableView<GetBucketResult> {

    private Bucket bucket;

    @Override
    public String render(final GetBucketResult result) {
        bucket = result.getBucket();
        if (null == bucket) {
            return "No details for bucket.";
        }
        initTable(ImmutableList.of("Name", "Id", "User Id", "Creation Date", "Data Policy Id", "Used Capacity"));
        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {

        final String [][] formatArray = new String[1][];
        final String[] arrayEntry = new String[this.columnCount];
        arrayEntry[0] = nullGuard(bucket.getName());
        arrayEntry[1] = nullGuardToString(bucket.getId());
        arrayEntry[2] = nullGuardToString(bucket.getUserId());
        arrayEntry[3] = nullGuardFromDate(bucket.getCreationDate(), DATE_FORMAT);
        arrayEntry[4] = nullGuardToString(bucket.getDataPolicyId());
        arrayEntry[5] = nullGuardToString(bucket.getLogicalUsedCapacity());
        formatArray[0] = arrayEntry;
        return formatArray;
    }

}
