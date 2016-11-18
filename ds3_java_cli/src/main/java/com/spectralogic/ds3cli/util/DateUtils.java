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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtils {
    /**
     * parse a string to get time span in seconds
     * @param diff format ddays.hhours.mminutes.sseconds e.g., "d2.h8.m30.s45"
     * @return span in seconds
     */
    public static long dateDiffToSeconds(final String diff) {
        long secs = 0;
        final String[] units = diff.split("[.]");
        for (final String unit : units) {
            if (unit.matches("[dhms][0-9]+")) {
                final char unitdesc = unit.charAt(0);
                switch (unitdesc) {
                    case 's':
                        secs += Integer.parseInt(unit.replace("s", ""));
                        break;
                    case 'm':
                        secs += Integer.parseInt(unit.replace("m", "")) * 60;
                        break;
                    case 'h':
                        secs += Integer.parseInt(unit.replace("h", "")) * 60 * 60;
                        break;
                    case 'd':
                        secs += Integer.parseInt(unit.replace("d", "")) * 60 * 60 * 24;
                        break;
                }
            }
        }
        return secs;
    }

    /**
     * parse a string to get absolute date
     * @param diff format Yyear.Mmonth.Ddate.hhours.mminutes.sseconds e.g., "Y2016.M11.D10.h12"
     * @return Date
     */
    public static Date parseParamDate(final String diff) throws java.text.ParseException {
        final String dateString =  "0000-01-01T00:00:00.000UTC";
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");
        final char[] dateChars =  dateString.toCharArray();

        String timeZone = "UTC";
        final String[] units = diff.split("[.]");
        for (final String unit : units) {
            if (unit.matches("[YMDhms][0-9]+") || unit.matches("[Z]...")) {
                final char unitdesc = unit.charAt(0);
                switch (unitdesc) {
                    case 's':
                        // pad to at least two chars and replace two chars at dateChars[19] and left
                        updateDateArray(dateChars, unit.replace("s", "00"), 19, 2);
                        break;
                    case 'm':
                        updateDateArray(dateChars, unit.replace("m", "00"), 16, 2);
                        break;
                    case 'h':
                        updateDateArray(dateChars, unit.replace("h", "00"), 13, 2);
                        break;
                    case 'D':
                        updateDateArray(dateChars, unit.replace("D", "00"), 10, 2);
                        break;
                    case 'M':
                        updateDateArray(dateChars, unit.replace("M", "00"), 7, 2);
                        break;
                    case 'Y':
                        updateDateArray(dateChars, unit.replace("Y", "0000"), 4, 4);
                        break;
                    case 'Z':
                        updateDateArray(dateChars, unit.replace("Z", ""), 26, 3);
                        timeZone = unit.replace("Z", "");
                        break;
                }
            } else {
                throw new ParseException("Cannot process date token: '" + unit + "'", diff.indexOf(unit));
            }
        }
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return dateFormat.parse(new String(dateChars));
    }

    private static void updateDateArray(final char[] chars, final String value, final int rightPos, final int fieldWidth) {
        // value is a padded string copy the chars we need from the right
        for (int i = 1; i <= fieldWidth; i++ ) {
            chars[rightPos - i] = value.charAt(value.length() - i);
        }
    }
}
