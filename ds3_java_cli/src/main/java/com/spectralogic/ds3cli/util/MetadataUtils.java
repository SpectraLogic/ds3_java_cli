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

import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.GuiceInjector;
import com.spectralogic.ds3cli.metadata.FileMetadata;
import com.spectralogic.ds3cli.metadata.FileMetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public final class MetadataUtils {
    private final static Logger LOG = LoggerFactory.getLogger(MetadataUtils.class);
    private final static FileMetadata FILE_METADATA = GuiceInjector.INSTANCE.injector().getInstance(FileMetadataFactory.class).fileMetadata();

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

    public static ImmutableMap<String, String> getMetadataValues(final Path path) {
        try {
            return FILE_METADATA.readMetadataFrom(path);
        } catch (final IOException e) {
            LOG.error("Could not get the last modified time for file: {}", path, e);
            return ImmutableMap.of();
        }
    }

    public static void restoreMetadataValues(final String filename, final com.spectralogic.ds3client.networking.Metadata metadata, final Path path) {
        try {
            FILE_METADATA.writeMetadataTo(path, metadata);
        } catch (final Throwable t) {
            LOG.error("Failed to restore the last modified date for object: {}", filename, t);
        }
    }
}
