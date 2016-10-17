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

package com.spectralogic.ds3cli.exceptions;

import com.spectralogic.ds3client.networking.FailedRequestException;
import java.io.IOException;

public class CommandExceptionFactory {

    /**
     * Provide an exception with a common message "[Command class] failed: reason, then exception
     *
     * @param commandName pass in simple name of command class or string describing module
     * @param e the caught exception
     * @return a new Command Exception with regularized message.
     */

    public static CommandException getResponseExcepion(final String commandName, final IOException e) {
        return new CommandException(commandName + " failed: " + getExcepionDescription(e), e);
    }

    /**
     * Test for common return types and create description
     * @param e
     * @return String describing general cause
     */
    public static String getExcepionDescription(final IOException e) {
        if (e instanceof FailedRequestException) {
            final int statusCode = ((FailedRequestException) e).getStatusCode();
            if (statusCode == 500 || statusCode == 502) {
                return "cannot communicate with the remote DS3 appliance.";
            } else if (statusCode == 403) {
                if (e.getMessage().contains("Client clock")) {
                    return "clock Synchronization error";
                }
                return "permissions / authorization error.";
            } else if (statusCode == 404) {
                return "target entity not found.";
            } else if (statusCode == 409) {
                return "target entity already exixts.";
            }
            return "unknown error of (" + statusCode + ") while accessing the remote DS3 appliance.";
        }
        return "IO Error " + e.getMessage();
    }

    /**
     * Checks exception type and status code
     * @param e caught exception
     * @param code code to test
     * @return true if exception is type of FailedRequestException and has status code == code
     */
    public static boolean hasStatusCode(final Exception e, final int code) {
        return e instanceof FailedRequestException && ((FailedRequestException) e).getStatusCode() == code;
    }

}
