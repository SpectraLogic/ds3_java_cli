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

package com.spectralogic.ds3cli.command;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetServiceRequest;
import com.spectralogic.ds3client.commands.GetServiceResponse;
import com.spectralogic.ds3client.models.Bucket;
import com.spectralogic.ds3client.models.ListAllMyBucketsResult;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;

public class GetService extends CliCommand {


    public GetService(Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public String call() throws IOException, SignatureException {
        final GetServiceResponse response = getClient().getService(new GetServiceRequest());
        final ListAllMyBucketsResult result = response.getResult();

        if(result.getBuckets() == null) {
            return "You do not have any buckets";
        }

        return "Owner: " + result.getOwner().getDisplayName() + "\n" +
                ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(result));
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
