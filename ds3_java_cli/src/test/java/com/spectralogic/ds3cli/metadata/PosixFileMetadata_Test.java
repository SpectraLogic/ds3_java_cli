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

package com.spectralogic.ds3cli.metadata;

import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.GuiceInjector;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.Platform;
import org.junit.Assume;
import org.junit.Test;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class PosixFileMetadata_Test {
    private final FileMetadata fileMetadata = GuiceInjector.INSTANCE.injector().getInstance(FileMetadataFactory.class).fileMetadata();

    @Test
    public void testFileModifiedTime() throws Exception {
        Assume.assumeFalse(Platform.isWindows());

        final FileNamePathTuple fileNamePathTuple = createAFile(true);

        try {
            final FileTime createdTime = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).creationTime();

            Runtime.getRuntime().exec("touch " + fileNamePathTuple.fileName()).waitFor();

            final FileTime modifedTimeAfterTouch = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastModifiedTime();

            fileMetadata.writeMetadataTo(fileNamePathTuple.filePath(), fileNamePathTuple.metadata());

            final FileTime modifiedTimeAfterRestore = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastModifiedTime();

            assertTrue(modifedTimeAfterTouch.compareTo(modifiedTimeAfterRestore) > 0);

            final FileTime createdTimeAfterRestoringLastModified = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).creationTime();

            assertEquals(createdTimeAfterRestoringLastModified, createdTime);
            assertTrue(modifiedTimeAfterRestore.compareTo(createdTimeAfterRestoringLastModified) > 0);
        } finally {
            if (fileNamePathTuple.filePath() != null) {
                Files.deleteIfExists(fileNamePathTuple.filePath());
            }
        }
    }

    private FileNamePathTuple createAFile(final boolean recordMetaData) throws Exception {
        final String fileName = "aFile.txt";
        final Path filePath = Paths.get(fileName);
        final Path createdFilePath = Files.createFile(filePath);

        Thread.sleep(1000);

        write(filePath, new byte[] { 0 });

        final ImmutableMap<String, String> metadata = fileMetadata.readMetadataFrom(filePath);

        assertEquals(MetadataFieldType.values().length, metadata.size());

        for (final String metadataField : metadata.values()) {
            assertFalse(Guard.isStringNullOrEmpty(metadataField));
        }

        Thread.sleep(1000);

        if (recordMetaData) {
            return new FileNamePathTuple(fileName, createdFilePath, metadata);
        } else {
            return new FileNamePathTuple(fileName, createdFilePath, ImmutableMap.of());
        }
    }

    private static class FileNamePathTuple {
        private final String fileName;
        private final Path filePath;
        private final ImmutableMap<String, String> metadata;

        private FileNamePathTuple(final String fileName, final Path filePath, final ImmutableMap<String, String> metadata) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.metadata = metadata;
        }

        private String fileName() {
            return fileName;
        }

        private Path filePath() {
            return filePath;
        }

        private ImmutableMap<String, String> metadata() {
            return metadata;
        }
    }

    @Test
    public void testFileAccessedTime() throws Exception {
        Assume.assumeFalse(Platform.isWindows());

        final FileNamePathTuple fileNamePathTuple = createAFile(true);

        try {
            readAllBytes(fileNamePathTuple.filePath);

            final FileTime accessedTimeAfterRead = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastAccessTime();

            fileMetadata.writeMetadataTo(fileNamePathTuple.filePath(), fileNamePathTuple.metadata());

            final FileTime accessedTimeAfterRestore = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastAccessTime();

            assertTrue(accessedTimeAfterRead.compareTo(accessedTimeAfterRestore) > 0);
        } finally {
            if (fileNamePathTuple.filePath() != null) {
                Files.deleteIfExists(fileNamePathTuple.filePath());
            }
        }
    }

    @Test
    public void testFileCreatedTime() throws Exception {
        Assume.assumeFalse(Platform.isWindows());

        final FileNamePathTuple fileNamePathTuple = createAFile(true);

        try {
            final FileTime lastModified = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastModifiedTime();

            Files.deleteIfExists(fileNamePathTuple.filePath);

            createAFile(false);

            final FileTime createdAfterDeletion = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).creationTime();

            fileMetadata.writeMetadataTo(fileNamePathTuple.filePath(), fileNamePathTuple.metadata());

            final FileTime lastModifiedAfterRestore = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastModifiedTime();
            assertEquals(lastModified, lastModifiedAfterRestore);

            final FileTime createdTimeAfterRestore = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).creationTime();

            assertTrue(createdAfterDeletion.compareTo(createdTimeAfterRestore) > 0);
        } finally {
            if (fileNamePathTuple.filePath() != null) {
                Files.deleteIfExists(fileNamePathTuple.filePath());
            }
        }
    }
}
