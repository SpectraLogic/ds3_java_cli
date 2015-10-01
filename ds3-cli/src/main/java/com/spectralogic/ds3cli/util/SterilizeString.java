/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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

public class SterilizeString {

    private final static String fs = System.getProperty("file.separator");
    private final static boolean isWindows = System.getProperty("os.name").contains("Windows");

    private SterilizeString() {}

    public static String toUnix(final String str) {
        if(isWindows) {
            return str.replace("\r\n", "\n");
        }
        return str;
    }

    public static String getFileDelimiter() {
        return getFileDelimiter(false);
    }

    public static String getFileDelimiter(final boolean isEscaped) {
        if(isEscaped && fs.compareTo("\\") == 0) {
            return "\\" + fs;
        }
        return fs;
    }

    public static String pathToWindows(final String str) {
        return pathToWindows(str, false);
    }

    public static String pathToWindows(final String str, boolean isEscaped) {
        if(isWindows) {
            if(isEscaped) {
                return str.replace("/", "\\\\");
            }
            return str.replace("/","\\");
        }
        return str;
    }
}
