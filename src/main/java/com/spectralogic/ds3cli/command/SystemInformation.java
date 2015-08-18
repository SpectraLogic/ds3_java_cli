package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.SystemInformationResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.GetSystemInformationRequest;
import com.spectralogic.ds3client.commands.GetSystemInformationResponse;

public class SystemInformation extends CliCommand<SystemInformationResult> {
    public SystemInformation(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public SystemInformationResult call() throws Exception {
        final GetSystemInformationResponse response = getClient().getSystemInformation(new GetSystemInformationRequest());

        return new SystemInformationResult(response.getSystemInformation());
    }
}
