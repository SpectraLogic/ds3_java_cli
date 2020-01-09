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

package com.spectralogic.ds3cli

import com.spectralogic.ds3client.Ds3Client
import com.spectralogic.ds3client.commands.GetBucketRequest
import com.spectralogic.ds3client.models.Contents

object PagingUtils {
    @JvmStatic
    fun Ds3Client.pageBucket(bucketName: String, prefix: String? = null, showVersions: Boolean = false, nextMarker: String? = null): Iterable<Contents> = sequence<Contents> {
        var next = nextMarker

        do {
            val request = GetBucketRequest(bucketName)
                    .withMarker(next)
                    .withPrefix(prefix)

            val result = this@pageBucket.getBucket(request).listBucketResult

            val objects = if(showVersions) {
                result.versionedObjects
            } else {
                result.objects
            }

            objects.forEach { yield(it) }

            next = result.nextMarker

        } while (next != null)
    }.asIterable()
}
