package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3cli.models.GetJobsResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.GetJobSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetJobSpectraS3Response;
import org.apache.commons.cli.MissingArgumentException;

import java.util.UUID;

public class GetJob extends CliCommand<GetJobResult> {

    private UUID jobId;

    protected com.spectralogic.ds3cli.View<GetJobResult> cliView = new com.spectralogic.ds3cli.views.cli.GetJobView();
    protected com.spectralogic.ds3cli.View<GetJobResult> jsonView = new com.spectralogic.ds3cli.views.json.GetJobView();

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
        final GetJobSpectraS3Response response = getClient().getJobSpectraS3(new GetJobSpectraS3Request(this.jobId));

        return new GetJobResult(response.getMasterObjectListResult());
    }

    @Override
    public View getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return this.jsonView;
        }
        return this.cliView;
    }
}
