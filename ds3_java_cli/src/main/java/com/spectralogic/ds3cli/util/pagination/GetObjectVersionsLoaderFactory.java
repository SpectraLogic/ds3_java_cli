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
 
package com.spectralogic.ds3cli.util.pagination;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.models.S3Object;
import com.spectralogic.ds3client.utils.collections.LazyIterable;

public class GetObjectVersionsLoaderFactory implements LazyIterable.LazyLoaderFactory<S3Object> {

    private final Ds3Client client;
    private final String bucketName;
    private final String objectName;

    public GetObjectVersionsLoaderFactory(final Ds3Client client, final String bucketName, final String objectName) {
        this.client = client;
        this.bucketName = bucketName;
        this.objectName = objectName;
    }

    @Override
    public LazyIterable.LazyLoader<S3Object> create() {
        return new GetObjectsVersionsLoader(client, bucketName, objectName);
    }
}
