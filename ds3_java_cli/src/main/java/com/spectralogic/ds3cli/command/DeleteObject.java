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

package com.spectralogic.ds3cli.command;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.DeleteObjectRequest;
import com.spectralogic.ds3client.commands.spectrads3.GetObjectsDetailsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetObjectsDetailsSpectraS3Response;
import com.spectralogic.ds3client.models.S3Object;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.util.UUID;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class DeleteObject extends CliCommand<DefaultResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, OBJECT_NAME);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(VERSION_ID, ALL_VERSIONS);

    private String bucketName;
    private String objectName;
    private String versionId;
    private boolean allVersions;

    public DeleteObject() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.bucketName = args.getBucket();
        this.objectName = args.getObjectName();
        this.versionId = args.getVersionId();
        this.allVersions = args.isAllVersions();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {

        if (allVersions) {
            final GetObjectsDetailsSpectraS3Request getObjectsDetailsSpectraS3Request = new GetObjectsDetailsSpectraS3Request()
                    .withName(objectName)
                    .withBucketId(bucketName);
            final GetObjectsDetailsSpectraS3Response spectraS3Response = getClient().getObjectsDetailsSpectraS3(getObjectsDetailsSpectraS3Request);

            for (final S3Object objectVersion : spectraS3Response.getS3ObjectListResult().getS3Objects()) {
                deleteObject(bucketName, objectName, objectVersion.getId());
            }

            return new DefaultResult("Success: Deleted object '" + this.objectName + "' and all of it's versions from bucket '" + this.bucketName + "'.");
        } else {
            final UUID version;
            if (versionId != null) {
                version = UUID.fromString(versionId);
            } else {
                version = null;
            }
            deleteObject(bucketName, objectName, version);

            return new DefaultResult("Success: Deleted object '" + this.objectName + "' from bucket '" + this.bucketName + "'.");
        }
    }

    private void deleteObject(final String bucketName, final String objectName, final UUID version) throws IOException {
        final DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, objectName);
        if (version != null) {
            deleteObjectRequest.withVersionId(version);
        }
        getClient().deleteObject(deleteObjectRequest);
    }
}
