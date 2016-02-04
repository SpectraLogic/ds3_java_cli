package com.spectralogic.ds3cli.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Metadata_Test {

    @Test
    public void singleMetadataEntry() {
        final ImmutableMap<String, String> metadata = Metadata.parse(new String[]{"key:value"});
        assertThat(metadata.size(), is(1));
        assertThat(metadata.get("key"), is("value"));
    }

    @Test
    public void twoMetadataEntries() {
        final ImmutableMap<String, String> metadata = Metadata.parse(new String[]{"key:value", "key2:value2"});
        assertThat(metadata.size(), is(2));
        assertThat(metadata.get("key"), is("value"));
        assertThat(metadata.get("key2"), is("value2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void malformedEntry() {
      Metadata.parse(new String[]{"key;value"});
    }
}
