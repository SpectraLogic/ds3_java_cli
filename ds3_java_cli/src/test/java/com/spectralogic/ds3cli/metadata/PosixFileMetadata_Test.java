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
import org.junit.BeforeClass;
import org.junit.Test;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.spectralogic.ds3client.networking.Metadata;

public class PosixFileMetadata_Test {
    @BeforeClass
    public static void setup() {
        Assume.assumeFalse(Platform.isWindows());
    }

    private final FileMetadata fileMetadata = GuiceInjector.INSTANCE.injector().getInstance(FileMetadataFactory.class).fileMetadata();

    @Test
    public void testFileModifiedTime() throws Exception {
        final FileNamePathTuple fileNamePathTuple = createAFile("Twitch.txt", true);

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

    private FileNamePathTuple createAFile(final String fileName, final boolean recordMetaData) throws Exception {
        final Path filePath = Paths.get(fileName);
        final Path createdFilePath = Files.createFile(filePath);

        Thread.sleep(1000);

        write(filePath, new byte[] { 0 });

        final ImmutableMap<String, String> metadataMap = fileMetadata.readMetadataFrom(filePath);

        assertEquals(FileMetadataFieldType.values().length, metadataMap.size());

        for (final String metadataField : metadataMap.values()) {
            assertFalse(Guard.isStringNullOrEmpty(metadataField));
        }

        Thread.sleep(1000);

        if (recordMetaData) {
            final Metadata metadata = new Metadata() {
                @Override
                public List<String> get(final String s) {
                    return Collections.singletonList(metadataMap.get(s));
                }

                @Override
                public Set<String> keys() {
                    return metadataMap.keySet();
                }
            };

            return new FileNamePathTuple(fileName, createdFilePath, metadata);
        } else {
            final Metadata metadata = new Metadata() {
                @Override
                public List<String> get(final String s) {
                    return Collections.emptyList();
                }

                @Override
                public Set<String> keys() {
                    return Collections.emptySet();
                }
            };

            return new FileNamePathTuple(fileName, createdFilePath, metadata);
        }
    }

    private static class FileNamePathTuple {
        private final String fileName;
        private final Path filePath;
        private final Metadata metadata;

        private FileNamePathTuple(final String fileName, final Path filePath, final Metadata metadata) {
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

        private Metadata metadata() {
            return metadata;
        }
    }

    @Test
    public void testFileAccessedTime() throws Exception {
        final FileNamePathTuple fileNamePathTuple = createAFile("Gracie.txt", true);

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
        final String fileName = "Shasta.txt";

        final FileNamePathTuple fileNamePathTuple = createAFile(fileName, true);

        try {
            final FileTime lastModified = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastModifiedTime();

            Files.deleteIfExists(fileNamePathTuple.filePath);

            createAFile(fileName, false);

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

    @Test
    public void testFileMode() throws Exception {
        final FileNamePathTuple fileNamePathTuple = createAFile("Nibbles.txt", true);

        try {
            final int fileModeBeforeChmod = (int)Files.getAttribute(fileNamePathTuple.filePath(), "unix:mode");
            Runtime.getRuntime().exec("chmod 400 " + fileNamePathTuple.fileName()).waitFor();
            final int fileModeAfterChmod = (int)Files.getAttribute(fileNamePathTuple.filePath(), "unix:mode");
            assertFalse(fileModeAfterChmod == fileModeBeforeChmod);

            fileMetadata.writeMetadataTo(fileNamePathTuple.filePath(), fileNamePathTuple.metadata());

            final int fileModeAfterRestore = (int)Files.getAttribute(fileNamePathTuple.filePath(), "unix:mode");
            assertTrue(fileModeAfterRestore == fileModeBeforeChmod);
        } finally {
            if (fileNamePathTuple.filePath() != null) {
                Files.deleteIfExists(fileNamePathTuple.filePath());
            }
        }
    }

    /**
     * About the only thing we can do for owner and group is check that we don't get an exception.
     */
    @Test
    public void testOwnerAndGroup() {
        Throwable badJuju = null;

        FileNamePathTuple fileNamePathTuple = null;

        try {
            fileNamePathTuple = createAFile("Marbles.txt", true);
            fileMetadata.writeMetadataTo(fileNamePathTuple.filePath(), fileNamePathTuple.metadata());
        } catch (final Throwable t) {
            badJuju = t;
        } finally {
            if (fileNamePathTuple.filePath() != null) {
                try {
                    Files.deleteIfExists(fileNamePathTuple.filePath());
                } catch (final Throwable t) {
                    // Don't really care if we got an exception here
                }
            }
        }

        assertNull(badJuju);
    }
}
