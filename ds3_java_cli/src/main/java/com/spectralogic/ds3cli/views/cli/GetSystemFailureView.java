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
import com.spectralogic.ds3cli.models.GetSystemFailureResult;
import com.spectralogic.ds3client.models.SystemFailure;
import com.spectralogic.ds3client.models.SystemFailureList;
import com.spectralogic.ds3client.utils.Guard;

import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.*;

public class GetSystemFailureView extends TableView<GetSystemFailureResult> {

    protected List<SystemFailure> failures;

    @Override
    public String render(final GetSystemFailureResult obj) {
        final SystemFailureList result = obj.getResult();
        if( (result == null) || (Guard.isNullOrEmpty(result.getSystemFailures())) ) {
            return "No system failures on remote appliance";
        }
        this.failures = result.getSystemFailures();

        initTable(ImmutableList.of("Type", "Failure Message", "Id", "Failure Date"));

        return "" + result.getSystemFailures().size() + " System Failures:\n" +
                ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final String [][] formatArray = new String[this.failures.size()][];
        int i = 0;
        for(final SystemFailure failure : this.failures) {
            final String [] bucketArray = new String[this.columnCount];
            bucketArray[0] = nullGuardToString(failure.getType());
            bucketArray[1] = nullGuard(failure.getErrorMessage());
            bucketArray[2] = nullGuardToString(failure.getId());
            bucketArray[3] = nullGuardToDate(failure.getDate(), DATE_FORMAT);
            formatArray[i++] = bucketArray;
        }
        return formatArray;
    }
}
