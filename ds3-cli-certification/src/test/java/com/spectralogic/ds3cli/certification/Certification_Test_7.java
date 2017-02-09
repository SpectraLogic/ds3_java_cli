/*
 * ******************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.exceptions.Ds3NoMoreRetriesException;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectPutter;
import com.spectralogic.ds3client.models.Job;
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
import java.util.Optional;
import java.util.UUID;

import static com.spectralogic.ds3cli.certification.CertificationUtil.waitForJobComplete;
import static com.spectralogic.ds3cli.certification.CertificationUtil.waitForTapePartitionQuiescedState;
import static com.spectralogic.ds3cli.helpers.TempStorageUtil.verifyAvailableTapePartition;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;


/**
 * Implement tests to automate the BlackPearl Certification process for the JavaCLI.
 *
 * For details, refer to
 *   https://developer.spectralogic.com/certification/
 *   https://developer.spectralogic.com/test-plan/
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Order only matters for manually verifying the results
public class Certification_Test_7 {
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(Certification_Test_7.class);
    private static final Ds3Client client = Ds3ClientBuilder.fromEnv().withHttps(false).withRedirectRetries(1).build();
    private static final String NO_RIGHTS_USERNAME = "no_rights_user";
    private static final Long GB_BYTES = 1073741824L;
    private static CertificationWriter OUT;

    private final static Ds3ExceptionHandlerMapper EXCEPTION = Ds3ExceptionHandlerMapper.getInstance();
    static {
        EXCEPTION.addHandler(FailedRequestException.class, new FailedRequestExceptionHandler());
        EXCEPTION.addHandler(RuntimeException.class, new RuntimeExceptionHandler());
    }

    @BeforeClass
    public static void startup() throws IOException {
        LOG.setLevel(Level.INFO);

        final String fileName = Certification_Test_7.class.getSimpleName() + "_" + new Date().getTime();
        OUT = new CertificationWriter(fileName);

        verifyAvailableTapePartition(client);
    }

    @AfterClass
    public static void teardown() throws IOException {
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
            final String createBucketCmd = "--http -c put_bucket -b " + bucketName;
            final CommandResponse createBucketResponse = Util.command(client, createBucketCmd);
            OUT.insertCommand(createBucketCmd, createBucketResponse.getMessage());
            assertThat(createBucketResponse.getMessage(), is(expectedCreateBucketMsg));

            OUT.insertLog("List all buckets");
            final String listBucketCmd = "--http -c get_service";
            final CommandResponse listBucketResponse = Util.command(client, listBucketCmd);
            OUT.insertCommand(listBucketCmd, listBucketResponse.getMessage());
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
            Util.command(invalid_client, "--http -c get_service");
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
            Util.command(invalid_client, "--http -c get_service");
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
            Util.command(client, "--http -c get_bucket -b " + bucketName);
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
            final String createBucketCmd = "--http -c put_bucket -b " + bucketName;
            final CommandResponse createBucketResponse = Util.command(client, createBucketCmd);
            assertThat(createBucketResponse.getReturnCode(), is(0));

            // Create a new user, and wrap it with a new client
            OUT.insertLog("Create user " + NO_RIGHTS_USERNAME);
            CertificationUtil.createUser(client, NO_RIGHTS_USERNAME);
            final String noRightsUserSecretKey = CertificationUtil.getUserSecretKey(client, NO_RIGHTS_USERNAME);
            final Credentials badCreds = new Credentials(NO_RIGHTS_USERNAME, noRightsUserSecretKey);
            final String endpoint = System.getenv("DS3_ENDPOINT");
            final Ds3Client invalid_client = Ds3ClientBuilder.create(endpoint, badCreds).withHttps(false).build();

            // Attempt to access the bucket with the new user, which should fail
            final String listBucketCmd = "--http -c get_bucket -b " + bucketName;
            Util.command(invalid_client, listBucketCmd);
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
            CertificationUtil.deleteUser(client, NO_RIGHTS_USERNAME);
        }
    }

    @Test
    public void test_7_4_get_nonexistent_object() throws Exception {
        final String testDescription = "7.4: Non-extant object";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        OUT.startNewTest(testDescription);
        boolean success = false;

        try {
            final String createBucketCmd = "--http -c put_bucket -b " + bucketName;
            final CommandResponse createBucketResponse = Util.command(client, createBucketCmd);
            OUT.insertCommand(createBucketCmd, createBucketResponse.getMessage());
            assertThat(createBucketResponse.getReturnCode(), is(0));

            final String getBucketCmd = "--http -c get_bucket -b " + bucketName;
            final CommandResponse getBucketResponse = Util.command(client, getBucketCmd);
            OUT.insertCommand(getBucketCmd, getBucketResponse.getMessage());
            assertThat(getBucketResponse.getReturnCode(), is(0));

            OUT.insertLog("Get non-extant object");
            final String getNonExtantObject = "--http -c get_object -o not_there -b " + bucketName;
            final CommandResponse getNonExtandObjectResponse = Util.command(client, getNonExtantObject);
            OUT.insertCommand(getNonExtantObject, getNonExtandObjectResponse.getMessage());

        } catch(final FailedRequestException fre) {
            final String formattedException = FailedRequestExceptionHandler.format(fre);
            final String expectedError = "not found";
            assertThat(formattedException, containsString(expectedError));
            OUT.insertLog("CommandResponse for 7.4 failed attempt to get nonexistent object:" + formattedException);
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
     * tape partitions, and then resume after unquiescing tape partitions to allow the job to finish.
     */
    @Test
    public void test_7_9_cache_full() throws Exception {
        final String testDescription = "7.9: Cache Full for Bulk PUT";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        final int numFiles = 175;
        final long fileSize = GB_BYTES;
        final long cacheLimit = 150 * GB_BYTES;

        // Assume there is a valid Tape Partition
        final GetTapePartitionsSpectraS3Request getTapePartitions = new GetTapePartitionsSpectraS3Request();
        final GetTapePartitionsSpectraS3Response getTapePartitionsResponse= client.getTapePartitionsSpectraS3(getTapePartitions);
        assumeThat(getTapePartitionsResponse.getTapePartitionListResult().getTapePartitions().size(), is(greaterThan(0)));
        final UUID tapePartitionId = getTapePartitionsResponse.getTapePartitionListResult().getTapePartitions().get(0).getId();

        OUT.startNewTest(testDescription);

        // Quiesce Tape Partition
        try {
            // Must transition state from NO -> PENDING -> YES
            OUT.insertLog("Modify all Tape Partitions to Quiesced.PENDING");
            client.modifyAllTapePartitionsSpectraS3(new ModifyAllTapePartitionsSpectraS3Request(Quiesced.PENDING));
            OUT.insertLog("Modify all Tape Partitions to Quiesced.YES");
            client.modifyAllTapePartitionsSpectraS3(new ModifyAllTapePartitionsSpectraS3Request(Quiesced.YES));
            waitForTapePartitionQuiescedState(client, tapePartitionId, Quiesced.YES);
        } catch (final FailedRequestException fre) {
            // Ignore Request failure if tape partitions are already quiesced
            LOG.info("Failed to Quiesce tape partitions: {}", fre.getMessage());
        }

        // Find s3cachefilesystem ID
        OUT.insertLog("Find CacheFilesystem UUID");
        final UUID cacheFsId = client.getCacheFilesystemsSpectraS3(new GetCacheFilesystemsSpectraS3Request()).getCacheFilesystemListResult().getCacheFilesystems().get(0).getId();
        final GetCacheFilesystemSpectraS3Request getCacheFs = new GetCacheFilesystemSpectraS3Request(cacheFsId.toString());

        // Lower s3cachefilesystem max capacity to 150GB
        OUT.insertLog("Lower CacheFilesystem capacity to 150GB");
        final ModifyCacheFilesystemSpectraS3Request limitCacheFsRequest = new ModifyCacheFilesystemSpectraS3Request(cacheFsId.toString()).withMaxCapacityInBytes(cacheLimit);
        final ModifyCacheFilesystemSpectraS3Response limitCacheFsResponse = client.modifyCacheFilesystemSpectraS3(limitCacheFsRequest);
        assertThat(client.getCacheFilesystemSpectraS3(getCacheFs).getCacheFilesystemResult().getMaxCapacityInBytes(), is(cacheLimit));
        OUT.insertLog("Modify Cache Pool size to " +  cacheLimit + "status: " + limitCacheFsResponse.getCacheFilesystemResult());

        // Create bucket
        final String createBucketCmd = "--http -c put_bucket -b" + bucketName;
        final CommandResponse createBucketResponse = Util.command(client, createBucketCmd);
        OUT.insertCommand(createBucketCmd, createBucketResponse.getMessage());
        assertThat(createBucketResponse.getReturnCode(), is(0));

        // Transfer 175 1GB files using JavaCLI methods
        final Path fillCacheFiles = CertificationUtil.createTempFiles("Cache_Full", numFiles, fileSize);

        try {
            final String putBulkCmd = "--http -c put_bulk -b " + bucketName + " -d " + fillCacheFiles + " -nt 3";
            final CommandResponse putBulkResponse = Util.command(client, putBulkCmd);
            OUT.insertCommand(putBulkCmd, putBulkResponse.getMessage());
            assertThat(putBulkResponse.getReturnCode(), is(0));

            // Wait for Cache full
        } catch (final Ds3NoMoreRetriesException noMoreRetriesException) {
            // expect to fail because job can't finish when cache fills and no tapes are available to offload to
            OUT.insertLog("Caught Ds3NoMoreRetriesException after BULK_PUT bigger than available cache: " + noMoreRetriesException.getMessage());

            final String getJobsCmd = "--http -c get_jobs";
            final CommandResponse getJobsResponse = Util.command(client, getJobsCmd);
            OUT.insertCommand(getJobsCmd, getJobsResponse.getMessage());
        }

        // Un-quiesce the tape partition to allow cache offload to tape
        final ModifyAllTapePartitionsSpectraS3Response unQuiesceAllTapePartitionsResponse = client.modifyAllTapePartitionsSpectraS3(new ModifyAllTapePartitionsSpectraS3Request(Quiesced.NO));
        OUT.insertLog("Unquiesce Tape Partition status: " + unQuiesceAllTapePartitionsResponse);
        if (!waitForTapePartitionQuiescedState(client, tapePartitionId, Quiesced.NO)) {
            OUT.insertLog("Timed out after an hour waiting for cache to drain and job to finish.");
            fail("Timed out waiting for TapePartition " + tapePartitionId + " Quiesced State to change to Quiesced.NO");
        }

        // Resume the job
        final GetJobsSpectraS3Response allJobs = client.getJobsSpectraS3(new GetJobsSpectraS3Request());
        final Optional<Job> bulkJob = allJobs.getJobListResult().getJobs()
                .stream()
                .filter(job -> job.getBucketName().matches(bucketName))
                .findFirst();
        assertTrue(bulkJob.isPresent());
        final UUID jobId = bulkJob.get().getJobId();
        final Ds3ClientHelpers.Job recoverJob = Ds3ClientHelpers.wrap(client, 3).recoverWriteJob(jobId);
        recoverJob.transfer(new FileObjectPutter(fillCacheFiles));

        // Verify Job finishes
        if (!waitForJobComplete(client, jobId)) {
            OUT.insertLog("Timed out after an hour waiting for cache to drain and job to finish.");
            fail("Timed out waiting for PUT job " + jobId.toString() + " State to change to Complete.");
        }

        final CommandResponse completedJobs = Util.command(client, "--http -c get_jobs --completed");
        OUT.insertCommand("Completed Jobs: ", completedJobs.getMessage());
        assertThat(completedJobs.getMessage(), containsString(bucketName));

        // Reset cache size to max
        final ModifyCacheFilesystemSpectraS3Response resetCacheFsResponse = client.modifyCacheFilesystemSpectraS3(new ModifyCacheFilesystemSpectraS3Request(cacheFsId.toString()).withMaxCapacityInBytes(null));
        OUT.insertLog("ModifyCacheFileSystem back to max capacity: " + resetCacheFsResponse.getCacheFilesystemResult().getMaxCapacityInBytes().toString());
        assertThat(resetCacheFsResponse.getCacheFilesystemResult().getMaxCapacityInBytes(), is(greaterThan(cacheLimit)));

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
            final String createBucketCmd = "--http -c put_bucket -b " + bucketName;
            final CommandResponse createBucketResponse = Util.command(client, createBucketCmd);
            OUT.insertCommand(createBucketCmd, createBucketResponse.getMessage());
            assertThat(createBucketResponse.getReturnCode(), is(0));

            OUT.insertLog("List bucket.");
            final String listBucketCmd = "--http -c get_bucket -b " + bucketName;
            final CommandResponse getBucketResponse = Util.command(client, listBucketCmd);
            OUT.insertCommand(listBucketCmd, getBucketResponse.getMessage());
            assertThat(getBucketResponse.getReturnCode(), is(0));

            // Create temp files for BULK_PUT
            OUT.insertLog("Creating ");
            final Path bulkPutLocalTempDir = CertificationUtil.createTempFiles(bucketName, numFiles, fileSize);

            OUT.insertLog("Bulk PUT from bucket " + bucketName);
            final long startPutTime = getCurrentTime();
            final String putBulkCmd = "--http -c put_bulk -b " + bucketName + " -d "  + bulkPutLocalTempDir.toString();
            final CommandResponse putBulkResponse = Util.command(client, putBulkCmd);
            OUT.insertCommand(putBulkCmd, putBulkResponse.getMessage());
            final long endPutTime = getCurrentTime();
            assertThat(putBulkResponse.getReturnCode(), is(0));
            OUT.insertPerformanceMetrics(startPutTime, endPutTime, numFiles * fileSize, true);

            final CommandResponse getBucketResponseAfterBulkPut = Util.command(client, listBucketCmd);
            OUT.insertCommand(listBucketCmd, getBucketResponseAfterBulkPut.getMessage());
            assertThat(getBucketResponseAfterBulkPut.getReturnCode(), is(0));

            // Free up disk space
            FileUtils.forceDelete(bulkPutLocalTempDir.toFile());

            // Start BULK_GET from the same bucket that we just did the BULK_PUT to, with a new local directory
            final Path bulkGetLocalTempDir = Files.createTempDirectory(bucketName);

            final long startGetTime = getCurrentTime();
            OUT.insertLog("Bulk GET from bucket " + bucketName);
            final String getBulkCmd = "--http -c get_bulk -b " + bucketName + " -d " + bulkGetLocalTempDir.toString() + " -nt 3";
            final CommandResponse getBulkResponse = Util.command(client, getBulkCmd);
            OUT.insertCommand(getBulkCmd, getBucketResponse.getMessage());
            final long endGetTime = getCurrentTime();
            OUT.insertPerformanceMetrics(startGetTime, endGetTime, numFiles * fileSize, true);
            assertThat(getBulkResponse.getReturnCode(), is(0));
            success = true;

            // Free up disk space
            FileUtils.forceDelete(bulkGetLocalTempDir.toFile());
        } finally {
            Util.deleteBucket(client, bucketName);
        }
        return success;
    }

    private static long getCurrentTime() {
        return System.currentTimeMillis();
    }
}

