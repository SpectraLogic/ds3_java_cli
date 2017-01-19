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
import com.spectralogic.ds3cli.models.GetServiceResult;
import com.spectralogic.ds3client.models.BucketDetails;
import com.spectralogic.ds3client.models.ListAllMyBucketsResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.spectralogic.ds3cli.utils.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.utils.Guard.nullGuard;
import static com.spectralogic.ds3cli.utils.Guard.nullGuardFromDate;

public class GetServiceView extends TableView<GetServiceResult> {
    private Iterator<BucketDetails> objectIterator;

    @Override
    public String render(final GetServiceResult obj) {
        final ListAllMyBucketsResult result = obj.getResult();
        if (result == null || null == result.getBuckets()) {
            return "You do not have any buckets";
        }
        this.objectIterator = result.getBuckets().iterator();

        initTable(ImmutableList.of("Bucket Name", "Creation Date"));

        return "Owner: " + result.getOwner().getDisplayName() + "\n" +
            ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final List<String[]> contents = new ArrayList<>();

        while(objectIterator.hasNext()) {

            final BucketDetails bucket = objectIterator.next();
            final String[] bucketArray = new String[this.columnCount];
            bucketArray[0] = nullGuard(bucket.getName());
            bucketArray[1] = nullGuardFromDate(bucket.getCreationDate(), DATE_FORMAT);
            contents.add(bucketArray);
        }
        return contents.toArray(new String[contents.size()][]);
    }

}
