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

package com.spectralogic.ds3cli.util;

import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.exceptions.TapeFailureException;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.GetTapeFailuresSpectraS3Request;
import com.spectralogic.ds3client.models.DetailedTapeFailure;
import com.spectralogic.ds3client.utils.Guard;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;

public final class BlackPearlUtils {

    public static void checkBlackPearlForTapeFailure(final Ds3Client client) throws IOException, SignatureException, CommandException {
        final List<DetailedTapeFailure> tapeFailures = client.getTapeFailuresSpectraS3(new GetTapeFailuresSpectraS3Request()).getDetailedTapeFailureListResult().getDetailedTapeFailures();

        if (Guard.isNotNullAndNotEmpty(tapeFailures)) {
            throw new CommandException(new TapeFailureException(tapeFailures.iterator()));
        }
    }
}
