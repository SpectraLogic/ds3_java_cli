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
import org.junit.Test;
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
            LOG.info("CommandResponse for 7.2.2 invalid endpoint: \n{}", uhe.getMessage());
        }
    }

    @Test
    public void test_7_3_1_list_nonexistent_bucket() throws Exception {
        final String bucketName = "test_list_nonexistent_bucket";
        try {
            Util.getBucket(client, bucketName);
        } catch(final CommandException ce) {
            assertThat(ce.getMessage(), containsString("Error: Unknown bucket."));
            LOG.info("CommandResponse for 7.3.1 failed attempt to list nonexistent bucket: \n{}", ce.getMessage());
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
            LOG.info("CommandResponse for 7.3.2 failed attempt to list bucket with invalid user credentials: \n{}", ice.getMessage());
        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void test_7_4_get_nonexistent_object() throws Exception {
        final String bucketName = "test_get_nonexistent_object";
        try {
            final CommandResponse createBucketResponse = Util.createBucket(client, bucketName);
            LOG.info("CommandResponse for creating a bucket: \n{}", createBucketResponse.getMessage());
            assertThat(createBucketResponse.getReturnCode(), is(0));

            final CommandResponse getBucketResponse = Util.getBucket(client, bucketName);
            LOG.info("CommandResponse for listing contents of bucket test_get_nonexistent_object: \n{}", getBucketResponse.getMessage());
            assertThat(getBucketResponse.getReturnCode(), is(0));

            final CommandResponse getNonexistentObjectResponse = Util.getObject(client, bucketName, "nonexistent_object");
            LOG.info("CommandResponse for getting nonexistent object from bucket test_get_nonexistent_object: \n{}", getNonexistentObjectResponse.getMessage());
            assertThat(getNonexistentObjectResponse.getReturnCode(), is(404));

        } catch(final FailedRequestException fre) {
            final String expectedError = "Expected a status code of 200, 204 but got 404. Error message: \"NotFound[404]: Objects do not exist: [nonexistent_object]\"";
            assertThat(fre.getMessage(), containsString(expectedError));
            LOG.info("CommandResponse for 7.4 failed attempt to get nonexistent object: \n{}", fre.getMessage());

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    /**
     * Combine the BULK_PUT and BULK_GET performance tests for efficiency.
     * @throws Exception
     */
    @Test
    public void test_7_5_and_6_bulk_performance_3x110GB() throws Exception {
        final String bucketName = "test_bulk_performance_3x110GB";
        final Integer numFiles = 3;
        final Long fileSize = 110 * 1073741824L;
        Path bulkPutLocalTempDir = null;
        try {
            // Create the test bucket to put the objects in
            final CommandResponse createBucketResponse = Util.createBucket(client, bucketName);
            LOG.info("CommandResponse for creating a bucket: \n{}", createBucketResponse.getMessage());
            assertThat(createBucketResponse.getReturnCode(), is(0));

            final CommandResponse getBucketResponse = Util.getBucket(client, bucketName);
            LOG.info("CommandResponse for listing contents of bucket test_get_nonexistent_object: \n{}", getBucketResponse.getMessage());
            assertThat(getBucketResponse.getReturnCode(), is(0));

            // Create 3 110GB files for BULK_PUT
            bulkPutLocalTempDir = createTempFiles(bucketName, numFiles, fileSize);

            // Record start time for BULK_PUT performance calculation
            final Date startTime = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
            final String startFormattedDate = sdf.format(startTime);
            LOG.info("Start datestamp for BULK_PUT 3x110GB objects[{}]\n", startFormattedDate);

            final CommandResponse putBulkResponse = Util.putBulk(client, bucketName, bulkPutLocalTempDir.toString());
            LOG.info("CommandResponse for put_bulk: \n{}", putBulkResponse.getMessage());
            assertThat(putBulkResponse.getReturnCode(), is(0));

            final Date endTime = new Date();
            final String endFormattedDate = sdf.format(endTime);
            LOG.info("End datestamp for BULK_PUT 3x110GB objects[{}]\n", endFormattedDate);

            // Calculate BULK_PUT performance
            final long durationSeconds = new Date(endTime.getTime() - startTime.getTime()).getTime() * 1000;
            final long transferrerdMb= fileSize / 1024L / 1024L;
            final long bulkPutMbps = transferrerdMb / durationSeconds;
            LOG.info("Duration in seconds for BULK_PUT 3x110GB objects[{}]\n", durationSeconds);
            LOG.info("Performance in mb/s for BULK_PUT 3x110GB objects[{}]\n", bulkPutMbps);


            final CommandResponse getBucketResponse2 = Util.getBucket(client, bucketName);
            LOG.info("CommandResponse for listing contents of bucket test_put_bulk: \n{}", getBucketResponse2.getMessage());
            assertThat(getBucketResponse2.getReturnCode(), is(0));

            // Record start time for BULK_GET performance calculation

        } finally {
            Util.deleteBucket(client, bucketName);
            if (bulkPutLocalTempDir != null) {
              FileUtils.forceDeleteOnExit(bulkPutLocalTempDir.toFile());
            }
        }
    }

    @Test
    public void test_7_5_bulk_get_3x110GB() throws Exception {
        final String bucketName = "test_get_bulk_3x110GB_performance";
        //final Integer numFiles = 3;
        //final Long fileSize = 110 * 1073741824L;
        Path tempDir = null;
        try {
            final CommandResponse createBucketResponse = Util.createBucket(client, bucketName);
            LOG.info("CommandResponse for creating a bucket: \n{}", createBucketResponse.getMessage());
            assertThat(createBucketResponse.getReturnCode(), is(0));

            final CommandResponse getBucketResponse = Util.getBucket(client, bucketName);
            LOG.info("CommandResponse for listing contents of bucket test_get_nonexistent_object: \n{}", getBucketResponse.getMessage());
            assertThat(getBucketResponse.getReturnCode(), is(0));

            // create local directory for bulk_get
            tempDir = Files.createTempDirectory("bulk_get_3x110GB");
            //tempDir = createTempFiles("bulk_put_3x110GB", numFiles, fileSize);

            final Date startTime = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
            final String startFormattedDate = sdf.format(startTime);
            LOG.info("Start datestamp for BULK_PUT 3x110GB objects[{}]\n", startFormattedDate);

            final CommandResponse putBulkResponse = Util.putBulk(client, bucketName, tempDir.toString());
            LOG.info("CommandResponse for put_bulk: \n{}", putBulkResponse.getMessage());
            assertThat(putBulkResponse.getReturnCode(), is(0));

            final Date endTime = new Date();
            final String endFormattedDate = sdf.format(endTime);
            LOG.info("Start datestamp for BULK_PUT 3x110GB objects[{}]\n", endFormattedDate);

            final CommandResponse getBucketResponse2 = Util.getBucket(client, bucketName);
            LOG.info("CommandResponse for listing contents of bucket test_put_bulk: \n{}", getBucketResponse2.getMessage());
            assertThat(getBucketResponse2.getReturnCode(), is(0));

        } finally {
            Util.deleteBucket(client, bucketName);
            if (tempDir != null) {
                FileUtils.forceDeleteOnExit(tempDir.toFile());
            }
        }
    }

   private static Path createTempFiles(final String prefix, final int numFiles, final long length) throws IOException {
       final Path tempDir = Files.createTempDirectory(prefix);
       for(int fileNum = 0; fileNum < numFiles; fileNum++) {
           final File tempFile = new File(tempDir.toString(), prefix + "_" + fileNum);
           final RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
           raf.seek(length);
           raf.writeBytes("end of RandomAccessFile.");
           raf.close();
       }
       return tempDir;
    }

}
