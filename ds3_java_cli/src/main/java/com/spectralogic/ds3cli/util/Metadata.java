package com.spectralogic.ds3cli.util;

import com.google.common.collect.ImmutableMap;

public final class Metadata {
    public static ImmutableMap<String, String> parse(final String[] metadataArgs) {
        final ImmutableMap.Builder<String, String> metadataBuilder = ImmutableMap.builder();

        for (final String arg : metadataArgs) {
            final String[] keyValue = arg.split(":");
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Malformed metadata entry: " + arg);
            }
            metadataBuilder.put(keyValue[0], keyValue[1]);
        }

        return metadataBuilder.build();
    }
}
