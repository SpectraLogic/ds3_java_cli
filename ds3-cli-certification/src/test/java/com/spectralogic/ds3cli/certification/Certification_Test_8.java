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
import com.spectralogic.ds3cli.helpers.TempStorageIds;
import com.spectralogic.ds3cli.helpers.TempStorageUtil;
import com.spectralogic.ds3cli.helpers.Util;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


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
    private static final String TEST_ENV_NAME = "JavaCLI_Certification_Test_Section8";
    private static final String NO_RIGHTS_USERNAME = "no_rights";
    private static TempStorageIds envStorageIds;
    private static UUID envDataPolicyId;
    private static CertificationWriter OUT;

//    private static final Long GB_BYTES = 1073741824L;
    private static final Long GB_BYTES = 1024L;

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
     * 8.5: Eject a bucket or tape
     *
    @Test
    public void test_8_5_eject_bucket() throws Exception {
        final String testDescription = "EjectBucket";
        final Integer numFiles = 6;
        final Long fileSize = 1L; // converted to GB in performance
        final String bucketName = "test_" + testDescription;

        try {
            // Put files into bucket
            final CommandResponse performanceResponse = CertificationUtil.putPerformanceFiles(client, bucketName,  numFiles, fileSize);

            // verify write
            final CommandResponse getBucketResponseAfterBulkPut = Util.getBucket(client, bucketName);
            LOG.info("CommandResponse for listing contents of bucket {}: \n{}", bucketName, getBucketResponseAfterBulkPut.getMessage());
            assertThat(getBucketResponseAfterBulkPut.getReturnCode(), is(0));

            // force to tape
            final String reclaimCacheargs = "--http -c reclaim_cache";
            LOG.info("Run command: {} \n", reclaimCacheargs);
            CertificationUtil.runCommand(client, reclaimCacheargs);

            // eject
            final String ejectDomainargs = "--http -c --eject_storage_domain "});
            LOG.info("Run command: {} \n", reclaimCacheargs);
            CertificationUtil.runCommand(client, ejectDomainargs);

            // get bucket details

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }
**/

    /**
     * 8.6: ost application showing a job is fully persisted to tape or disk archive location
     */
     @Test
     public void test_8_6_fully_persisted() throws Exception {
         final String testDescription = "8.6: Fully Peristed";
         final Integer numFiles = 6;
         final Long fileSize = 1L; // converted to GB in performance
         final String bucketName = CertificationUtil.getBucketName(testDescription);
         boolean success = false;

         OUT.startNewTest(testDescription);
         try {
             // Put files into bucket
             final CommandResponse performanceResponse = CertificationUtil.putPerformanceFiles(client, bucketName,  numFiles, fileSize);

             // verify write
             OUT.insertLog("List bucket contents");
             final Iterator<Contents> objects = HELPERS.listObjects(bucketName).iterator();
             assertTrue(objects.hasNext());
             final String objectName = objects.next().getKey();

             // force to tape
             OUT.insertLog("Reclaim cache (force to tape)");
             final String reclaimCacheArgs = "--http -c reclaim_cache";
             final CommandResponse reclaimResponse = Util.command(client, reclaimCacheArgs);
             assertThat(reclaimResponse.getReturnCode(), is(0));

             // Show persisted
             OUT.insertLog("Show as persisted");
             final String getPhysicalArgs = "--http -c get_physical_placement -b " + bucketName + " -o " + objectName;
             Util.command(client, getPhysicalArgs);

             success = true;

         } finally {
             OUT.finishTest(testDescription, success);
             Util.deleteBucket(client, bucketName);
         }
     }

    /**
     * 8.7: If application can list objects in a bucket, show that it can handle (paginate) a large list
     */
    @Test
    public void test_8_7_large_list() throws Exception {
        final String testDescription = "8.7: Large List";
        final Integer numFiles = 5;
        final Long fileSize = 1L; // converted to GB in performance
        final String bucketName = CertificationUtil.getBucketName(testDescription);

        OUT.startNewTest(testDescription);
        boolean success = false;
        try {
            // Put 500 files into bucket
            final CommandResponse performanceResponse = CertificationUtil.putPerformanceFiles(client, bucketName,  numFiles, fileSize);

            final String listBucketArgs = "--http -c get_bucket -b " + bucketName;
            final CommandResponse getBucketResponseAfterBulkPut =  Util.command(client, listBucketArgs);
            assertThat(getBucketResponseAfterBulkPut.getReturnCode(), is(0));
            success = true;
        } finally {
            OUT.finishTest(testDescription, success);
            Util.deleteBucket(client, bucketName);
        }
    }

}
