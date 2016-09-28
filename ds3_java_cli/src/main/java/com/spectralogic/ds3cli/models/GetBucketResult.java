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

package com.spectralogic.ds3cli.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.spectralogic.ds3client.models.Contents;

import java.util.Iterator;

public class GetBucketResult implements Result {
    @JsonProperty("BucketName")
    final private String bucketName;
    @JsonProperty("Objects")
    @JacksonXmlElementWrapper(
            useWrapping = true
    )
    final private Iterable<Contents> contents;


    public String getBucketName() {
        return bucketName;
    }

    public Iterable<Contents> getContents() {
        return contents;
    }

    public GetBucketResult(final String bucketName, final Iterable<Contents> contents) {
        this.bucketName = bucketName;
        this.contents = contents;
    }
}
