package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.bulk.JobInfo;

import java.util.List;

public class GetJobsResult implements Result {

    private final List<JobInfo> jobs;

    public GetJobsResult(final List<JobInfo> jobs) {
        this.jobs = jobs;
    }

    public List<JobInfo> getJobs() {
        return jobs;
    }
}
