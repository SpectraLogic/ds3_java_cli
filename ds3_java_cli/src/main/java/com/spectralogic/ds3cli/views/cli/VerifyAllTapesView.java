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
import com.spectralogic.ds3cli.models.VerifyAllTapesResult;
import com.spectralogic.ds3client.models.TapeFailure;
import com.spectralogic.ds3client.models.TapeFailureList;
import com.spectralogic.ds3client.utils.Guard;

import java.util.List;

import static com.spectralogic.ds3cli.util.Guard.nullGuard;

public class VerifyAllTapesView extends TableView<VerifyAllTapesResult> {

    private TapeFailureList tapeFailureList;

    @Override
    public String render(final VerifyAllTapesResult result) {

        final TapeFailureList tapeFailureList = result.getResult();

        if (tapeFailureList == null || Guard.isNullOrEmpty(tapeFailureList.getFailures())) {
            return "Verify tasks for all of the tapes have been scheduled.";
        }

        initTable(ImmutableList.of("Error", "Tape Id"));

        this.tapeFailureList = tapeFailureList;

        return "Failed to schedule verify tasks on the following tapes:\n" + ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    @Override
    protected String[][] formatTableContents() {

        final List<TapeFailure> tapeFailures = tapeFailureList.getFailures();

        final int numTapeFailures = tapeFailures.size();
        final String [][] formatArray = new String[numTapeFailures][];

        for (int i = 0; i < numTapeFailures; i++) {
            final TapeFailure tapeFailure = tapeFailures.get(i);

            final String [] tapeFailureArray = new String[this.columnCount];
            tapeFailureArray[0] = nullGuard(tapeFailure.getCause());
            if (tapeFailure.getTape() == null) {
                tapeFailureArray[1] = "Missing";
            } else {
                tapeFailureArray[1] = nullGuard(tapeFailure.getTape().getBarCode());
            }
            formatArray[i] = tapeFailureArray;
        }

        return formatArray;

    }
}
