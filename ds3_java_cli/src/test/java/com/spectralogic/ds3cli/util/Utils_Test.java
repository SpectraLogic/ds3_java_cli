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

import org.junit.Test;

import static com.spectralogic.ds3cli.util.Utils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Utils_Test {

    @Test
     public void windowsNormalizeObjectName_Test() {
        final String expected = "file.txt";
        assertThat(windowsNormalizeObjectName("file.txt"), is(expected));
        assertThat(windowsNormalizeObjectName("C:\\file.txt"), is(expected));
        assertThat(windowsNormalizeObjectName("\\file.txt"), is(expected));
        assertThat(windowsNormalizeObjectName(".\\file.txt"), is(expected));
        assertThat(windowsNormalizeObjectName("..\\file.txt"), is(expected));
        assertThat(windowsNormalizeObjectName("..\\..\\file.txt"), is(expected));
        assertThat(windowsNormalizeObjectName("..\\..\\..\\file.txt"), is(expected));
    }

    @Test
    public void unixNormalizeObjectName_Test() {
        final String expected = "file.txt";
        assertThat(unixNormalizeObjectName("file.txt"), is(expected));
        assertThat(unixNormalizeObjectName("/file.txt"), is(expected));
        assertThat(unixNormalizeObjectName("./file.txt"), is(expected));
        assertThat(unixNormalizeObjectName("../file.txt"), is(expected));
        assertThat(unixNormalizeObjectName("../../file.txt"), is(expected));
        assertThat(unixNormalizeObjectName("../../../file.txt"), is(expected));
    }

    @Test
    public void removePrefixRecursively_Test() {
        final String expected = "file.txt";

        assertThat(removePrefixRecursively(null, "..\\"), is(""));
        assertThat(removePrefixRecursively("", "..\\"), is(""));

        assertThat(removePrefixRecursively("file.txt", null), is(expected));
        assertThat(removePrefixRecursively("file.txt", ""), is(expected));

        assertThat(removePrefixRecursively("file.txt", "..\\"), is(expected));
        assertThat(removePrefixRecursively("..\\file.txt", "..\\"), is(expected));
        assertThat(removePrefixRecursively("..\\..\\file.txt", "..\\"), is(expected));
        assertThat(removePrefixRecursively("..\\..\\..\\file.txt", "..\\"), is(expected));
    }
}
