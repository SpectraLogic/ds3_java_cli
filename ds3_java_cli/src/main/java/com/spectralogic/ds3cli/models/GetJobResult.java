package com.spectralogic.ds3cli.models;


import com.spectralogic.ds3client.models.MasterObjectList;

public class GetJobResult implements Result {
    private final MasterObjectList jobDetails;

    public GetJobResult(final MasterObjectList masterObjectList) {
        this.jobDetails = masterObjectList;
    }

    public MasterObjectList getJobDetails() {
        return jobDetails;
    }
}
