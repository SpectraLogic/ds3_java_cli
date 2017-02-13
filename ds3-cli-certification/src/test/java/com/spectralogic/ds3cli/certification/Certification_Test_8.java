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
import com.spectralogic.ds3cli.command.PutBulk;
import com.spectralogic.ds3cli.exceptions.*;
import com.spectralogic.ds3cli.helpers.TempStorageIds;
import com.spectralogic.ds3cli.helpers.TempStorageUtil;
import com.spectralogic.ds3cli.helpers.Util;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.commands.spectrads3.GetJobsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetJobsSpectraS3Response;
import com.spectralogic.ds3client.commands.spectrads3.GetTapesSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetTapesSpectraS3Response;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.cglib.core.CollectionUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static com.spectralogic.ds3cli.certification.CertificationUtil.ensureTapePartitionQuiescedState;
import static com.spectralogic.ds3cli.certification.CertificationUtil.getValidTapePartition;
import static com.spectralogic.ds3cli.certification.CertificationUtil.waitForJobComplete;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
public class Certification_Test_8 {

    private static final Logger LOG =  (Logger) LoggerFactory.getLogger(Certification_Test_8.class);
    private static final Ds3Client client = Ds3ClientBuilder.fromEnv().withHttps(false).build();
    private static final Ds3ClientHelpers HELPERS = Ds3ClientHelpers.wrap(client);
    private static final String TEST_ENV_NAME = "JavaCLI_Certification_Test_Section_8";
    private static TempStorageIds envStorageIds;
    private static UUID envDataPolicyId;
    private static CertificationWriter OUT;

    private static final Long GB_BYTES = 1073741824L;

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
        final String fileName = Certification_Test_8.class.getSimpleName() + "_" + new Date().getTime();
        OUT = new CertificationWriter(fileName);
    }

    @AfterClass
    public static void teardown() throws IOException {
        TempStorageUtil.teardown(TEST_ENV_NAME, envStorageIds, client);
        client.close();
        OUT.close();
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

        OUT.startNewTest(testDescription);
        try {
            OUT.insertLog("Set data policy to use versioning");
            final String enableDataPolicyVersioningCmd = "--http -c modify_data_policy --modify-params versioning:KEEP_LATEST -i " + envDataPolicyId;
            final CommandResponse modifyDataPolicyResponse = Util.command(client, enableDataPolicyVersioningCmd);
            OUT.insertCommand(enableDataPolicyVersioningCmd, modifyDataPolicyResponse.getMessage());
            assertThat(modifyDataPolicyResponse.getReturnCode(), is(0));

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
            LOG.info("Exception: {}", e.getMessage(), e);
        } finally {
            Util.deleteBucket(client, bucketName);

            // undo versioning
            Util.command(client, "--http -c modify_data_policy --modify-params versioning:NONE -i " + envDataPolicyId);

            OUT.finishTest(testDescription, success);
            assertTrue(success);
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

            final String putBulkCmd = "--http -c put_bulk -b " + bucketName + " -d "  + bulkPutLocalTempDir.toString();
            final CommandResponse putBulkResponse = Util.command(client, putBulkCmd);
            OUT.insertCommand(putBulkCmd, putBulkResponse.getMessage());
            assertThat(putBulkResponse.getReturnCode(), is(0));

            // verify write
            OUT.insertLog("List bucket " + bucketName + "contents");
            final Iterator<Contents> objects = HELPERS.listObjects(bucketName).iterator();
            assertTrue(objects.hasNext());
            final String objectName = objects.next().getKey();
            OUT.insertPreformat(objectName);

            // restore first 100 bytes
            final String getPartialObjectCmd = "--http -c get_object --range-offset 0 --range-length 100 -b " + bucketName + " -o " + objectName + " -d "  + bulkPutLocalTempDir.toString();
            final CommandResponse getPartialResponse = Util.command(client, getPartialObjectCmd);
            OUT.insertCommand(getPartialObjectCmd, getPartialResponse.getMessage());
            assertThat(getPartialResponse.getReturnCode(), is(0));

            // check file size
            final List<Path> filesToPut = FileUtils.listObjectsForDirectory(bulkPutLocalTempDir);
            final PutBulk.ObjectsToPut objectsToPut = FileUtils.getObjectsToPut(filesToPut, bulkPutLocalTempDir, true);
            final Ds3Object obj = objectsToPut.getDs3Objects().get(0);
            assertTrue(obj.getSize() < 150);
            OUT.insertLog(obj.getName()  + " size: " + Long.toString(obj.getSize()));
            success = true;

        } catch (final Exception e) {
            LOG.info("Exception in {}", testDescription, e );
        } finally {
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
            assertTrue(success);
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
            LOG.info("Exception in {}", testDescription, e );
        } finally {
            OUT.finishTest(testDescription, success);
            CertificationUtil.deleteJob(client, jobId);
            Util.deleteBucket(client, bucketName);
            assertTrue(success);
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

            final Collection normalTapes = CollectionUtils.filter(response.getTapeListResult().getTapes(),
                    tape -> ((Tape) tape).getState() == TapeState.NORMAL
                         || ((Tape) tape).getState() == TapeState.FOREIGN);
            assertFalse(normalTapes.isEmpty());
            barcode = ((Tape) normalTapes.iterator().next()).getBarCode();

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
            LOG.info("Exception in {}", testDescription, e );
        } finally {
            CertificationUtil.cancelTapeEject(client, barcode);
            OUT.finishTest(testDescription, success);
            assertTrue(success);
        }
    }

    /**
     * 8.6: Show a job is fully persisted to tape or disk archive location
     */
    @Test
    public void test_8_6_fully_persisted() throws Exception {
        final String testDescription = "8.6: Show Fully Persisted";
        final Integer numFiles = 6;
        final Long fileSize = GB_BYTES;
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

            final String putBulkCmd = "--http -c put_bulk -b " + bucketName + " -d "  + bulkPutLocalTempDir.toString();
            final CommandResponse putBulkResponse = Util.command(client, putBulkCmd);
            assertThat(putBulkResponse.getReturnCode(), is(0));

            // verify write
            OUT.insertLog("List bucket " + bucketName + "contents");
            final Iterator<Contents> objects = HELPERS.listObjects(bucketName).iterator();
            assertTrue(objects.hasNext());
            final String objectName = objects.next().getKey();
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

            // Show persisted
            final GetJobsSpectraS3Response allJobs = client.getJobsSpectraS3(new GetJobsSpectraS3Request());
            final Optional<Job> bulkJob = allJobs.getJobListResult().getJobs()
                    .stream()
                    .filter(job -> job.getBucketName().matches(bucketName))
                    .findFirst();
            assertTrue(bulkJob.isPresent());
            final UUID jobId = bulkJob.get().getJobId();

            // Verify Job finishes
            if (!waitForJobComplete(client, jobId)) {
                final String jobFinishErrorMsg = "Timed out waiting for PUT job " + jobId.toString() + " State to change to Complete.";
                OUT.insertLog(jobFinishErrorMsg);
                fail(jobFinishErrorMsg);
            }

            OUT.insertLog("Show fully persisted");
            final CommandResponse getPhysicalPlacementAfterResponse = Util.command(client, getPhysicalPlacementCmd);
            OUT.insertCommand(getPhysicalPlacementCmd, getPhysicalPlacementAfterResponse.getMessage());
        } catch (final Exception e) {
            LOG.info("Exception in {}", testDescription, e );
        } finally {
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
            assertTrue(success);
        }
    }

    /**
     * 8.7: If application can list objects in a bucket, show that it can handle (paginate) a large list
     */
    @Test
    public void test_8_7_large_list() throws Exception {
        final String testDescription = "8.7: Large List";
        final Integer numFiles = 500;
        final Long fileSize = 1L;
        final String bucketName = CertificationUtil.getBucketName(testDescription);

        OUT.startNewTest(testDescription);
        boolean success = false;
        try {
            // Put 500 files into bucket
            final CommandResponse performanceResponse = Util.putPerformanceFiles(client, bucketName,  numFiles, fileSize);
            assertThat(performanceResponse.getReturnCode(), is(0));

            final String listBucketArgs = "--http -c get_bucket -b " + bucketName;
            final CommandResponse getBucketResponseAfterBulkPut =  Util.command(client, listBucketArgs);
            OUT.insertCommand(listBucketArgs, getBucketResponseAfterBulkPut.getMessage());
            assertThat(getBucketResponseAfterBulkPut.getReturnCode(), is(0));
            success = true;
        } catch (final Exception e) {
            LOG.info("Exception in {}", testDescription, e );
        } finally {
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
        }
    }

}
