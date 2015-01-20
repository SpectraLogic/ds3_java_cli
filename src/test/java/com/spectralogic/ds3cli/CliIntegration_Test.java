/*package com.spectralogic.ds3cli.integration;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.SignatureException;

import com.spectralogic.ds3cli.Ds3Cli;
import com.spectralogic.ds3client.commands.*;
import org.junit.BeforeClass;
import org.junit.Test;

import com.spectralogic.ds3cli.command.*;
import com.spectralogic.ds3cli.models.*;
import com.spectralogic.ds3cli.views.*;
import com.spectralogic.ds3cli;

public class CliIntegration_Test {
}
    private static Ds3Cli cli;

    @BeforeClass
    public static void startup() {
        client = Util.fromEnv();
    }

    @Test
    public void createBucket() throws IOException, SignatureException {
        final String bucketName = "test_create_bucket";
        client.putBucket(new PutBucketRequest(bucketName));

        HeadBucketResponse response = null;
        try {
            response = client.headBucket(new HeadBucketRequest(bucketName));
            assertThat(response.getStatus(),
                    is(HeadBucketResponse.Status.EXISTS));
        } finally {
            if (response != null) {
                client.deleteBucket(new DeleteBucketRequest(bucketName));
            }
        }
    }
    */