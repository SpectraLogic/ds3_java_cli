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

import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.Main;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Metadata_Utils_Test {

    @Test
    public void singleMetadataEntry() {
        final ImmutableMap<String, String> metadata = Main.metadataUtils().parse(new String[]{"key:value"});
        assertThat(metadata.size(), is(1));
        assertThat(metadata.get("key"), is("value"));
    }

    @Test
    public void twoMetadataEntries() {
        final ImmutableMap<String, String> metadata = Main.metadataUtils().parse(new String[]{"key:value", "key2:value2"});
        assertThat(metadata.size(), is(2));
        assertThat(metadata.get("key"), is("value"));
        assertThat(metadata.get("key2"), is("value2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void malformedEntry() {
        Main.metadataUtils().parse(new String[]{"key;value"});
    }
}
