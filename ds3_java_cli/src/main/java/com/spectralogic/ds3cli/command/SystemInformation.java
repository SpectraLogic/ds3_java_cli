package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.SystemInformationResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Response;

public class SystemInformation extends CliCommand<SystemInformationResult> {
    public SystemInformation(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public String getLongHelp() {
        final StringBuffer helpStringBuffer = new StringBuffer();
        helpStringBuffer.append("Retrieves basic system information: software version, build, and system serial number.\n");
        helpStringBuffer.append("Useful to test communication\n");

        return helpStringBuffer.toString();
    }

    @Override
    public SystemInformationResult call() throws Exception {
        final GetSystemInformationSpectraS3Response response = getClient().getSystemInformationSpectraS3(new GetSystemInformationSpectraS3Request());

        return new SystemInformationResult(response.getSystemInformationResult());
    }
}
