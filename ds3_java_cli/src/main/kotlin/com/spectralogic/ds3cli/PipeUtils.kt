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

import com.google.common.collect.ImmutableList
import com.spectralogic.ds3cli.exceptions.CommandException
import com.spectralogic.ds3client.Ds3Client
import com.spectralogic.ds3client.commands.spectrads3.GetObjectDetailsSpectraS3Request
import com.spectralogic.ds3client.commands.spectrads3.GetObjectsDetailsSpectraS3Request
import com.spectralogic.ds3client.models.Contents
import com.spectralogic.ds3client.networking.FailedRequestException
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.IOException

object PipeUtils {

    private val LOG = LoggerFactory.getLogger(PipeUtils::class.java)

    @JvmStatic
    @Throws(CommandException::class)
    fun getObjectsByPipe(pipedFileNames: ImmutableList<String>, client: Ds3Client, bucketName: String): Iterable<Contents> {
        return runBlocking {

            val fileDetails = supervisorScope {
                 pipedFileNames.map { objName ->
                    async(Dispatchers.IO) {
                        try {
                            if (objName.endsWith(".json", ignoreCase = true)) {
                                // If this is an object that ends in .json, then perform the more expensive call that
                                // bypasses the json formatting feature of response payloads in BP
                                val result = client.getObjectsDetailsSpectraS3(
                                        GetObjectsDetailsSpectraS3Request().withBucketId(bucketName).withName(objName).withLatest(true))

                                if (result.s3ObjectListResult.s3Objects == null || result.s3ObjectListResult.s3Objects.size == 0) {
                                    GetObjectResponse.NotFound(objName)
                                } else if (result.s3ObjectListResult.s3Objects.size > 1) {
                                    GetObjectResponse.Error(IOException("There are multiple versions of object with name: $objName"))
                                } else {
                                    GetObjectResponse.Success(Contents().apply {
                                        key = result.s3ObjectListResult.s3Objects[0].name
                                        lastModified = result.s3ObjectListResult.s3Objects[0].creationDate
                                    })
                                }
                            } else {
                                val result = client.getObjectDetailsSpectraS3(GetObjectDetailsSpectraS3Request(objName, bucketName)).s3ObjectResult

                                GetObjectResponse.Success(Contents().apply {
                                    key = result.name
                                    lastModified = result.creationDate
                                })
                            }
                        } catch (f: FailedRequestException) {
                            if (f.statusCode == 404) {
                                GetObjectResponse.NotFound(objName)
                            } else {
                                GetObjectResponse.Error(f)
                            }
                        } catch (e: IOException) {
                            GetObjectResponse.Error(e)
                        }
                    }
                }.awaitAll()
            }

            val errorBuilder = ImmutableList.builder<IOException>()
            val notFoundBuilder = ImmutableList.builder<String>()
            val contentBuilder = ImmutableList.builder<Contents>()

            for (result in fileDetails) {
                when (result) {
                    is GetObjectResponse.Success -> contentBuilder.add(result.contents)
                    is GetObjectResponse.NotFound -> notFoundBuilder.add(result.fileName)
                    is GetObjectResponse.Error -> errorBuilder.add(result.t)
                }
            }

            val errorList = errorBuilder.build()

            if (errorList.isNotEmpty()) {

                for (e in errorList) {
                    LOG.error("Encountered an error while getting the object details", e)
                }

                throw CommandException("Encountered an unexpected error while attempting to get object details for piped files.")
            }

            val notFoundList = notFoundBuilder.build()

            if (notFoundList.isNotEmpty()) {
                throw CommandException("Could not find the following objects: ${notFoundList.joinToString(", ")}")
            }

            contentBuilder.build()
        }
   }

    sealed class GetObjectResponse {
        class Success(val contents: Contents): GetObjectResponse()
        class NotFound(val fileName: String): GetObjectResponse()
        class Error(val t: IOException): GetObjectResponse()
    }
}
