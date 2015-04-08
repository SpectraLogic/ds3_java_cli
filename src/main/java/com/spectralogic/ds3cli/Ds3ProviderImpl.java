package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

class Ds3ProviderImpl implements Ds3Provider {

    private final Ds3Client client;
    private final Ds3ClientHelpers helpers;

    public Ds3ProviderImpl(final Ds3Client client, final Ds3ClientHelpers helpers) {
        this.client = client;
        this.helpers = helpers;
    }

    @Override
    public Ds3Client getClient() {
        return this.client;
    }

    @Override
    public Ds3ClientHelpers getClientHelpers() {
        return this.helpers;
    }
}
