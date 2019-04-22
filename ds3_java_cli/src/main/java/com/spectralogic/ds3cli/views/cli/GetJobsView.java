/*
 * *****************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ***************************************************************************
 */

package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetJobsResult;
import com.spectralogic.ds3cli.util.Guard;
import com.spectralogic.ds3client.models.Job;
import com.spectralogic.ds3client.models.JobList;

import java.util.List;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Guard.nullGuard;
import static com.spectralogic.ds3cli.util.Guard.nullGuardFromDate;
import static com.spectralogic.ds3cli.util.Guard.nullGuardToString;

public class GetJobsView extends TableView<GetJobsResult> {

    private List<Job> jobs;

    @Override
    public String render(final GetJobsResult result) {
        final JobList jobsInfo = result.getResult();
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
            jobArray[2] = nullGuardFromDate(job.getStartDate(), DATE_FORMAT);
            jobArray[3] = nullGuard(job.getUserName());
            jobArray[4] = nullGuardToString(job.getRequestType());
            jobArray[5] = nullGuardToString(job.getStatus());
            formatArray[i] = jobArray;
        }
        return formatArray;
    }
}
