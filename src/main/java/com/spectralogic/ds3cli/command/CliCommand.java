package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3client.Ds3Client;

import java.util.concurrent.Callable;

public abstract class CliCommand implements Callable<String> {

    private final Ds3Client client;

    public CliCommand(final Ds3Client client) {
        this.client = client;
    }

    protected Ds3Client getClient() {
        return client;
    }

    public abstract void init(final Arguments args) throws Exception;
}
