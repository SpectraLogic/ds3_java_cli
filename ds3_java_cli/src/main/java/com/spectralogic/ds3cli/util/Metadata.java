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

public final class Metadata {
    public static ImmutableMap<String, String> parse(final String[] metadataArgs) {
        final ImmutableMap.Builder<String, String> metadataBuilder = ImmutableMap.builder();

        for (final String arg : metadataArgs) {
            final String[] keyValue = arg.split(":");
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Malformed METADATA entry: " + arg);
            }
            metadataBuilder.put(keyValue[0], keyValue[1]);
        }

        return metadataBuilder.build();
    }
}
