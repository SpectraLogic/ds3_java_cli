package com.spectralogic.ds3cli.integration.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
