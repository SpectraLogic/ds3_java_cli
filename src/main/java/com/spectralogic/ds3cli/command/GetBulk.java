package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3client.Ds3Client;

public class GetBulk extends CliCommand {
    public GetBulk(Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(Arguments args) throws Exception {
        
        return this;
    }

    @Override
    public String call() throws Exception {
        return null;
    }
}
