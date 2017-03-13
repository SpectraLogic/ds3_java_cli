/*
 * *****************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Contents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class SyncFilter implements FilteringIterable.FilterFunction<Path> {

    private final static Logger LOG = LoggerFactory.getLogger(SyncFilter.class);

    private final String prefix;
    private final Path inputDirectory;
    private final ImmutableMap<String, Contents> mapBucketFiles;

    public SyncFilter(final String prefix, final Path inputDirectory, final Ds3ClientHelpers helpers, final String bucketName) throws IOException {
        this.prefix = prefix;
        this.inputDirectory = inputDirectory;
        this.mapBucketFiles = generateBucketFileMap(prefix, helpers, bucketName);
    }

    private static ImmutableMap<String, Contents> generateBucketFileMap(final String prefix, final Ds3ClientHelpers helpers, final String bucketName) throws IOException {
        final Iterable<Contents> contents = helpers.listObjects(bucketName, prefix);
        final ImmutableMap.Builder<String, Contents> bucketFileMapBuilder = ImmutableMap.builder();
        for (final Contents content : contents) {
            bucketFileMapBuilder.put(content.getKey(), content);
        }
        return bucketFileMapBuilder.build();
    }

    @Override
    public boolean filter(final Path item) {
        final String fileName = FileUtils.getFileName(this.inputDirectory, item);
        final Contents content = mapBucketFiles.get(prefix + fileName);
        try {
            if (content == null) {
                return false;
            } else if (SyncUtils.isNewFile(item, content, true)) {
                LOG.info("Syncing new version of {}", fileName);
                return false;
            } else {
                LOG.info("No need to sync {}", fileName);
                return true;
            }
        } catch (final IOException e) {
            LOG.error("Failed to check the status of a file", e);
            // return false to let other code catch the exception when trying to access it
            return false;
        }
    }
}
