package com.spectralogic.ds3cli.integration.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Meta {
    @JsonProperty("Date")
    private final String date;

    @JsonCreator
    public Meta(@JsonProperty("Date") final String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }
}
