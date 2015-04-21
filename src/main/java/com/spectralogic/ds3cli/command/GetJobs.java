package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.GetJobsResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.GetJobsRequest;

public class GetJobs extends CliCommand<GetJobsResult> {
    public GetJobs(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public GetJobsResult call() throws Exception {
        return new GetJobsResult(getClient().getJobs(new GetJobsRequest()).getJobs());
    }
}
