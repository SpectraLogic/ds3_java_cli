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
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;

import java.util.ArrayList;
import java.util.Iterator;

public class GetBucket extends CliCommand<String> {
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
            final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(getClient());

            final Iterable<Contents> objects = helper.listObjects(bucketName);
            final Iterator<Contents> objIterator = objects.iterator();

            if(!objIterator.hasNext()) {
                return "No objects were reported in the bucket '" + bucketName + "'";
            }
            else {
                return ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(objIterator));
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

    private String[][] formatBucketList(final Iterator<Contents> iterator) {
        final ArrayList<String[]> contents = new ArrayList<>();

        while(iterator.hasNext()) {

            final Contents content = iterator.next();
            final String[] arrayEntry = new String[5];
            arrayEntry[0] = nullGuard(content.getKey());
            arrayEntry[1] = nullGuard(Long.toString(content.getSize()));
            arrayEntry[2] = nullGuard(content.getOwner().getDisplayName());
            arrayEntry[3] = nullGuard(content.getLastModified());
            arrayEntry[4] = nullGuard(content.geteTag());
            contents.add(arrayEntry);
        }

        return contents.toArray(new String[contents.size()][]);
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
