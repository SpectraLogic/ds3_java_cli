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
import com.spectralogic.ds3client.models.Bucket;
import com.spectralogic.ds3client.models.ListAllMyBucketsResult;

import java.util.List;

public class GetServiceView implements View<ListAllMyBucketsResult> {
    @Override
    public String render(final ListAllMyBucketsResult obj) {
        if( (obj == null) || (null == obj.getBuckets()) ){
            return "You do not have any buckets";
        }

        return "Owner: " + obj.getOwner().getDisplayName() + "\n" +
            ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(obj));
    }

    private String[][] formatBucketList(final ListAllMyBucketsResult result) {
        final List<Bucket> buckets = result.getBuckets();
        final String [][] formatArray = new String[buckets.size()][];
        for(int i = 0; i < buckets.size(); i ++) {
            final Bucket bucket = buckets.get(i);
            final String [] bucketArray = new String[2];
            bucketArray[0] = bucket.getName();
            bucketArray[1] = bucket.getCreationDate();
            formatArray[i] = bucketArray;
        }
        return formatArray;
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Bucket Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Creation Date", ASCIITable.ALIGN_RIGHT)
        };
    }
}
