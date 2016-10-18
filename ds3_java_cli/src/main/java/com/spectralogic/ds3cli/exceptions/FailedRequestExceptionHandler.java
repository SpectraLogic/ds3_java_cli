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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailedRequestExceptionHandler  implements Ds3ExceptionHandler<FailedRequestException> {

    private final static Logger LOG = LoggerFactory.getLogger(FailedRequestExceptionHandler.class);

    public void handle(final String locationDescription, final FailedRequestException e, final boolean throwRuntimeException) {
        final int statusCode = e.getStatusCode();
        final StringBuilder description = new StringBuilder(locationDescription);
        description.append(" failed (FailedRequestException): ");
        if (statusCode == 500 || statusCode == 502) {
            description.append("cannot communicate with the remote DS3 appliance.");
        } else if (statusCode == 403) {
            if ( e.getMessage().contains("Client clock")) {
                description.append("clock Synchronization error");
            }
            description.append("permissions / authorization error.");
        } else if (statusCode == 404) {
            description.append("target entity not found.");
        } else if (statusCode == 409) {
            description.append("target entity already exixts.");
        } else {
            description.append("unknown error of (" + statusCode + ") while accessing the remote DS3 appliance.");
        }
        LOG.info(description.toString(), e);
        if (throwRuntimeException) {
            throw new RuntimeException(description.toString(), e);
        } else {
            System.out.println(description.toString());
        }
    }

}
