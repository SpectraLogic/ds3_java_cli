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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.exceptions.*;
import com.spectralogic.ds3cli.helpers.TempStorageIds;
import com.spectralogic.ds3cli.helpers.TempStorageUtil;
import com.spectralogic.ds3cli.helpers.Util;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.models.common.Credentials;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.*;

import static com.spectralogic.ds3cli.certification.CertificationUtil.*;
import static com.spectralogic.ds3cli.helpers.TempStorageUtil.setupDataPolicy;
import static com.spectralogic.ds3cli.helpers.TempStorageUtil.verifyAvailableTapePartition;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;


/**
 * Implement tests to automate the BlackPearl Certification process for the JavaCLI.
 * <p>
 * For details, refer to
 * https://developer.spectralogic.com/certification/
 * https://developer.spectralogic.com/test-plan/
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Order only matters for manually verifying the results


public class Certification_Test {
    private static final Logger LOG = LoggerFactory.getLogger(Certification_Test.class);
    private static final Ds3Client client = Ds3ClientBuilder.fromEnv().withHttps(false).withRedirectRetries(1).build();
    private static final Ds3ClientHelpers HELPERS = Ds3ClientHelpers.wrap(client);
    private static final String NO_RIGHTS_USERNAME = "no_rights_user";
    private static final Long GB_IN_BYTES = 1073741824L;
    private static CertificationWriter OUT;
    private static UUID envDataPolicyId;

    private final static Ds3ExceptionHandlerMapper EXCEPTION_MAPPER = Ds3ExceptionHandlerMapper.getInstance();

    static {
        EXCEPTION_MAPPER.addHandler(FailedRequestException.class, new FailedRequestExceptionHandler());
        EXCEPTION_MAPPER.addHandler(RuntimeException.class, new RuntimeExceptionHandler());
    }

    @BeforeClass
    public static void startup() throws IOException {
        final String fileName = Certification_Test.class.getSimpleName() + "_" + new Date().getTime();
        OUT = new CertificationWriter(fileName);
        OUT.writeHeader();

        envDataPolicyId = verifyAvailableTapePartition(client);
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
            assertTrue(testDescription + " did not complete", success);
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
        } catch (final FailedRequestException e) {
            final String formattedException = FailedRequestExceptionHandler.format(e);
            OUT.insertPreformat(formattedException);
            final String expectedError = "permissions / authorization error";
            assertThat(formattedException, containsString(expectedError));
            success = true;
        } finally {
            OUT.finishTest(testDescription, success);
            assertTrue(testDescription + " did not complete", success);
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
        } catch (final UnknownHostException uhe) {
            final String formattedException = ExceptionFormatter.format(uhe);
            final String expectedError = "UnknownHost";
            assertThat(formattedException, containsString(expectedError));
            OUT.insertLog("CommandResponse for 7.2.2 invalid endpoint:");
            OUT.insertPreformat(formattedException);
            success = true;
        } finally {
            OUT.finishTest(testDescription, success);
            assertTrue(testDescription + " did not complete", success);
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
        } catch (final CommandException ce) {
            final String formattedException = ExceptionFormatter.format(ce);
            assertThat(formattedException, containsString("Error: Unknown bucket."));
            OUT.insertLog("CommandResponse for 7.3.1 failed attempt to list nonexistent bucket:");
            OUT.insertPreformat(formattedException);
            success = true;
        } finally {
            OUT.finishTest(testDescription, success);
            assertTrue(testDescription + " did not complete", success);
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
        } catch (final FailedRequestException e) {
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
            assertTrue(testDescription + " did not complete", success);
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

        } catch (final FailedRequestException fre) {
            final String formattedException = FailedRequestExceptionHandler.format(fre);
            final String expectedError = "not found";
            assertThat(formattedException, containsString(expectedError));
            OUT.insertLog("CommandResponse for 7.4 failed attempt to get nonexistent object:" + formattedException);
            success = true;

        } finally {
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
            assertTrue(testDescription + " did not complete", success);
        }
    }

    /**
     * 7.5-7: Archive and Restore 3 objects larger than the chunk size to BP simultaneously.
     *
     * @throws Exception
     */
    @Test
    public void test_7_5_and_6_bulk_performance_3x110GB() throws Exception {
        final Integer numFiles = 3;
        final Long fileSize = 110 * GB_IN_BYTES;
        final String testDescription = "7.5 & 7.6: Bulk Performance 3x110GB";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        OUT.startNewTest(testDescription);
        final boolean success = testBulkPutAndBulkGetPerformance(bucketName, numFiles, fileSize);
        OUT.finishTest(testDescription, success);
        assertTrue(testDescription + " did not complete", success);
    }

    /**
     * 7.7-8: Archive 250 objects of approximately 1GB size to BP.
     *
     * @throws Exception
     */
    @Test
    public void test_7_7_and_8_bulk_performance_250x1GB() throws Exception {
        final Integer numFiles = 250;
        final Long fileSize = GB_IN_BYTES;
        final String testDescription = "7.7 & 7.8: Bulk Performance 250X1GB";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        OUT.startNewTest(testDescription);
        final boolean success = testBulkPutAndBulkGetPerformance(bucketName, numFiles, fileSize);
        OUT.finishTest(testDescription, success);
        assertTrue(testDescription + " did not complete", success);
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
            final String putBulkCmd = "--http -c put_bulk -b " + bucketName + " -d " + bulkPutLocalTempDir.toString();
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
            final Path bulkGetLocalTempDir = CertificationUtil.createTempDs3Directory(bucketName);

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

    /**
     * 8.1:	Versioning
     */
    @Test
    public void test_8_1_versioning() throws Exception {
        final String testDescription = "8.1: Updating an existing object in BlackPearl";
        final Integer numFiles = 1;
        final Long fileSize = 1024L;
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        boolean success = false;
        final UUID dataPolicy = setupDataPolicy(testDescription, false, ChecksumType.Type.MD5, client);
        final TempStorageIds tempStorageIds = TempStorageUtil.setup(testDescription, dataPolicy, client);


        OUT.startNewTest(testDescription);
        try {

            OUT.insertLog("Set data policy to use versioning");
            final String enableDataPolicyVersioningCmd = "--http -c modify_data_policy --modify-params versioning:KEEP_LATEST -i " + dataPolicy;
            final CommandResponse modifyDataPolicyResponse = Util.command(client, enableDataPolicyVersioningCmd);
            OUT.insertCommand(enableDataPolicyVersioningCmd, modifyDataPolicyResponse.getMessage());
            assertThat(modifyDataPolicyResponse.getReturnCode(), is(0));
            client.modifyUserSpectraS3(new ModifyUserSpectraS3Request("Administrator")
                    .withDefaultDataPolicyId(dataPolicy));

            // create and store one file
            Path bulkPutLocalTempDir = CertificationUtil.createTempFiles(bucketName, numFiles, fileSize);
            final String putInitialCmd = "--http -c put_bulk -b " + bucketName + " -d " + bulkPutLocalTempDir.toString();
            final CommandResponse putInitialResponse = Util.command(client, putInitialCmd);
            OUT.insertCommand(putInitialCmd, putInitialResponse.getMessage());
            assertThat(putInitialResponse.getReturnCode(), is(0));

            OUT.insertLog("List bucket contents");
            final String getBucketCmd = "--http -c get_bucket -b " + bucketName;
            final CommandResponse listInitialContentsResponse = Util.command(client, getBucketCmd);
            OUT.insertCommand(getBucketCmd, listInitialContentsResponse.getMessage());
            assertThat(listInitialContentsResponse.getReturnCode(), is(0));

            // make new object half size, clobber previous
            OUT.insertLog("Put new object of same name but half size");
            bulkPutLocalTempDir = CertificationUtil.createTempFiles(bucketName, numFiles, fileSize / 2);
            final String putNewVersionCmd = "--http --force -c put_bulk -b " + bucketName + " -d " + bulkPutLocalTempDir.toString();
            final CommandResponse putNewVersionResponse = Util.command(client, putNewVersionCmd);
            OUT.insertCommand(putNewVersionCmd, putNewVersionResponse.getMessage());
            assertThat(putNewVersionResponse.getReturnCode(), is(0));

            OUT.insertLog("List bucket contents (half size)");
            final CommandResponse listNewContentsResponse = Util.command(client, getBucketCmd);
            OUT.insertCommand(getBucketCmd, listNewContentsResponse.getMessage());
            assertThat(listNewContentsResponse.getReturnCode(), is(0));

            success = true;
        } catch (final Exception e) {
            LOG.error("Exception: {}", e.getMessage(), e);
        } finally {
            client.modifyUserSpectraS3(new ModifyUserSpectraS3Request("Administrator")
                    .withDefaultDataPolicyId(envDataPolicyId));
            TempStorageUtil.teardown(testDescription, tempStorageIds, client);
            OUT.finishTest(testDescription, success);
            assertTrue(testDescription + " did not complete", success);
        }
    }

    /**
     * 8.2:	Retrieve partial file
     */
    @Test
    public void test_8_2_partial_restore() throws Exception {
        final String testDescription = "8.2: Partial Restore";
        final Integer numFiles = 1;
        final Long fileSize = 1024L;
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        boolean success = false;

        OUT.startNewTest(testDescription);
        try {
            final Path bulkPutLocalTempDir = CertificationUtil.createTempFiles(bucketName, numFiles, fileSize);

            final String putBulkCmd = "--http -c put_bulk -b " + bucketName + " -d " + bulkPutLocalTempDir.toString();
            final CommandResponse putBulkResponse = Util.command(client, putBulkCmd);
            OUT.insertCommand(putBulkCmd, putBulkResponse.getMessage());
            assertThat(putBulkResponse.getReturnCode(), is(0));

            // verify write
            OUT.insertLog("List bucket " + bucketName + "contents");
            final Iterable<Contents> objects = HELPERS.listObjects(bucketName);
            assertFalse(Iterables.isEmpty(objects));
            final String objectName = objects.iterator().next().getKey();
            OUT.insertPreformat(objectName);

            // restore first 100 bytes
            final String getPartialObjectCmd = "--http -c get_object --range-offset 0 --range-length 100 -b " + bucketName + " -o " + objectName + " -d " + bulkPutLocalTempDir.toString();
            final CommandResponse getPartialResponse = Util.command(client, getPartialObjectCmd);
            OUT.insertCommand(getPartialObjectCmd, getPartialResponse.getMessage());
            assertThat(getPartialResponse.getReturnCode(), is(0));

            // check file size
            final List<Path> filesToPut = com.spectralogic.ds3cli.util.FileUtils.listObjectsForDirectory(bulkPutLocalTempDir);
            final com.spectralogic.ds3cli.util.FileUtils.ObjectsToPut objectsToPut = com.spectralogic.ds3cli.util.FileUtils.getObjectsToPut(filesToPut, bulkPutLocalTempDir, "", true);
            final Ds3Object obj = objectsToPut.getDs3Objects().get(0);
            assertTrue(obj.getSize() < 150);
            OUT.insertLog(obj.getName() + " size: " + Long.toString(obj.getSize()));
            success = true;

        } catch (final Exception e) {
            LOG.error("Exception in " + testDescription, e);
        } finally {
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
            assertTrue(testDescription + " did not complete", success);
        }
    }

    /**
     * 8.4: Manually change job priorities
     */
    @Test
    public void test_8_4_change_priorities() throws Exception {
        final String testDescription = "8.4: Manually change job priorities";
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        boolean success = false;
        String jobId = "";

        OUT.startNewTest(testDescription);
        OUT.insertLog("Start Bulk Job with single object, priority LOW");
        try {
            //create the Bucket
            Util.createBucket(client, bucketName);

            //create PUT job but don't start transfer
            jobId = CertificationUtil.createPutJob(client, bucketName, Priority.LOW);

            // show starting ("LOW");
            final String getJobCmd = "--http -c get_job -i " + jobId;
            final CommandResponse getInitialJobResponse = Util.command(client, getJobCmd);
            OUT.insertCommand(getJobCmd, getInitialJobResponse.getMessage());
            assertTrue(getInitialJobResponse.getMessage().contains("| Priority: LOW |"));

            // set priority to URGENT
            OUT.insertLog("Set priority to URGENT");
            final String modifyJobPriorityUrgentCmd = "--http -c modify_job --priority URGENT -i " + jobId;
            final CommandResponse modifyJobResponse = Util.command(client, modifyJobPriorityUrgentCmd);
            OUT.insertCommand(modifyJobPriorityUrgentCmd, modifyJobResponse.getMessage());
            assertTrue(modifyJobResponse.getMessage().contains("| Priority: URGENT |"));
            success = true;

        } catch (final Exception e) {
            LOG.error("Exception in " + testDescription, e);
        } finally {
            OUT.finishTest(testDescription, success);
            CertificationUtil.deleteJob(client, jobId);
            Util.deleteBucket(client, bucketName);
            assertTrue(testDescription + " did not complete", success);
        }
    }


    /**
     * 8.5: Eject a bucket or tape
     */
    @Test
    public void test_8_5_eject_bucket() throws Exception {
        final String testDescription = "8.5: Eject Bucket";
        boolean success = false;
        String barcode = "";
        OUT.startNewTest(testDescription);
        try {
            // Get All Tapes -- once for display
            final String getTapesArgs = "--http -c get_tapes";
            Util.command(client, getTapesArgs);

            // Get a normal tape
            final GetTapesSpectraS3Response response = client.getTapesSpectraS3(new GetTapesSpectraS3Request());

            final Collection<Tape> normalTapes
                    = Collections2.filter(response.getTapeListResult().getTapes(), new Predicate<Tape>() {
                @Override
                public boolean apply(@Nullable final Tape tape) {
                    return (tape.getState() == TapeState.NORMAL || tape.getState() == TapeState.FOREIGN);
                }
            });
            assertFalse(normalTapes.isEmpty());
            barcode = normalTapes.iterator().next().getBarCode();

            OUT.insertLog("Eject tape: " + barcode);
            // Eject
            final String ejectLocation = "Undisclosed_location";
            final String ejectTapesArgs = "--http -c eject_tape --eject-location " + ejectLocation + " -i " + barcode;
            final CommandResponse ejectTapesResponse = Util.command(client, ejectTapesArgs);
            OUT.insertCommand(ejectTapesArgs, ejectTapesResponse.getMessage());

            // Show label
            final CommandResponse postEjectResponse = Util.command(client, getTapesArgs);
            OUT.insertCommand(getTapesArgs, postEjectResponse.getMessage());
            assertTrue(postEjectResponse.getMessage().contains(ejectLocation));
            success = true;
        } catch (final Exception e) {
            LOG.error("Exception in " + testDescription, e);
        } finally {
            CertificationUtil.cancelTapeEject(client, barcode);
            OUT.finishTest(testDescription, success);
            assertTrue(testDescription + " did not complete", success);
        }
    }

    /**
     * 8.6: Show a job is fully persisted to tape or disk archive location
     */
    @Test
    public void test_8_6_fully_persisted() throws Exception {
        final String testDescription = "8.6: Show Fully Persisted";
        final Integer numFiles = 6;
        final Long fileSize = 1 * GB_IN_BYTES;
        final String bucketName = CertificationUtil.getBucketName(testDescription);
        boolean success = false;

        OUT.startNewTest(testDescription);
        try {
            // Assume there is a valid Tape Partition
            final UUID tapePartitionId = getValidTapePartition(client);
            assumeThat(tapePartitionId, is(notNullValue()));

            // Un-quiesce the tape partition to allow cache offload to tape
            if (!ensureTapePartitionQuiescedState(client, tapePartitionId, Quiesced.NO)) {
                final String tapePartitionStateChangeErrorMsg = "Timed out waiting for TapePartition " + tapePartitionId + " Quiesced State to change to Quiesced.NO";
                OUT.insertLog(tapePartitionStateChangeErrorMsg);
                fail(tapePartitionStateChangeErrorMsg);
            }

            // Put files into bucket
            // Create temp files for BULK_PUT
            final Path bulkPutLocalTempDir = CertificationUtil.createTempFiles(bucketName, numFiles, fileSize);

            final String putBulkCmd = "--http -c put_bulk -b " + bucketName + " -d " + bulkPutLocalTempDir.toString();
            final CommandResponse putBulkResponse = Util.command(client, putBulkCmd);
            assertThat(putBulkResponse.getReturnCode(), is(0));

            // verify write
            OUT.insertLog("List bucket " + bucketName + "contents");
            final Iterable<Contents> objects = HELPERS.listObjects(bucketName);
            assertFalse(Iterables.isEmpty(objects));
            final String objectName = objects.iterator().next().getKey();
            OUT.insertPreformat(objectName);

            // Show not persisted
            OUT.insertLog("Show not persisted");
            final String getPhysicalPlacementCmd = "--http -c get_detailed_objects_physical -b " + bucketName;
            final CommandResponse getPhysicalPlacementBeforeResponse = Util.command(client, getPhysicalPlacementCmd);
            OUT.insertCommand(getPhysicalPlacementCmd, getPhysicalPlacementBeforeResponse.getMessage());

            // force to tape
            OUT.insertLog("Reclaim cache (force to tape)");
            final String reclaimCacheArgs = "--http -c reclaim_cache";
            final CommandResponse reclaimResponse = Util.command(client, reclaimCacheArgs);
            OUT.insertCommand(reclaimCacheArgs, reclaimResponse.getMessage());
            assertThat(reclaimResponse.getReturnCode(), is(0));

            // Poll until all Show persisted
            OUT.insertLog("Show as persisted");
            LOG.info("Poll get_detailed_objects_physical to block until it is written to tape.");
            success = CertificationUtil.waitForPhysicalWrite(client, bucketName);

            // now log the run
            final CommandResponse getPhysicalPlacementAfterResponse = Util.command(client, getPhysicalPlacementCmd);
            OUT.insertCommand(getPhysicalPlacementCmd, getPhysicalPlacementAfterResponse.getMessage());
        } catch (final Exception e) {
            LOG.error("Exception in " + testDescription, e);
        } finally {
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
            assertTrue(testDescription + " did not complete", success);
        }
    }

    /**
     * 8.7: If application can list objects in a bucket, show that it can handle (paginate) a large list
     */
    @Test
    public void test_8_7_large_list() throws Exception {
        final String testDescription = "8.7: Large List";
        final int numFiles = 500;
        final long fileSize = 1L;
        final String bucketName = CertificationUtil.getBucketName(testDescription);

        OUT.startNewTest(testDescription);
        boolean success = false;
        try {
            // Put 500 files into bucket
            final CommandResponse performanceResponse = Util.putPerformanceFiles(client, bucketName, numFiles, fileSize);
            assertThat(performanceResponse.getReturnCode(), is(0));

            final String listBucketArgs = "--http -c get_bucket -b " + bucketName;
            final CommandResponse getBucketResponseAfterBulkPut = Util.command(client, listBucketArgs);
            OUT.insertCommand(listBucketArgs, getBucketResponseAfterBulkPut.getMessage());
            assertThat(getBucketResponseAfterBulkPut.getReturnCode(), is(0));
            success = true;
        } catch (final Exception e) {
            LOG.error("Exception in " + testDescription, e);
        } finally {
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
            assertTrue(testDescription + " did not complete", success);
        }
    }

}

