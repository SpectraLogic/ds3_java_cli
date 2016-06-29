package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Response;
import com.spectralogic.ds3client.models.BuildInformation;

public class SystemInformation extends CliCommand<DefaultResult> {
    public SystemInformation(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final GetSystemInformationSpectraS3Response response = getClient().getSystemInformationSpectraS3(new GetSystemInformationSpectraS3Request());

        final com.spectralogic.ds3client.models.SystemInformation sysInfo = response.getSystemInformationResult();
        final BuildInformation buildInfo = sysInfo.getBuildInformation();
        return new DefaultResult(String.format("Build Number: %s.%s, API Version: %s, Serial Number: %s", buildInfo.getVersion(), buildInfo.getRevision(), sysInfo.getApiVersion(), sysInfo.getSerialNumber()));
    }
}
