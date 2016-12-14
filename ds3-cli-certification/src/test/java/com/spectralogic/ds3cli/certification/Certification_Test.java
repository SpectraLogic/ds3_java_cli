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

import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.helpers.Util;
import com.spectralogic.ds3cli.helpers.TempStorageIds;
import com.spectralogic.ds3cli.helpers.TempStorageUtil;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.common.Credentials;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
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
    private static final Ds3Client client = Ds3ClientBuilder.fromEnv().withHttps(false).build();
    private static final Ds3ClientHelpers HELPERS = Ds3ClientHelpers.wrap(client);
    private static final String TEST_ENV_NAME = "JavaCLI_Certification_Test";
    private static TempStorageIds envStorageIds;
    private static UUID envDataPolicyId;

    @BeforeClass
    public static void startup() throws IOException {
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
            final String expected = "Success: created bucket " + bucketName + ".";
            final CommandResponse response = Util.createBucket(client, bucketName);
            assertThat(response.getMessage(), is(expected));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void test_7_2_1_invalid_credentials() throws Exception {
        final String bucketName = "test_create_bucket";
        try {
            final String endpoint = System.getenv("DS3_ENDPOINT");
            final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
            final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).build();
            final CommandResponse response = Util.getService(invalid_client);

            assertThat(response.getReturnCode(), is(400));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void test_7_2_2_invalid_endpoint() throws Exception {
        final String bucketName = "test_create_bucket";
        try {
            final String endpoint = "INVALID_DS3_ENDPOINT";
            final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
            final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).build();
            final CommandResponse response = Util.getService(invalid_client);

            assertThat(response.getReturnCode(), is(400));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void test_7_3_1_list_nonexistent_bucket() throws Exception {
        final String bucketName = "test_create_bucket";
        try {
            final CommandResponse response = Util.createBucket(client, bucketName);

            assertThat(response.getReturnCode(), is(400));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void test_7_3_2_access_bucket_wrong_user() throws Exception {
        final String bucketName = "test_create_bucket";
        try {
            final String endpoint = System.getenv("DS3_ENDPOINT");
            final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
            final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).build();
            final CommandResponse response = Util.getService(invalid_client);

            assertThat(response.getReturnCode(), is(400));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }
}
