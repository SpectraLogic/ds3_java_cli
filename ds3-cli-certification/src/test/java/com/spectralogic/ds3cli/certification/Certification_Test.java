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
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.exceptions.ExceptionFormatter;
import com.spectralogic.ds3cli.exceptions.FailedRequestExceptionHandler;
import com.spectralogic.ds3cli.helpers.Util;
import com.spectralogic.ds3cli.helpers.TempStorageIds;
import com.spectralogic.ds3cli.helpers.TempStorageUtil;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.exceptions.InvalidCertificate;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.common.Credentials;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


/**
 * Implement tests to automate the BlackPearl Certification process for the JavaCLI.
 *
 * For details, refer to
 *   https://developer.spectralogic.com/certification/
 *   https://developer.spectralogic.com/test-plan/
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Order only matters for manually verifying the results
public class Certification_Test {
    private static final ch.qos.logback.classic.Logger LOG =  (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private static final Ds3Client client = Ds3ClientBuilder.fromEnv().withHttps(false).build();
    private static final Ds3ClientHelpers HELPERS = Ds3ClientHelpers.wrap(client);
    private static final String TEST_ENV_NAME = "JavaCLI_Certification_Test";
    private static final String NO_RIGHTS_USERNAME = "no_rights";
    private static TempStorageIds envStorageIds;
    private static UUID envDataPolicyId;
    private static CertificationWriter OUT;

    //    private static final Long GB_BYTES = 1073741824L;
    private static final Long GB_BYTES = 1024L;

    @BeforeClass
    public static void startup() throws IOException {
        LOG.setLevel(Level.INFO);
        envDataPolicyId = TempStorageUtil.setupDataPolicy(TEST_ENV_NAME, false, ChecksumType.Type.MD5, client);
        envStorageIds = TempStorageUtil.setup(TEST_ENV_NAME, envDataPolicyId, client);
        final String fileName = Certification_Test.class.getSimpleName() + "_" + new Date().getTime();
        OUT = new CertificationWriter(fileName);
    }

    @AfterClass
    public static void teardown() throws IOException {
        TempStorageUtil.teardown(TEST_ENV_NAME, envStorageIds, client);
        client.close();
        OUT.close();
    }

    @Test
    public void test_7_1_create_bucket() throws Exception {
        final String bucketName = "test_create_bucket";
        OUT.startNewTest("7.1: Create Bucket");
        boolean success = false;

        try {
            final String expectedCreateBucketMsg = "Success: created bucket " + bucketName + ".";
            OUT.insertLog("Create bucket");
            final String createBucketArgs = "--http -c put_bucket -b " + bucketName;
            final CommandResponse createBucketResponse = OUT.runCommand(client, createBucketArgs);
            assertThat(createBucketResponse.getMessage(), is(expectedCreateBucketMsg));

            OUT.insertLog("List all buckets");
            final String listBucketArgs = "--http -c get_service";
            final CommandResponse listBucketResponse = OUT.runCommand(client, listBucketArgs);
            assertThat(listBucketResponse.getMessage(), containsString(bucketName));
            success = true;

        } finally {
            OUT.finishTest("7.1: Create Bucket", success);
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void test_7_2_1_invalid_credentials() throws Exception {
        final String endpoint = System.getenv("DS3_ENDPOINT");
        final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
        final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).withHttps(false).build();
        OUT.startNewTest("7.2.1: Invalid Credentials");
        boolean success = false;

        try {
            OUT.runCommand(invalid_client, "--http -c get_service");
        } catch(final FailedRequestException e) {
            final String formattedException = FailedRequestExceptionHandler.format(e);
            OUT.insertPreformat(formattedException);
            final String expectedError = "permissions / authorization error";
            assertThat(formattedException, containsString(expectedError));
            success = true;
        } finally {
            OUT.finishTest("7.2.1: Invalid Credentials", success);
        }
    }

    @Test
    public void test_7_2_2_invalid_endpoint() throws Exception {
        final String endpoint = "invalid_endpoint";
        final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
        final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).build();
        OUT.startNewTest("7.2.2: Invalid endpoint");
        boolean success = false;
        try {
            OUT.runCommand(invalid_client, "--http -c get_service");
        } catch(final UnknownHostException uhe) {
            final String formattedException = ExceptionFormatter.format(uhe);
            final String expectedError = "UnknownHost";
            assertThat(formattedException, containsString(expectedError));
            OUT.insertLog("CommandResponse for 7.2.2 invalid endpoint:");
            OUT.insertPreformat(formattedException);
            success = true;
        } finally {
            OUT.finishTest("7.2.2: Invalid Endpoint", success);
        }
    }

    @Test
    public void test_7_3_1_list_nonexistent_bucket() throws Exception {
        final String bucketName = "test_list_nonexistent_bucket";
        OUT.startNewTest("7.3.1: Non-extant bucket");
        boolean success = false;

        try {
            OUT.runCommand(client, "--http -c get_bucket -b " + bucketName);
        } catch(final CommandException ce) {
            final String formattedException = ExceptionFormatter.format(ce);
            assertThat(formattedException, containsString("Error: Unknown bucket."));
            OUT.insertLog("CommandResponse for 7.3.1 failed attempt to list nonexistent bucket:");
            OUT.insertPreformat(formattedException);
            success = true;
        } finally {
            OUT.finishTest("7.3.1: Non-extant bucket", success);
        }
    }

    @Test
    public void test_7_3_2_access_bucket_wrong_user() throws Exception {
        final String bucketName = "test_wrong_user_bucket";
        OUT.startNewTest("7.3.2: Access bucket wrong user");
        boolean success = false;
        try {
            final String  noRightsUserSecretKey = CertificationUtil.getUserSecretKey(client, NO_RIGHTS_USERNAME);
            if (noRightsUserSecretKey.isEmpty()) {
                throw new RuntimeException("Must have account for user " + NO_RIGHTS_USERNAME);
            }
            final Credentials badCreds = new Credentials(NO_RIGHTS_USERNAME, noRightsUserSecretKey);
            final String endpoint = System.getenv("DS3_ENDPOINT");
            final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, badCreds).withHttps(false).build();

            final CommandResponse createBucketResponse = OUT.runCommand(client, "--http -c put_bucket -b " + bucketName);;
            OUT.insertLog("CommandResponse for creating a bucket:");
            OUT.insertPreformat(createBucketResponse.getMessage());
            assertThat(createBucketResponse.getReturnCode(), is(0));

            OUT.runCommand(invalid_client, "--http -c get_bucket -b " + bucketName);;

        } catch(final FailedRequestException e) {
            final String formattedException = FailedRequestExceptionHandler.format(e);
            final String expectedError = "permissions / authorization error";
            assertThat(formattedException, containsString(expectedError));
            OUT.insertLog("CommandResponse for 7.3.2 failed attempt to list bucket with invalid user credentials:");
            OUT.insertPreformat(formattedException);
            success = true;
        } finally {
            OUT.finishTest("7.3.2: Access bucket wrong user", success);
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void test_7_4_get_nonexistent_object() throws Exception {
        final String bucketName = "test_get_nonexistent_object";
        OUT.startNewTest("7.4: Non-extant object");
        boolean success = false;

        try {
            OUT.insertLog("Create bucket.");
            final CommandResponse createBucketResponse = OUT.runCommand(client, "--http -c put_bucket -b " + bucketName);
            assertThat(createBucketResponse.getReturnCode(), is(0));

            OUT.insertLog("List bucket.");
            final CommandResponse getBucketResponse = OUT.runCommand(client, "--http -c get_bucket -b " + bucketName);
            assertThat(getBucketResponse.getReturnCode(), is(0));

            OUT.insertLog("Get non-extant object");
            OUT.runCommand(client, "--http -c get_object -o not_there -b " + bucketName);

        } catch(final FailedRequestException fre) {
            final String formattedException = FailedRequestExceptionHandler.format(fre);
            final String expectedError = "not found";
            assertThat(formattedException, containsString(expectedError));
            OUT.insertCommandOutput("CommandResponse for 7.4 failed attempt to get nonexistent object:", formattedException);
            success = true;

        } finally {
            OUT.finishTest("7.4: Non-extant object", success);
            Util.deleteBucket(client, bucketName);
        }
    }

    /**
     * 7.5-7: Archive and Restore 3 objects larger than the chunk size to BP simultaneously.
     * @throws Exception
     */
    @Test
    public void test_7_5_and_6_bulk_performance_3x110GB() throws Exception {
        final String testDescription = "3x110GB";
        final Integer numFiles = 3;
        final Long fileSize = 110 * GB_BYTES;
        OUT.startNewTest("7.5.6: Bulk performance 3 x 110GB");
        boolean success = testBulkPutAndBulkGetPerformance(testDescription, numFiles, fileSize);
        OUT.finishTest("7.5.6: Bulk performance 3 x 110GB", success);
    }

    /**
     * 7.7-8: Archive 250 objects of approximately 1GB size to BP.
     * @throws Exception
     */
    @Test
    public void test_7_7_and_8_bulk_performance_250x1GB() throws Exception {
        final String testDescription = "250x1GB";
        final Integer numFiles = 250;
        final Long fileSize = 1 * GB_BYTES;
        OUT.startNewTest("7.7.8: Bulk performance 250 x 1GB");
        boolean success = testBulkPutAndBulkGetPerformance(testDescription, numFiles, fileSize);
        OUT.finishTest("7.7.8: Bulk performance 250 x 1GB", success);
    }

    private static boolean testBulkPutAndBulkGetPerformance(
            final String testDescription,
            final Integer numFiles,
            final Long fileSize) throws Exception {
        final String bucketName = "test_bulk_performance_" + testDescription;

        boolean success = false;
        try {
            // Start BULK_PUT
            OUT.insertLog("Create bucket.");
            final CommandResponse createBucketResponse = OUT.runCommand(client, "--http -c put_bucket -b " + bucketName);
            assertThat(createBucketResponse.getReturnCode(), is(0));

            OUT.insertLog("List bucket.");
            final CommandResponse getBucketResponse = OUT.runCommand(client, "--http -c get_bucket -b " + bucketName);
            assertThat(getBucketResponse.getReturnCode(), is(0));

            // Create temp files for BULK_PUT
            final Path bulkPutLocalTempDir = CertificationUtil.createTempFiles(bucketName, numFiles, fileSize);

            final CommandResponse putBulkResponse = OUT.runCommand(client, "--http -c put_bulk -b " + bucketName + " -d "  + bulkPutLocalTempDir.toString());
            OUT.insertCommandOutput("CommandResponse for put_bulk:", putBulkResponse.getMessage());
            assertThat(putBulkResponse.getReturnCode(), is(0));

            final CommandResponse getBucketResponseAfterBulkPut = OUT.runCommand(client, "--http -c get_bucket -b " + bucketName);
            OUT.insertCommandOutput("CommandResponse for listing contents of bucket: " + bucketName, getBucketResponseAfterBulkPut.getMessage());
            assertThat(getBucketResponseAfterBulkPut.getReturnCode(), is(0));

            // Free up disk space
            FileUtils.forceDelete(bulkPutLocalTempDir.toFile());

            // Start BULK_GET from the same bucket that we just did the BULK_PUT to, with a new local directory
            final Path bulkGetLocalTempDir = Files.createTempDirectory(bucketName);

            final CommandResponse getBulkResponse = Util.getBulk(client, bucketName, bulkGetLocalTempDir.toString());
            LOG.info("CommandResponse for BULK_GET: \n{}", getBulkResponse.getMessage());
            assertThat(getBulkResponse.getReturnCode(), is(0));
            success = true;

            // Free up disk space
            FileUtils.forceDelete(bulkGetLocalTempDir.toFile());
        } finally {
            Util.deleteBucket(client, bucketName);
        }
        return success;
    }

}
