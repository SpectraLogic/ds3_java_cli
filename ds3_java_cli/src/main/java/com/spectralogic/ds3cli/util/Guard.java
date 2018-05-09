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

package com.spectralogic.ds3cli.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This guard class is used to help prevent a NPE when serializing data to the console
 */
public final class Guard {
    public static String nullGuard(final String message) {
        if (message == null) {
            return "N/A";
        }
        return message;
    }

    public static String nullGuard(final Object o) {
        if (o == null) {
            return "N/A";
        } else {
            return o.toString();
        }
    }

    public static String nullGuardToString(final Object o) {
        // default alternate
        return nullGuardToString(o, "N/A");
    }

    public static String nullGuardToString(final Object o, final String alternate) {
        if (o == null) {
            return alternate;
        }
        return o.toString();
    }

    public static String nullGuardFromDate(final Date date, final SimpleDateFormat dateFormat) {
        if (date == null) {
            return "N/A";
        }
        return dateFormat.format(date);
    }
}
