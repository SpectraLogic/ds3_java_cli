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

package com.spectralogic.ds3cli.helpers.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMultimap;
import com.spectralogic.ds3client.commands.HeadObjectResponse;

public class HeadObject {

    @JsonProperty("Meta")
    private final Meta meta;

    @JsonProperty("Data")
    private final HeaderData data;

    @JsonProperty("Status")
    private final String status;

    @JsonCreator
    public HeadObject(@JsonProperty("Meta") final Meta meta, @JsonProperty("Data") final HeaderData data, @JsonProperty("Status") final String status) {
        this.meta = meta;
        this.data = data;
        this.status = status;
    }

    public Meta getMeta() {
        return meta;
    }

    public HeaderData getData() {
        return data;
    }

    public String getStatus() {
        return status;
    }

    public static class HeaderData {

        @JsonProperty("Metadata")
        private final ImmutableMultimap<String, String> metadata;

        @JsonProperty("Status")
        private final HeadObjectResponse.Status status;

        @JsonCreator
        public HeaderData(@JsonProperty("Metadata") final ImmutableMultimap<String, String> metadata, @JsonProperty("Status") final HeadObjectResponse.Status status) {
            this.metadata = metadata;
            this.status = status;
        }

        public ImmutableMultimap<String, String> getMetadata() {
            return metadata;
        }

        public HeadObjectResponse.Status getStatus() {
            return status;
        }
    }
}
