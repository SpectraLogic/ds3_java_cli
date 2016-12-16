/*
 * ******************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3cli.certification;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.Ds3ProviderImpl;
import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.command.CliCommandFactory;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.helpers.Util;
import com.spectralogic.ds3cli.helpers.TempStorageIds;
import com.spectralogic.ds3cli.helpers.TempStorageUtil;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.exceptions.InvalidCertificate;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.common.Credentials;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Implement tests to automate the BlackPearl Certification process for the JavaCLI.
 *
 * For details, refer to
 *   https://developer.spectralogic.com/certification/
 *   https://developer.spectralogic.com/test-plan/
 */
public class Certification_Test {
    private static final ch.qos.logback.classic.Logger LOG =  (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private static final Ds3Client client = Ds3ClientBuilder.fromEnv().withHttps(false).build();
    private static final Ds3ClientHelpers HELPERS = Ds3ClientHelpers.wrap(client);
    private static final String TEST_ENV_NAME = "JavaCLI_Certification_Test";
    private static TempStorageIds envStorageIds;
    private static UUID envDataPolicyId;

    @BeforeClass
    public static void startup() throws IOException {
        LOG.setLevel(Level.INFO);
        envDataPolicyId = TempStorageUtil.setupDataPolicy(TEST_ENV_NAME, false, ChecksumType.Type.MD5, client);
        envStorageIds = TempStorageUtil.setup(TEST_ENV_NAME, envDataPolicyId, client);
    }

    @AfterClass
    public static void teardown() throws IOException {
        TempStorageUtil.teardown(TEST_ENV_NAME, envStorageIds, client);
        client.close();
    }

    @Test
    public void test_7_1_create_bucket() throws Exception {
        final String bucketName = "test_create_bucket";
        try {
            final String expectedCreateBucketMsg = "Success: created bucket " + bucketName + ".";
            final CommandResponse createBucketResponse = Util.createBucket(client, bucketName);
            LOG.info("CommandResponse for creating a bucket: \n{}", createBucketResponse.getMessage());
            assertThat(createBucketResponse.getMessage(), is(expectedCreateBucketMsg));

            final CommandResponse getServiceResponse = Util.getService(client);
            LOG.info("CommandResponse for listing buckets: \n{}", getServiceResponse.getMessage());
            assertThat(getServiceResponse.getMessage(), containsString(bucketName));
        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void test_7_2_1_invalid_credentials() throws Exception {
        final String endpoint = System.getenv("DS3_ENDPOINT");
        final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
        final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).build();
        try {
            Util.getService(invalid_client);
        } catch(final InvalidCertificate ice) {
            final String expectedError = "The certificate on black pearl is not a strong certificate and the request is being aborted.  Configure with the insecure option to perform the request.";
            assertThat(ice.getMessage(), containsString(expectedError));
            LOG.info("CommandResponse for 7.2.1 invalid credentials: \n{}", ice.getMessage());
        }
    }

    @Test
    public void test_7_2_2_invalid_endpoint() throws Exception {
        final String endpoint = "INVALID_DS3_ENDPOINT";
        final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
        final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).build();
        try {
            Util.getService(invalid_client);
        } catch(final UnknownHostException uhe) {
            final String expectedError = "INVALID_DS3_ENDPOINT: unknown error";
            assertThat(uhe.getMessage(), containsString(expectedError));
            LOG.info(uhe.getMessage());
        }
    }

    @Test
    public void test_7_3_1_list_nonexistent_bucket() throws Exception {
        final String bucketName = "test_list_nonexistent_bucket";
        try {
            Util.getBucket(client, bucketName);
        } catch(final CommandException ce) {
            assertThat(ce.getMessage(), containsString("Error: Unknown bucket."));
            LOG.info(ce.getMessage());
        }
    }

    @Test
    public void test_7_3_2_access_bucket_wrong_user() throws Exception {
        final String bucketName = "test_wrong_user_bucket";
        try {
            final String endpoint = System.getenv("DS3_ENDPOINT");
            final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
            final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).build();

            final CommandResponse createBucketResponse = Util.createBucket(client, bucketName);
            LOG.info("CommandResponse for creating a bucket: \n{}", createBucketResponse.getMessage());
            assertThat(createBucketResponse.getReturnCode(), is(0));

            final CommandResponse response = Util.getBucket(invalid_client, bucketName);
            assertThat(response.getReturnCode(), is(400));

        } catch(final InvalidCertificate ice) {
            final String expectedError = "The certificate on black pearl is not a strong certificate and the request is being aborted.  Configure with the insecure option to perform the request.";
            assertThat(ice.getMessage(), containsString(expectedError));
            LOG.info(ice.getMessage());
        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }
}
