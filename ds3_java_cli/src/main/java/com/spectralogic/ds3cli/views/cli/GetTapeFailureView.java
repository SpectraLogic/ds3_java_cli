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
import com.spectralogic.ds3cli.models.GetTapeFailureResult;
import com.spectralogic.ds3client.models.DetailedTapeFailure;
import com.spectralogic.ds3client.models.DetailedTapeFailureList;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class GetTapeFailureView implements View<GetTapeFailureResult> {
    @Override
    public String render(final GetTapeFailureResult obj) {
        final DetailedTapeFailureList result = obj.getResult();
        if( (result == null)
                || (null == result.getDetailedTapeFailures())
                || (result.getDetailedTapeFailures().size() == 0) ) {
            return "No tape failures on remote appliance";
        }

        return "" + result.getDetailedTapeFailures().size() + " Tape Failures:\n" +
                ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(result));
    }

    private String[][] formatBucketList(final DetailedTapeFailureList result) {
        final List<DetailedTapeFailure> failures = result.getDetailedTapeFailures();
        final String [][] formatArray = new String[failures.size()][];
        int i = 0;
        for(final DetailedTapeFailure failure : failures) {
            final String [] bucketArray = new String[4];
            bucketArray[0] = nullGuard(failure.getType().toString());
            bucketArray[1] = nullGuard(failure.getErrorMessage());
            bucketArray[2] = nullGuard(failure.getId().toString());
            final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
            bucketArray[3] = DATE_FORMAT.format(failure.getDate());
            formatArray[i++] = bucketArray;
        }
        return formatArray;
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Failure Type", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Failure Message", ASCIITable.ALIGN_CENTER),
                new ASCIITableHeader("Id", ASCIITable.ALIGN_CENTER),
                new ASCIITableHeader("Failure Date", ASCIITable.ALIGN_RIGHT)
        };
    }
}
