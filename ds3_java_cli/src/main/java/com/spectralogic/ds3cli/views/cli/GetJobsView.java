package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetJobsResult;
import com.spectralogic.ds3client.models.Job;
import com.spectralogic.ds3client.models.JobList;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class GetJobsView implements View<GetJobsResult> {
    @Override
    public String render(final GetJobsResult result) throws JsonProcessingException {

        final JobList jobsInfo = result.getJobs();
        if (jobsInfo == null || jobsInfo.getJobs().isEmpty()) {
            return "There are not jobs currently running.";
        }

        return ASCIITable.getInstance().getTable(getHeaders(), fromJobInfoList(jobsInfo));
    }

    private String[][] fromJobInfoList(final JobList jobs) {
        final String [][] formatArray = new String[jobs.getJobs().size()][];
        for (int i = 0; i < jobs.getJobs().size(); i ++) {
            final Job job = jobs.getJobs().get(i);
            final String [] jobArray = new String[6];
            jobArray[0] = nullGuard(job.getBucketName());
            jobArray[1] = nullGuard(job.getJobId().toString());
            jobArray[2] = nullGuard(job.getStartDate().toString());
            jobArray[3] = nullGuard(job.getUserName());
            jobArray[4] = nullGuard(job.getRequestType().toString());
            jobArray[5] = nullGuard(job.getStatus().toString());
            formatArray[i] = jobArray;
        }
        return formatArray;
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Bucket Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Job Id", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Creation Date", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("User Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Job Type", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Status", ASCIITable.ALIGN_LEFT)
        };
    }
}
