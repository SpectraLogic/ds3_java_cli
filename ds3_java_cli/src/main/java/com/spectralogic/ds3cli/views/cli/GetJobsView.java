package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetJobsResult;
import com.spectralogic.ds3client.models.Job;
import com.spectralogic.ds3client.models.JobList;

import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class GetJobsView extends TableView<GetJobsResult> {

    protected List<Job> jobs;

    @Override
    public String render(final GetJobsResult result) {

        final JobList jobsInfo = result.getJobs();
        if (jobsInfo == null || jobsInfo.getJobs().isEmpty()) {
            return "There are no jobs currently running.";
        }
        this.jobs = jobsInfo.getJobs();

        initTable(ImmutableList.of("Bucket Name", "Job Id", "Creation Date", "User Name", "Job Type", "Status" ));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final String [][] formatArray = new String[jobs.size()][];
        for (int i = 0; i < jobs.size(); i ++) {
            final Job job = jobs.get(i);
            final String [] jobArray = new String[this.columnCount];
            jobArray[0] = nullGuard(job.getBucketName());
            jobArray[1] = nullGuardToString(job.getJobId());
            jobArray[2] = nullGuardToString(job.getStartDate());
            jobArray[3] = nullGuard(job.getUserName());
            jobArray[4] = nullGuardToString(job.getRequestType());
            jobArray[5] = nullGuardToString(job.getStatus());
            formatArray[i] = jobArray;
        }
        return formatArray;
    }
}
