/*
 * ******************************************************************************
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
 * ****************************************************************************
 */

package com.spectralogic.ds3cli.helpers.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;
import java.util.UUID;

public class JobResponse {

    @JsonProperty("Meta")
    private Meta meta;

    @JsonProperty("Data")
    private Data data;

    @JsonProperty("Status")
    private String status;

    public Meta getMeta() {
        return meta;
    }

    public Data getData() {
        return data;
    }

    public String getStatus() {
        return status;
    }

    public static class Meta {
        @JsonProperty("Date")
        private String date;

        public String getDate() {
            return date;
        }
    }

    public static class Data {
        @JsonProperty("result")
        private JobDetails jobDetails;

        public JobDetails getJobDetails() {
            return jobDetails;
        }
    }

    public static class JobDetails {

        @JsonProperty("entirelyInCache")
        private boolean entirelyInCache;

        @JsonProperty("Nodes")
        private String nodes;

        @JsonProperty("cachedSizeInBytes")
        private long cachedSizeInBytes;

        @JsonProperty("completedSizeInBytes")
        private long completedSizeInBytes;

        @JsonProperty("originalSizeInBytes")
        private long originalSizeInBytes;

        @JsonProperty("bucketName")
        private String bucketName;

        @JsonProperty("jobId")
        private UUID jobId;

        @JsonProperty("userId")
        private String userId;

        @JsonProperty("userName")
        private String userName;

        @JsonProperty("writeOptimization")
        private String writeOptimization;

        @JsonProperty("priority")
        private String priority;

        @JsonProperty("requestType")
        private String requestType;

        @JsonProperty("startDate")
        private String startDate;

        @JsonProperty("chunkClientProcessingOrderGuarantee")
        private String chunkClientProcessingOrderGuarantee;

        @JsonProperty("status")
        private String status;

        @JsonProperty("Objects")
        @JacksonXmlElementWrapper(useWrapping = false)
        final private List<String> objects = null;

        @JsonProperty("aggregating")
        private boolean aggregating;

        @JsonProperty("naked")
        private boolean naked;

        @JsonProperty("name")
        private String name;

        public String getNodes() {
            return nodes;
        }

        public long getCachedSizeInBytes() {
            return cachedSizeInBytes;
        }

        public long getCompletedSizeInBytes() {
            return completedSizeInBytes;
        }

        public long getOriginalSizeInBytes() {
            return originalSizeInBytes;
        }

        public String getBucketName() {
            return bucketName;
        }

        public UUID getJobId() {
            return jobId;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getWriteOptimization() {
            return writeOptimization;
        }

        public String getPriority() {
            return priority;
        }

        public String getRequestType() {
            return requestType;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getChunkClientProcessingOrderGuarantee() {
            return chunkClientProcessingOrderGuarantee;
        }

        public String getStatus() {
            return status;
        }

        public List<String> getObjects() {
            return objects;
        }

        public boolean getAggregating() {
            return aggregating;
        }

        public boolean getNaked() {
            return naked;
        }

        public String getName() {
            return name;
        }
    }
}
