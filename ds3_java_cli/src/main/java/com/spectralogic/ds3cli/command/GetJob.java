package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.GetJobSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetJobSpectraS3Response;
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
    public String getLongHelp() {
        final StringBuffer helpStringBuffer = new StringBuffer();
        helpStringBuffer.append("Retrieves information about a current job.\n");
        helpStringBuffer.append("Requires the '-i' parameter with the UUID of the job\n");
        helpStringBuffer.append("\nUse the get_jobs command to retrieve a list of jobs.");

        return helpStringBuffer.toString();
    }

    @Override
    public GetJobResult call() throws Exception {
        final GetJobSpectraS3Response response = getClient().getJobSpectraS3(new GetJobSpectraS3Request(this.jobId));

        return new GetJobResult(response.getMasterObjectListResult());
    }
}
