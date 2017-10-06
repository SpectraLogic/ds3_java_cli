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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

public class PosixFileMetadata implements FileMetadata {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSSX");

    private static final ImmutableMap<String, MetadataFieldHandler> METADATA_HANDLERS;

    static {
        final ImmutableMap.Builder<String, MetadataFieldHandler> mapBuilder = ImmutableMap.builder();
        mapBuilder.put(MetadataFieldNames.LAST_MODIFIED_TIME, (filePath, metadataValue) -> Files.getFileAttributeView(filePath, BasicFileAttributeView.class).setTimes(makeLocalFileTime(metadataValue), null, null));
        mapBuilder.put(MetadataFieldNames.LAST_ACCESSED_TIME, (filePath, metadataValue) -> Files.getFileAttributeView(filePath, BasicFileAttributeView.class).setTimes(null, makeLocalFileTime(metadataValue), null));
        mapBuilder.put(MetadataFieldNames.CREATION_TIME, (filePath, metadataValue) -> Files.getFileAttributeView(filePath, BasicFileAttributeView.class).setTimes(null, null, makeLocalFileTime(metadataValue)));
        mapBuilder.put(MetadataFieldNames.OWNER, (filePath, metadataValue) -> Files.setAttribute(filePath, "posix:owner", userPrincipalLookupService(filePath).lookupPrincipalByName(metadataValue)));
        mapBuilder.put(MetadataFieldNames.GROUP, (filePath, metadataValue) -> Files.setAttribute(filePath, "posix:group", userPrincipalLookupService(filePath).lookupPrincipalByGroupName(metadataValue)));
        mapBuilder.put(MetadataFieldNames.MODE, (filePath, metadataValue) -> Files.setAttribute(filePath, "unix:mode", Integer.parseInt(metadataValue)));
        METADATA_HANDLERS = mapBuilder.build();
    }

    private static FileTime makeLocalFileTime(final String metadataValue) {
        final Instant timeReadBack = LocalDateTime.parse(metadataValue, DATE_TIME_FORMATTER).toInstant(ZoneOffset.UTC);
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(timeReadBack, ZoneId.systemDefault());
        final Date localDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return FileTime.fromMillis(localDate.getTime());
    }

    private static UserPrincipalLookupService userPrincipalLookupService(final Path filePath) {
        return filePath.getFileSystem().getUserPrincipalLookupService();
    }

    @Override
    public ImmutableMap<String, String> readMetadataFrom(final Path filePath) throws IOException {
        final BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);

        return new PosixFileMetadataBuilder(DATE_TIME_FORMATTER)
                .withLastModifiedTime(basicFileAttributes.lastModifiedTime())
                .withLastAccessTime(basicFileAttributes.lastAccessTime())
                .withCreationTime(basicFileAttributes.creationTime())
                .withChangedTime((FileTime)Files.getAttribute(filePath, "unix:ctime"))
                .withOwnerId((int)Files.getAttribute(filePath, "unix:uid"))
                .withGroupId((int)Files.getAttribute(filePath, "unix:gid"))
                .withMode((int)Files.getAttribute(filePath, "unix:mode"))
                .toMetadataMap();
    }

    @Override
    public void writeMetadataTo(final Path filePath, final Map<String, String> metadata) throws IOException {
        for (final String metadataKey : metadata.keySet()) {
            final MetadataFieldHandler metadataFieldHandler = METADATA_HANDLERS.get(metadataKey);

            if (metadataFieldHandler != null) {
                metadataFieldHandler.apply(filePath, metadata.get(metadataKey));
            }
        }
    }

    private interface MetadataFieldHandler {
        void apply(final Path path, final String metadataValue) throws IOException;
    }
}
