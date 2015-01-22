/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.spectralogic.ds3client.models.Contents;

import java.util.Iterator;

public class GetBucketResult {
    @JsonProperty("bucket_name")
    final private String bucketName;
    @JsonProperty("objects")
    @JacksonXmlElementWrapper(
            useWrapping = true
    )
    final private Iterator<Contents> objIterator;


    public String getBucketName() {
        return bucketName;
    }

    public Iterator<Contents> getObjIterator() {
        return objIterator;
    }

    public GetBucketResult(final String bucketName, final Iterator<Contents> objIterator) {
        this.bucketName = bucketName;
        this.objIterator = objIterator;
    }
}
