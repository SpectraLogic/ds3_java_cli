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

package com.spectralogic.ds3cli.metadata;

import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.GuiceInjector;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.Platform;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.spectralogic.ds3client.networking.Metadata;
import org.junit.rules.TemporaryFolder;

public class PosixFileMetadata_Test {
    @BeforeClass
    public static void setup() {
        Assume.assumeFalse(Platform.isWindows());
    }

    private final FileMetadata fileMetadata = GuiceInjector.INSTANCE.injector().getInstance(FileMetadataFactory.class).fileMetadata();

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testFileModifiedTime() throws Exception {
        final FileNamePathTuple fileNamePathTuple = createAFile("Twitch.txt", true);
        Runtime.getRuntime().exec("touch " + fileNamePathTuple.filePath().toAbsolutePath().toString()).waitFor();

        final FileTime modifiedTimeAfterTouch = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastModifiedTime();
        fileMetadata.writeMetadataTo(fileNamePathTuple.filePath(), fileNamePathTuple.metadata());

        final FileTime modifiedTimeAfterRestore = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastModifiedTime();
        assertThat(modifiedTimeAfterTouch.compareTo(modifiedTimeAfterRestore), greaterThan(0));
    }

    private FileNamePathTuple createAFile(final String fileName, final boolean recordMetaData) throws Exception {
        final Path filePath = tempFolder.newFile(fileName).toPath();

        Thread.sleep(1000);

        write(filePath, new byte[] { 0 });

        final ImmutableMap<String, String> metadataMap = fileMetadata.readMetadataFrom(filePath);

        assertEquals(FileMetadataFieldType.values().length, metadataMap.size());

        for (final String metadataField : metadataMap.values()) {
            assertFalse(Guard.isStringNullOrEmpty(metadataField));
        }

        Thread.sleep(1000);

        final Metadata metadata;

        if (recordMetaData) {
            metadata = new Metadata() {
                @Override
                public List<String> get(final String s) {
                    return Collections.singletonList(metadataMap.get(s));
                }

                @Override
                public Set<String> keys() {
                    return metadataMap.keySet();
                }
            };

        } else {
            metadata = new Metadata() {
                @Override
                public List<String> get(final String s) {
                    return Collections.emptyList();
                }

                @Override
                public Set<String> keys() {
                    return Collections.emptySet();
                }
            };
        }

        return new FileNamePathTuple(filePath, metadata);
    }

    private static class FileNamePathTuple {
        private final Path filePath;
        private final Metadata metadata;

        private FileNamePathTuple(final Path filePath, final Metadata metadata) {
            this.filePath = filePath;
            this.metadata = metadata;
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
        readAllBytes(fileNamePathTuple.filePath);

        final FileTime accessedTimeAfterRead = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastAccessTime();
        fileMetadata.writeMetadataTo(fileNamePathTuple.filePath(), fileNamePathTuple.metadata());

        final FileTime accessedTimeAfterRestore = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastAccessTime();
        assertThat(accessedTimeAfterRead.compareTo(accessedTimeAfterRestore), greaterThan(0));
    }

    @Test
    public void testFileCreatedTime() throws Exception {
        final String fileName = "Shasta.txt";
        final FileNamePathTuple fileNamePathTuple = createAFile(fileName, true);
        final FileTime lastModified = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastModifiedTime();
        Files.deleteIfExists(fileNamePathTuple.filePath);
        createAFile(fileName, false);

        final FileTime createdAfterDeletion = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).creationTime();
        fileMetadata.writeMetadataTo(fileNamePathTuple.filePath(), fileNamePathTuple.metadata());

        final FileTime lastModifiedAfterRestore = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).lastModifiedTime();
        assertEquals(lastModified, lastModifiedAfterRestore);

        final FileTime createdTimeAfterRestore = Files.readAttributes(fileNamePathTuple.filePath(), BasicFileAttributes.class).creationTime();
        assertThat(createdAfterDeletion.compareTo(createdTimeAfterRestore), greaterThan(0));

    }

    @Test
    public void testFileMode() throws Exception {
        final FileNamePathTuple fileNamePathTuple = createAFile("Nibbles.txt", true);

        final int fileModeBeforeChmod = (int)Files.getAttribute(fileNamePathTuple.filePath(), "unix:mode");
        Runtime.getRuntime().exec("chmod 400 " + fileNamePathTuple.filePath().toAbsolutePath().toString()).waitFor();
        final int fileModeAfterChmod = (int)Files.getAttribute(fileNamePathTuple.filePath(), "unix:mode");
        assertNotEquals(fileModeAfterChmod, fileModeBeforeChmod);

        fileMetadata.writeMetadataTo(fileNamePathTuple.filePath(), fileNamePathTuple.metadata());

        final int fileModeAfterRestore = (int)Files.getAttribute(fileNamePathTuple.filePath(), "unix:mode");
        assertEquals(fileModeAfterRestore, fileModeBeforeChmod);
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
