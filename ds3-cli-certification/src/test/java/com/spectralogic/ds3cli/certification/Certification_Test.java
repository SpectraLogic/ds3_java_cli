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
import com.spectralogic.ds3cli.exceptions.*;
import com.spectralogic.ds3cli.helpers.Util;
import com.spectralogic.ds3cli.helpers.TempStorageIds;
import com.spectralogic.ds3cli.helpers.TempStorageUtil;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.Quiesced;
import com.spectralogic.ds3client.models.common.Credentials;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;


/**
 * Implement tests to automate the BlackPearl Certification process for the JavaCLI.
 *
 * For details, refer to
 *   https://developer.spectralogic.com/certification/
 *   https://developer.spectralogic.com/test-plan/
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Order only matters for manually verifying the results
public class Certification_Test {
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(Certification_Test.class);
    private static final Ds3Client client = Ds3ClientBuilder.fromEnv().withHttps(false).build();
    private static final Ds3ClientHelpers HELPERS = Ds3ClientHelpers.wrap(client);
    private static final String TEST_ENV_NAME = "JavaCLI_Certification_Test";
    private static final String NO_RIGHTS_USERNAME = "no_rights_user";
    private static final Long GB_BYTES = 1073741824L;
    private static TempStorageIds envStorageIds;
    private static UUID envDataPolicyId;
    private static CertificationWriter OUT;

    private final static Ds3ExceptionHandlerMapper EXCEPTION = Ds3ExceptionHandlerMapper.getInstance();
    static {
        EXCEPTION.addHandler(FailedRequestException.class, new FailedRequestExceptionHandler());
        EXCEPTION.addHandler(RuntimeException.class, new RuntimeExceptionHandler());
    }

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
        final String testDescription = "7.1: Create Bucket";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        OUT.startNewTest(testDescription);
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
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void test_7_2_1_invalid_credentials() throws Exception {
        final String testDescription = "7.2.1: Invalid Credentials";
        final String endpoint = System.getenv("DS3_ENDPOINT");
        final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
        final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).withHttps(false).build();
        OUT.startNewTest(testDescription);
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
            OUT.finishTest(testDescription, success);
        }
    }

    @Test
    public void test_7_2_2_invalid_endpoint() throws Exception {
        final String testDescription = "7.2.2: Invalid Endpoint";
        final String endpoint = "invalid_endpoint";
        final Credentials creds = new Credentials("invalid_access_id", "invalid_secret_key");
        final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, creds).build();
        OUT.startNewTest(testDescription);
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
            OUT.finishTest(testDescription, success);
        }
    }

    @Test
    public void test_7_3_1_list_nonexistent_bucket() throws Exception {
        final String testDescription = "7.3.1: Non-extant bucket";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        OUT.startNewTest("");
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
            OUT.finishTest(testDescription, success);
        }
    }

    @Test
    public void test_7_3_2_access_bucket_wrong_user() throws Exception {
        final String testDescription = "7.3.2: Access bucket wrong user";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        OUT.startNewTest(testDescription);
        boolean success = false;
        try {
            // Create a bucket
            final CommandResponse createBucketResponse = OUT.runCommand(client, "--http -c put_bucket -b " + bucketName);
            assertThat(createBucketResponse.getReturnCode(), is(0));

            // Create a new user, and wrap it with a new client
            final DelegateCreateUserSpectraS3Request createUserRequest = new DelegateCreateUserSpectraS3Request(NO_RIGHTS_USERNAME);
            final DelegateCreateUserSpectraS3Response createUserResponse = client.delegateCreateUserSpectraS3(createUserRequest);
            final String noRightsUserSecretKey = createUserResponse.getSpectraUserResult().getSecretKey();
            final Credentials badCreds = new Credentials(NO_RIGHTS_USERNAME, noRightsUserSecretKey);
            final String endpoint = System.getenv("DS3_ENDPOINT");
            final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, badCreds).withHttps(false).build();

            // Attempt to access the bucket with the new user, which should fail
            OUT.runCommand(invalid_client, "--http -c get_bucket -b " + bucketName);
        } catch(final FailedRequestException e) {
            final String formattedException = FailedRequestExceptionHandler.format(e);
            final String expectedError = "permissions / authorization error";
            assertThat(formattedException, containsString(expectedError));
            OUT.insertLog("CommandResponse for 7.3.2 failed attempt to list bucket with invalid user credentials:");
            OUT.insertPreformat(formattedException);
            success = true;
        } finally {
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
            client.delegateDeleteUserSpectraS3(new DelegateDeleteUserSpectraS3Request(NO_RIGHTS_USERNAME));
        }
    }

    @Test
    public void test_7_4_get_nonexistent_object() throws Exception {
        final String testDescription = "7.4: Non-extant object";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        OUT.startNewTest(testDescription);
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
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
        }
    }

    /**
     * 7.5-7: Archive and Restore 3 objects larger than the chunk size to BP simultaneously.
     * @throws Exception
     */
    @Test
    public void test_7_5_and_6_bulk_performance_3x110GB() throws Exception {
        final Integer numFiles = 3;
        final Long fileSize = 110 * GB_BYTES;
        final String testDescription = "7.5 & 7.6: Bulk Performance 3x110GB";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        OUT.startNewTest(testDescription);
        final boolean success = testBulkPutAndBulkGetPerformance(bucketName, numFiles, fileSize);
        OUT.finishTest(testDescription, success);
    }

    /**
     * 7.7-8: Archive 250 objects of approximately 1GB size to BP.
     * @throws Exception
     */
    @Test
    public void test_7_7_and_8_bulk_performance_250x1GB() throws Exception {
        final Integer numFiles = 3;
        final Long fileSize = 110 * GB_BYTES;
        final String testDescription = "7.5 & 7.6: Bulk Performance 250X1GB";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        OUT.startNewTest(testDescription);
        final boolean success = testBulkPutAndBulkGetPerformance(bucketName, numFiles, fileSize);
        OUT.finishTest(testDescription, success);
    }

    /**
     * 7.9: Test "cache full" by explicitly lowering cache pool size and do not allow cache to drain by quiescing any
     * tape partitions.
     */
    @Test
    public void test_7_9_cache_full() throws Exception {
        final String testDescription = "7.9: Cache Full for Bulk PUT";
        final String bucketName = "testCacheFull";
        final int numFiles = 175;
        final long fileSize = GB_BYTES;
        final long cacheLimit = 150 * GB_BYTES;

        //*** Assume there is a valid Tape Partition ***
        final GetTapePartitionsSpectraS3Request getTapePartitions = new GetTapePartitionsSpectraS3Request();
        final GetTapePartitionsSpectraS3Response getTapePartitionsResponse= client.getTapePartitionsSpectraS3(getTapePartitions);
        assumeThat(getTapePartitionsResponse.getTapePartitionListResult().getTapePartitions().size(), is(greaterThan(0)));

        OUT.startNewTest(testDescription);

        // Quiesce Tape Partition
        try {
            // Must transition state from NO -> PENDING -> YES
            ModifyAllTapePartitionsSpectraS3Response modAllTapePartitionsResponse = client.modifyAllTapePartitionsSpectraS3(new ModifyAllTapePartitionsSpectraS3Request(Quiesced.PENDING));
            assertThat(modAllTapePartitionsResponse.getStatusCode(), is(204));
            modAllTapePartitionsResponse = client.modifyAllTapePartitionsSpectraS3(new ModifyAllTapePartitionsSpectraS3Request(Quiesced.YES));
            assertThat(modAllTapePartitionsResponse.getStatusCode(), is(204));
        } catch (final FailedRequestException fre) {
            // Ignore Request failure if no tape partitions or they are already quiesced
        }

        // Find s3cachefilesystem ID
        final GetCacheFilesystemsSpectraS3Response getCacheFilesystemsSpectraS3Response= client.getCacheFilesystemsSpectraS3(new GetCacheFilesystemsSpectraS3Request());
        final UUID cacheFsId = getCacheFilesystemsSpectraS3Response.getCacheFilesystemListResult().getCacheFilesystems().get(0).getId();
        final GetCacheFilesystemSpectraS3Request getCacheFs = new GetCacheFilesystemSpectraS3Request(cacheFsId.toString());

        // Lower s3cachefilesystem to 150GB
        final ModifyCacheFilesystemSpectraS3Request limitCacheFsRequest = new ModifyCacheFilesystemSpectraS3Request(cacheFsId.toString()).withMaxCapacityInBytes(cacheLimit);
        final ModifyCacheFilesystemSpectraS3Response limitCacheFsResponse = client.modifyCacheFilesystemSpectraS3(limitCacheFsRequest);
        assertThat(limitCacheFsResponse.getResponse().getStatusCode(), is(200));
        assertThat(client.getCacheFilesystemSpectraS3(getCacheFs).getCacheFilesystemResult().getMaxCapacityInBytes(), is(cacheLimit));
        OUT.insertLog("Modify Cache Pool size to " +  cacheLimit + "status: " + limitCacheFsResponse.getResponse().getStatusCode());

        // Create bucket
        final CommandResponse createBucketResponse = OUT.runCommand(client, "--http -c put_bucket -b" + bucketName);
        assertThat(createBucketResponse.getReturnCode(), is(0));

        // Transfer 175 1GB files using JavaCLI methods
        final Path fillCacheFiles = CertificationUtil.createTempFiles("cache_full", numFiles, fileSize);

        try {
            final CommandResponse putBulkResponse = OUT.runCommand(client, "--http -c put_bulk -b " + bucketName + " -d " + fillCacheFiles + " -nt 3");
            assertThat(putBulkResponse.getReturnCode(), is(0));

            // Wait for Cache full
        } catch (final IOException ioe) {
            // expect to fail because job can't finish when cache fills and no tapes are available to offload to
            OUT.insertCommandOutput("Caught IOException after BULK_PUT bigger than available cache: ", ioe.toString());
            //final String cacheFullFailureMsg = "cache full";
        } finally {

        }

        // Un-quiesce the tape partition to allow cache offload to tape
        final ModifyAllTapePartitionsSpectraS3Request unQuiesceAllTapePartitions = new ModifyAllTapePartitionsSpectraS3Request(Quiesced.NO);
        final ModifyAllTapePartitionsSpectraS3Response unQuiesceAllTapePartitionsResponse = client.modifyAllTapePartitionsSpectraS3(unQuiesceAllTapePartitions);
        assertThat(unQuiesceAllTapePartitionsResponse.getResponse().getStatusCode(), is(0));
        OUT.insertCommandOutput("Unquiesce Tape Partition: ", unQuiesceAllTapePartitionsResponse.getResponse().getResponseStream().toString());

        // Resume the job
        final CommandResponse allJobsResponse = OUT.runCommand(client, "--http -c get_jobs --json");
        //assertThat(allJobsResponse.getMessage(), containsString("cache_full"));

        //HELPERS.recoverWriteJob()

        // Verify Job finishes
        final CommandResponse completedJobsResponse = OUT.runCommand(client, "--http -c get_jobs --completed");
        //assertThat(allJobsResponse.getMessage(), containsString("cache_full"));

        // Reset cache size to max
        final ModifyCacheFilesystemSpectraS3Request resetCacheFs = new ModifyCacheFilesystemSpectraS3Request(cacheFsId.toString()).withMaxCapacityInBytes(0L);
        //assertThat(client.modifyCacheFilesystemSpectraS3(resetCacheFs).getResponse().getStatusCode(), is(0));
        //assertThat(client.getCacheFilesystemSpectraS3(getCacheFs).getCacheFilesystemResult().getMaxCapacityInBytes(), is(0L));

        OUT.finishTest(testDescription, true);
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
            assertThat(putBulkResponse.getReturnCode(), is(0));

            final CommandResponse getBucketResponseAfterBulkPut = OUT.runCommand(client, "--http -c get_bucket -b " + bucketName);
            assertThat(getBucketResponseAfterBulkPut.getReturnCode(), is(0));

            // Free up disk space
            FileUtils.forceDelete(bulkPutLocalTempDir.toFile());

            // Start BULK_GET from the same bucket that we just did the BULK_PUT to, with a new local directory
            final Path bulkGetLocalTempDir = Files.createTempDirectory(bucketName);

            final CommandResponse getBulkResponse = OUT.runCommand(client, "--http -c get_bulk -b " + bucketName + "-d " + bulkGetLocalTempDir.toString() + "-nt 3");
            OUT.insertCommandOutput(String.format("CommandResponse for BULK_GET from %s to %s", bucketName, bulkGetLocalTempDir), getBulkResponse.getMessage());
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
