package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetJobsResult;
import com.spectralogic.ds3client.models.bulk.JobInfo;

import java.util.List;

public class GetJobsView implements View<GetJobsResult> {
    @Override
    public String render(final GetJobsResult obj) throws JsonProcessingException {

        final List<JobInfo> info = obj.getJobs();
        if (info == null || info.isEmpty()) {
            return "There are not jobs currently running.";
        }

        return ASCIITable.getInstance().getTable(getHeaders(), fromJobInfoList(obj.getJobs()));
    }

    private String[][] fromJobInfoList(final List<JobInfo> result) {
        final String [][] formatArray = new String[result.size()][];
        for(int i = 0; i < result.size(); i ++) {
            final JobInfo job = result.get(i);
            final String [] jobArray = new String[6];
            jobArray[0] = job.getBucketName();
            jobArray[1] = job.getJobId().toString();
            jobArray[2] = job.getStartDate();
            jobArray[3] = job.getUserName();
            jobArray[4] = job.getRequestType().toString();
            jobArray[5] = job.getStatus().toString();
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
