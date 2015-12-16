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

import com.spectralogic.ds3cli.Exceptions.TapeFailureException;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetTapeFailureRequest;
import com.spectralogic.ds3client.models.tape.TapeFailure;


import java.io.IOException;
import java.security.SignatureException;
import java.util.List;

public final class BlackPearlUtils {

    public static void checkBlackPearlForTapeFailure(final Ds3Client client) throws IOException, SignatureException, TapeFailureException {
        final List<TapeFailure> tapeFailures = client.getTapeFailure(new GetTapeFailureRequest()).getTapeFailures();

        if (tapeFailures!=null && tapeFailures.size() > 0) {
            throw new TapeFailureException(tapeFailures);
        }
    }
}
