package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.JobList;
import com.spectralogic.ds3client.models.MasterObjectList;

public class GetJobsResult implements Result {

    private final JobList jobs;

    public GetJobsResult(final JobList jobs) {
        this.jobs = jobs;
    }

    public JobList getJobs() {
        return jobs;
    }
}
