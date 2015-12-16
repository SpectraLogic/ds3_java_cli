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

package com.spectralogic.ds3cli.Exceptions;

import com.spectralogic.ds3client.models.tape.TapeFailure;

import java.util.List;

public class TapeFailureException extends Exception{
    public TapeFailureException(final List<TapeFailure> tapeFailures) {
        super(BuildTapeFailureMessage(tapeFailures));
    }

    private static String BuildTapeFailureMessage(final List<TapeFailure> tapeFailures) {
        final StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("There are tape failures in BlackPearl:\n");
        for (final TapeFailure tapeFailure : tapeFailures) {
            messageBuilder.append("Tape ID: ");
            messageBuilder.append(tapeFailure.getTapeId());
            messageBuilder.append(", ");
            messageBuilder.append("Error Message: ");
            messageBuilder.append(tapeFailure.getErrorMessage());
            messageBuilder.append("\n");
        }
        messageBuilder.append("To ignore this error use --force");
        return messageBuilder.toString();
    }
}
