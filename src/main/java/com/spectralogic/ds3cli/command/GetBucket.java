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
import com.spectralogic.ds3client.commands.GetBucketRequest;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.ListBucketResult;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;

import java.util.List;

public class GetBucket extends CliCommand {
    private String bucketName;
    public GetBucket(final Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The get bucket command requires '-b' to be set.");
        }
        return this;
    }

    @Override
    public String call() throws Exception {

        try {
            final ListBucketResult fileList = getClient().getBucket(new GetBucketRequest(bucketName)).getResult();
            if(fileList.getContentsList() == null) {
                return "No objects were reported in the bucket.";
            }
            else {
                return ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(fileList));
            }
        }
        catch(final FailedRequestException e) {
            if(e.getStatusCode() == 500) {
                return "Error: Cannot communicate with the remote DS3 appliance.";
            }
            else if(e.getStatusCode() == 404) {
                return "Error: Unknown bucket.";
            }
            else {
                return "Error: Encountered an unknown error of ("+ e.getStatusCode() +") while accessing the remote DS3 appliance.";
            }
        }
    }

    private String[][] formatBucketList(final ListBucketResult listBucketResult) {
        final List<Contents> contentList = listBucketResult.getContentsList();
        final String[][] formatArray = new String[contentList.size()][];

        for(int i = 0; i < contentList.size(); i++) {
            final Contents content = contentList.get(i);
            final String[] arrayEntry = new String[5];
            arrayEntry[0] = content.getKey();
            arrayEntry[1] = Integer.toString(content.getSize());
            arrayEntry[2] = content.getOwner().getDisplayName();
            arrayEntry[3] = nullGuard(content.getLastModified());
            arrayEntry[4] = nullGuard(content.geteTag());
            formatArray[i] = arrayEntry;
        }

        return formatArray;
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("File Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Size", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Owner", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Last Modified", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("ETag", ASCIITable.ALIGN_RIGHT)};
    }

    private String nullGuard(String message) {
        if(message == null) {
            return "N/A";
        }
        return message;
    }
}
