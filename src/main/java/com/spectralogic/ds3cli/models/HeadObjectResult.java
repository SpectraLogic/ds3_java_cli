package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.commands.HeadObjectResponse;
import com.spectralogic.ds3client.networking.Metadata;

public class HeadObjectResult implements Result {
    private final Metadata metadata;
    private final HeadObjectResponse.Status status;
    public HeadObjectResult(final HeadObjectResponse result) {
        this.metadata = result.getMetadata();
        this.status = result.getStatus();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public HeadObjectResponse.Status getStatus() {
        return status;
    }
}
