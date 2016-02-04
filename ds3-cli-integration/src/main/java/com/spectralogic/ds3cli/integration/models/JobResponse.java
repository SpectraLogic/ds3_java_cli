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

package com.spectralogic.ds3cli.integration.models;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        @JsonProperty("jobDetails")
        private JobDetails jobDetails;

        public JobDetails getJobDetails() {
            return jobDetails;
        }
    }

    public static class JobDetails {

        @JsonProperty("Nodes")
        private String nodes;

        @JsonProperty("CachedSizeInBytes")
        private long cachedSizeInBytes;

        @JsonProperty("CompletedSizeInBytes")
        private long completedSizeInBytes;

        @JsonProperty("OriginalSizeInBytes")
        private long originalSizeInBytes;

        @JsonProperty("BucketName")
        private String bucketName;

        @JsonProperty("JobId")
        private UUID jobId;

        @JsonProperty("UserId")
        private String userId;

        @JsonProperty("UserName")
        private String userName;

        @JsonProperty("WriteOptimization")
        private String writeOptimization;

        @JsonProperty("Priority")
        private String priority;

        @JsonProperty("RequestType")
        private String requestType;

        @JsonProperty("StartDate")
        private String startDate;

        @JsonProperty("ChunkClientProcessingOrderGuarantee")
        private String chunkClientProcessingOrderGuarantee;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("Objects")
        private String objects;

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

        public String getObjects() {
            return objects;
        }

    }
}
