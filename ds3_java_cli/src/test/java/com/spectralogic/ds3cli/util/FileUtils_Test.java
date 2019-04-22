/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FileUtils_Test {

    @Test
     public void windowsNormalizeObjectName_Test() {
        final String expected = "file.txt";
        assertThat(FileUtils.windowsNormalizeObjectName("file.txt"), is(expected));
        assertThat(FileUtils.windowsNormalizeObjectName("C:\\file.txt"), is(expected));
        assertThat(FileUtils.windowsNormalizeObjectName("\\file.txt"), is(expected));
        assertThat(FileUtils.windowsNormalizeObjectName(".\\file.txt"), is(expected));
        assertThat(FileUtils.windowsNormalizeObjectName("..\\file.txt"), is(expected));
        assertThat(FileUtils.windowsNormalizeObjectName("..\\..\\file.txt"), is(expected));
        assertThat(FileUtils.windowsNormalizeObjectName("..\\..\\..\\file.txt"), is(expected));
    }

    @Test
    public void unixNormalizeObjectName_Test() {
        final String expected = "file.txt";
        assertThat(FileUtils.unixNormalizeObjectName("file.txt"), is(expected));
        assertThat(FileUtils.unixNormalizeObjectName("/file.txt"), is(expected));
        assertThat(FileUtils.unixNormalizeObjectName("./file.txt"), is(expected));
        assertThat(FileUtils.unixNormalizeObjectName("../file.txt"), is(expected));
        assertThat(FileUtils.unixNormalizeObjectName("../../file.txt"), is(expected));
        assertThat(FileUtils.unixNormalizeObjectName("../../../file.txt"), is(expected));
    }

    @Test
    public void removePrefixRecursively_Test() {
        final String expected = "file.txt";

        assertThat(FileUtils.removePrefixRecursively(null, "..\\"), is(""));
        assertThat(FileUtils.removePrefixRecursively("", "..\\"), is(""));

        assertThat(FileUtils.removePrefixRecursively("file.txt", null), is(expected));
        assertThat(FileUtils.removePrefixRecursively("file.txt", ""), is(expected));

        assertThat(FileUtils.removePrefixRecursively("file.txt", "..\\"), is(expected));
        assertThat(FileUtils.removePrefixRecursively("..\\file.txt", "..\\"), is(expected));
        assertThat(FileUtils.removePrefixRecursively("..\\..\\file.txt", "..\\"), is(expected));
        assertThat(FileUtils.removePrefixRecursively("..\\..\\..\\file.txt", "..\\"), is(expected));
    }
}
