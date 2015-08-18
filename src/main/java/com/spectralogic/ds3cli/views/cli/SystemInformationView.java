package com.spectralogic.ds3cli.views.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.SystemInformationResult;
import com.spectralogic.ds3client.models.SystemInformation;

public class SystemInformationView implements View<SystemInformationResult> {
    @Override
    public String render(final SystemInformationResult obj) throws JsonProcessingException {
        final SystemInformation sysInfo = obj.getSystemInformation();
        final SystemInformation.BuildInformation buildInfo = sysInfo.getBuildInformation();
        return String.format("Build Number: %s.%s, API Version: %s, Serial Number: %s", buildInfo.getVersion(), buildInfo.getRevision(), sysInfo.getApiVersion(), sysInfo.getSerialNumber());
    }
}
