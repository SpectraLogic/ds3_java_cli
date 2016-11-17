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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.TapeResult;
import com.spectralogic.ds3client.models.Tape;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Utils.nullGuard;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;

public class GetTapeView implements View<TapeResult> {
    @Override
    public String render(final TapeResult obj) throws JsonProcessingException {

        final Tape tape = obj.getResult();

        return String.format("Tape Bar Code: %s, Tape Type: %s, Available Space: %s, Full: %b, Last Modified: %s, Last Verification: %s",
            nullGuard(tape.getBarCode()),
            nullGuard(tape.getType().name()),
            Long.toString(tape.getAvailableRawCapacity()),
            tape.getFullOfData(),
            nullGuardToDate(tape.getLastModified(), DATE_FORMAT),
            nullGuardToDate(tape.getLastVerified(), DATE_FORMAT)
            ) + ejectionState(tape);
    }

    private String ejectionState(final Tape tape) {

        if (tape.getEjectDate() == null) {
            return "";
        }

        return String.format(", Ejection Date: %s, Ejection Pending: %s, Ejection Label: %s, Ejection Location: %s",
                nullGuardToDate(tape.getEjectDate(), DATE_FORMAT),
                nullGuardToDate(tape.getEjectPending(), DATE_FORMAT),
                nullGuard(tape.getEjectLabel()),
                nullGuard(tape.getEjectLocation()));
    }
}
