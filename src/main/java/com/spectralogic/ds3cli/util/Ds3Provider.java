package com.spectralogic.ds3cli.util;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

public interface Ds3Provider {
    Ds3Client getClient();
    Ds3ClientHelpers getClientHelpers();
}
