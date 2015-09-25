package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.GetJobRequest;
import com.spectralogic.ds3client.commands.GetJobResponse;
import org.apache.commons.cli.MissingArgumentException;

import java.util.UUID;

public class GetJob extends CliCommand<GetJobResult> {

    private UUID jobId;

    public GetJob(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {

        final String jobIdString = args.getId();
        if (jobIdString == null) {
            throw new MissingArgumentException("The get job command requires '-i' to be set.");
        }

        this.jobId = UUID.fromString(jobIdString);

        return this;
    }

    @Override
    public GetJobResult call() throws Exception {
        final GetJobResponse response = getClient().getJob(new GetJobRequest(this.jobId));

        return new GetJobResult(response.getMasterObjectList());
    }
}
