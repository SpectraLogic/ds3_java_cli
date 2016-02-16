package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.JobsApiBean;

public class GetJobsResult implements Result {

    private final JobsApiBean jobs;

    public GetJobsResult(final JobsApiBean jobs) {
        this.jobs = jobs;
    }

    public JobsApiBean getJobs() {
        return jobs;
    }
}
