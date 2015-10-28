package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.models.GetTapesResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.GetTapesRequest;
import com.spectralogic.ds3client.commands.GetTapesResponse;
import com.spectralogic.ds3client.networking.FailedRequestException;

public class GetTapes extends CliCommand<GetTapesResult> {
    public GetTapes(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public GetTapesResult call() throws Exception {try {
            final GetTapesResponse response = getClient().getTapes(new GetTapesRequest());

            return new GetTapesResult(response.getTapes());
        } catch (final FailedRequestException e) {
            throw new CommandException("Failed Get Service", e);
        }
    }
}
