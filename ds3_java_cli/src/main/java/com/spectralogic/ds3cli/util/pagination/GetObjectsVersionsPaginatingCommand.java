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
 
package com.spectralogic.ds3cli.util.pagination;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.GetObjectsDetailsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetObjectsDetailsSpectraS3Response;
import com.spectralogic.ds3client.helpers.pagination.PaginatingCommand;
import com.spectralogic.ds3client.models.S3Object;

import java.io.IOException;
import java.util.List;

public class GetObjectsVersionsPaginatingCommand implements PaginatingCommand<S3Object, GetObjectsDetailsSpectraS3Request, GetObjectsDetailsSpectraS3Response> {

    private final String bucketName;
    private final String objectName;
    private final Ds3Client client;

    public GetObjectsVersionsPaginatingCommand(final Ds3Client client, final String bucketName, final String objectName) {
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.client = client;
    }


    @Override
    public GetObjectsDetailsSpectraS3Request createRequest() {
        return new GetObjectsDetailsSpectraS3Request().withBucketId(bucketName).withName(objectName);
    }

    @Override
    public GetObjectsDetailsSpectraS3Response invokeCommand(final GetObjectsDetailsSpectraS3Request getObjectsDetailsSpectraS3Request) throws IOException {
        return client.getObjectsDetailsSpectraS3(getObjectsDetailsSpectraS3Request);
    }

    @Override
    public List<S3Object> getResponseContents(final GetObjectsDetailsSpectraS3Response getObjectsDetailsSpectraS3Response) {
        return ImmutableList.copyOf(getObjectsDetailsSpectraS3Response.getS3ObjectListResult().getS3Objects());
    }
}
