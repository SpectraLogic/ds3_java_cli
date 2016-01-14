package com.spectralogic.ds3cli.integration.helpers;

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

    public String getStatus() {
        return status;
    }

    public Data getData() {
        return data;
    }

    public static class Meta {
        @JsonProperty("Date")
        private String date;

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
        private long CompletedSizeInBytes;

        @JsonProperty("OriginalSizeInBytes")
        private long OriginalSizeInBytes;

        @JsonProperty("BucketName")
        private String BucketName;

        @JsonProperty("JobId")
        private UUID JobId;

        @JsonProperty("UserId")
        private String UserId;

        @JsonProperty("UserName")
        private String UserName;

        @JsonProperty("WriteOptimization")
        private String WriteOptimization;

        @JsonProperty("Priority")
        private String Priority;

        @JsonProperty("RequestType")
        private String RequestType;

        @JsonProperty("StartDate")
        private String StartDate;

        @JsonProperty("ChunkClientProcessingOrderGuarantee")
        private String ChunkClientProcessingOrderGuarantee;

        @JsonProperty("Status")
        private String Status;

        @JsonProperty("Objects")
        private String Objects;

        public String getNodes() {
            return nodes;
        }

        public long getCachedSizeInBytes() {
            return cachedSizeInBytes;
        }

        public long getCompletedSizeInBytes() {
            return CompletedSizeInBytes;
        }

        public long getOriginalSizeInBytes() {
            return OriginalSizeInBytes;
        }

        public String getBucketName() {
            return BucketName;
        }

        public UUID getJobId() {
            return JobId;
        }

        public String getUserName() {
            return UserName;
        }

        public String getPriority() {
            return Priority;
        }

        public String getRequestType() {
            return RequestType;
        }

        public String getChunkClientProcessingOrderGuarantee() {
            return ChunkClientProcessingOrderGuarantee;
        }

        public String getStatus() {
            return Status;
        }

        public String getObjects() {
            return Objects;
        }

    }

}
