package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.ListAllMyBucketsResult;

public class GetServiceResult implements Result {

    private final ListAllMyBucketsResult result;

    public GetServiceResult(final ListAllMyBucketsResult result) {
        this.result = result;
    }

    public ListAllMyBucketsResult getResult() {
        return this.result;
    }
}
