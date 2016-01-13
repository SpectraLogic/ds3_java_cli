package com.spectralogic.ds3cli.integration.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

/**
 *
 {
 "Meta" : {
 "Date" : "2016-01-13T22:47:40.020Z"
 },
 "Data" : {
 "jobDetails" : {
 "Nodes" : null,
 "CachedSizeInBytes" : 0,
 "CompletedSizeInBytes" : 0,
 "OriginalSizeInBytes" : 776649,
 "BucketName" : "test_get_job_json",
 "JobId" : "b1cb1d58-e1fe-4a78-b0f5-a925cddc9945",
 "UserId" : "323fc5f3-a233-47ee-a35e-980773d05413",
 "UserName" : "spectra",
 "WriteOptimization" : "CAPACITY",
 "Priority" : "HIGH",
 "RequestType" : "GET",
 "StartDate" : "2016-01-13T22:47:39.000Z",
 "ChunkClientProcessingOrderGuarantee" : "NONE",
 "Status" : "COMPLETED",
 "Objects" : null
 }
 },
 "Status" : "OK"
 }
 */

public class JobResponse {

    @JsonProperty("Meta")
    private Meta meta;

    @JsonProperty("Data")
    private Data data;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(final Meta meta) {
        this.meta = meta;
    }

    public static class Meta {
        @JsonProperty("Date")
        private String date;

        public String getDate() {
            return date;
        }

        public void setDate(final String date) {
            this.date = date;
        }
    }

    public static class Data {
        @JsonProperty("jobDetails")
        private JobDetails jobDetails;

        public JobDetails getJobDetails() {
            return jobDetails;
        }

        public void setJobDetails(final JobDetails jobDetails) {
            this.jobDetails = jobDetails;
        }
    }

    public static class JobDetails {

        @JsonProperty("Nodes")
        private String nodes;

        @JsonProperty("CachedSizeInBytes")
        private long cachedSizeInBytes;

        public String getNodes() {
            return nodes;
        }

        public void setNodes(final String nodes) {
            this.nodes = nodes;
        }

        public long getCachedSizeInBytes() {
            return cachedSizeInBytes;
        }

        public void setCachedSizeInBytes(final long cachedSizeInBytes) {
            this.cachedSizeInBytes = cachedSizeInBytes;
        }
    }
}
