package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.S3Object;

public class GetObjectVersionsResult implements Result<Iterable<S3Object>> {
    private final Iterable<S3Object> result;

    public GetObjectVersionsResult(final Iterable<S3Object> result) {
        this.result = result;
    }

    @Override
    public Iterable<S3Object> getResult() {
        return result;
    }
}
