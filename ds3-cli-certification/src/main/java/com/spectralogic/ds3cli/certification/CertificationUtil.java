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

import com.google.common.collect.Lists;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.JobStatus;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.Quiesced;
import com.spectralogic.ds3client.models.SpectraUser;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.lang.Thread.sleep;


public class CertificationUtil {

    private static final ch.qos.logback.classic.Logger LOG =  (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CertificationUtil.class);

    public static Path createTempFiles(
           final String prefix,
           final int numFiles,
           final long length) throws IOException {
        LOG.info("Creating {} files of size {}...", numFiles, length);
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

    public static String getUserSecretKey(final Ds3Client client, final String username) {
        try {
            return client.getUserSpectraS3(new GetUserSpectraS3Request(username)).getSpectraUserResult().getSecretKey();
        } catch (final Exception e) {
            LOG.info("Could not get Secret Key fro User {}", username);
            return "";
        }
    }

    public static SpectraUser createUser(final Ds3Client client, final String username) throws IOException {
        final DelegateCreateUserSpectraS3Response response
                =  client.delegateCreateUserSpectraS3(new DelegateCreateUserSpectraS3Request(username));
        return response.getSpectraUserResult();
    }

    public static void deleteUser(final Ds3Client client, final String username) throws IOException {
        client.delegateDeleteUserSpectraS3(new DelegateDeleteUserSpectraS3Request(username));
    }

    public static String createPutJob(final Ds3Client client, final String bucketName, final Priority priority) throws Exception {
        final Long fileSize = 1024L;

        final Ds3Object ds3Obj = new Ds3Object(bucketName + "_0", fileSize);
        final PutBulkJobSpectraS3Response putBulkResponse = client.putBulkJobSpectraS3(
                new PutBulkJobSpectraS3Request(
                        bucketName,
                        Lists.newArrayList(ds3Obj)).withPriority(priority));
        return putBulkResponse.getMasterObjectList().getJobId().toString();
    }

    public static void deleteJob(final Ds3Client client, final String jobId) {
        try {
            final CancelJobSpectraS3Request request = new CancelJobSpectraS3Request(jobId);
            client.cancelJobSpectraS3(request);
        } catch (final IOException ex) {
            LOG.info("Could not delete job Id {}", jobId);
        }
    }

    public static void cancelTapeEject(final Ds3Client client, final String barcode) {
        try {
            client.cancelEjectTapeSpectraS3(new CancelEjectTapeSpectraS3Request(barcode));
        } catch (final IOException e) {
            LOG.info("Failed cancel eject tape, barcode {}", barcode);
        }
    }

    public static String getBucketName(final String testName) {
        return "test_" + testName.replaceAll("[ !,.:;<>&]+", "_");
    }

    public static UUID getValidTapePartition(final Ds3Client client) throws IOException {
        final GetTapePartitionsSpectraS3Response getTapePartitionsResponse = client.getTapePartitionsSpectraS3(new GetTapePartitionsSpectraS3Request());
        return getTapePartitionsResponse.getTapePartitionListResult().getTapePartitions().get(0).getId();
    }

    public static boolean waitForTapePartitionQuiescedState(
            final Ds3Client client,
            final UUID tapePartitionId,
            final Quiesced quiescedState) throws InterruptedException, IOException {
        int retries = 0;
        final int max_retries = 12;
        while (quiescedState != client.getTapePartitionSpectraS3(new GetTapePartitionSpectraS3Request(tapePartitionId.toString())).getTapePartitionResult().getQuiesced()) {
            LOG.info("Sleeping 5 minutes while waiting for tape partition {} to change to Quiesced state {}...", tapePartitionId, quiescedState);
            sleep(300 * 1000); // sleep 5 minutes
            if (++retries > max_retries) {
                return false;
            }
        }
        return true;
    }

    public static boolean ensureTapePartitionQuiescedState(
            final Ds3Client client,
            final UUID tapePartitionId,
            final Quiesced desiredState) throws IOException, InterruptedException {
        if (desiredState == client.getTapePartitionSpectraS3(new GetTapePartitionSpectraS3Request(tapePartitionId.toString())).getTapePartitionResult().getQuiesced()) {
            return true;
        }

        try {
            if (desiredState == Quiesced.NO) {
                client.modifyAllTapePartitionsSpectraS3(new ModifyAllTapePartitionsSpectraS3Request(Quiesced.NO));
            } else {
                // Must transition state from NO -> PENDING -> YES
                client.modifyAllTapePartitionsSpectraS3(new ModifyAllTapePartitionsSpectraS3Request(Quiesced.PENDING));
                client.modifyAllTapePartitionsSpectraS3(new ModifyAllTapePartitionsSpectraS3Request(Quiesced.YES));
            }
        } catch (final FailedRequestException fre) {
            // Ignore Request failure if tape partitions are already quiesced
            LOG.info("Failed to modify TapePartition {} to Quiesce state: {}", tapePartitionId, fre.getMessage());
        }
        return waitForTapePartitionQuiescedState(client, tapePartitionId, desiredState);
    }

    public static boolean waitForJobComplete(
            final Ds3Client client,
            final UUID jobId) throws InterruptedException, IOException {
        int retries = 0;
        final int max_retries = 12;
        while (JobStatus.IN_PROGRESS == client.getJobSpectraS3(new GetJobSpectraS3Request(jobId)).getMasterObjectListResult().getStatus()) {
            LOG.info("Sleeping 5 minutes while waiting for job to finish...");
            sleep(300 * 1000); // sleep 5 minutes
            if (++retries > max_retries) {
                return false;
            }
        }
        return true;
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
