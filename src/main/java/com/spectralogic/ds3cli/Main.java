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

package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.logging.Logging;
import com.spectralogic.ds3client.networking.FailedRequestException;

public class Main {

    public static void main(final String[] args) {
        try {
            final Arguments arguments = new Arguments(args);
            final Ds3Cli runner = new Ds3Cli(arguments);
            System.out.println(runner.call());
        } catch (final Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            if (Logging.isVerbose()) {
                e.printStackTrace();
                if (e instanceof FailedRequestException) {
                    Logging.log("Printing out the response from the server:");
                    Logging.log(((FailedRequestException) e).getResponseString());
                }
            }
            System.exit(1);
        }
    }
}