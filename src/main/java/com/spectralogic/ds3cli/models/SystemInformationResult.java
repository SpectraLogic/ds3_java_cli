package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.SystemInformation;

public class SystemInformationResult implements Result {

    private final SystemInformation systemInformation;

    public SystemInformationResult(final SystemInformation systemInformation) {
        this.systemInformation = systemInformation;
    }

    public SystemInformation getSystemInformation() {
        return systemInformation;
    }
}
