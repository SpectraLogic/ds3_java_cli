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
import com.spectralogic.ds3client.utils.Guard;

import java.nio.file.attribute.FileTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class PosixFileMetadataBuilder {
    private final DateTimeFormatter dateTimeFormatter;

    private String lastModifiedTimeString;
    private String lastAccessedTimeString;
    private String creationTimeString;
    private String changedTimeString;
    private String ownerIdString;
    private String groupIdString;
    private String modeString;

    public PosixFileMetadataBuilder(final DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public PosixFileMetadataBuilder withLastModifiedTime(final FileTime lastModifiedTime) {
        lastModifiedTimeString = fileTimeToString(lastModifiedTime);
        return this;
    }

    private String fileTimeToString(final FileTime fileTime) {
        final OffsetDateTime fileTimeOffsetToUTC = fileTime.toInstant().atOffset(ZoneOffset.UTC);
        return fileTimeOffsetToUTC.format(dateTimeFormatter);
    }

    public PosixFileMetadataBuilder withLastAccessTime(final FileTime lastAccessTime) {
        lastAccessedTimeString = fileTimeToString(lastAccessTime);
        return this;
    }

    public PosixFileMetadataBuilder withCreationTime(final FileTime fileTime) {
        creationTimeString = fileTimeToString(fileTime);
        return this;
    }

    public PosixFileMetadataBuilder withChangedTime(final FileTime fileTime) {
        changedTimeString = fileTimeToString(fileTime);
        return this;
    }

    public PosixFileMetadataBuilder withOwnerId(final Integer ownerId) {
        ownerIdString = ownerId.toString();
        return this;
    }

    public PosixFileMetadataBuilder withGroupId(final Integer groupId) {
        groupIdString = groupId.toString();
        return this;
    }

    public PosixFileMetadataBuilder withMode(final Integer mode) {
        modeString = mode.toString();
        return this;
    }

    public ImmutableMap<String, String> toMetadataMap() {
        final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();

        if ( ! Guard.isStringNullOrEmpty(lastModifiedTimeString)) {
            mapBuilder.put(MetadataFieldType.LastModifiedTime.metadataFieldId(), lastModifiedTimeString);
        }

        if ( ! Guard.isStringNullOrEmpty(lastAccessedTimeString)) {
            mapBuilder.put(MetadataFieldType.LastAccessedTime.metadataFieldId(), lastAccessedTimeString);
        }

        if ( ! Guard.isStringNullOrEmpty(creationTimeString)) {
            mapBuilder.put(MetadataFieldType.CreationTime.metadataFieldId(), creationTimeString);
        }

        if ( ! Guard.isStringNullOrEmpty(changedTimeString)) {
            mapBuilder.put(MetadataFieldType.ChangedTime.metadataFieldId(), changedTimeString);
        }

        if ( ! Guard.isStringNullOrEmpty(ownerIdString)) {
            mapBuilder.put(MetadataFieldType.OwnerId.metadataFieldId(), ownerIdString);
        }

        if ( ! Guard.isStringNullOrEmpty(groupIdString)) {
            mapBuilder.put(MetadataFieldType.GroupId.metadataFieldId(), groupIdString);
        }

        if ( ! Guard.isStringNullOrEmpty(modeString)) {
            mapBuilder.put(MetadataFieldType.Mode.metadataFieldId(), modeString);
        }

        return mapBuilder.build();
    }
}
