package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.JobList;

public class GetJobsResult implements Result {

    private final JobList jobs;

    public GetJobsResult(final JobList jobs) {
        this.jobs = jobs;
    }

    public JobList getJobs() {
        return jobs;
    }
}
