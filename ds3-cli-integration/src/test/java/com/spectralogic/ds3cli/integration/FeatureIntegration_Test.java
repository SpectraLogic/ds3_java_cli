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

package com.spectralogic.ds3cli.integration;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.command.GetBulk;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.helpers.JsonMapper;
import com.spectralogic.ds3cli.helpers.TempStorageIds;
import com.spectralogic.ds3cli.helpers.TempStorageUtil;
import com.spectralogic.ds3cli.helpers.Util;
import com.spectralogic.ds3cli.helpers.models.HeadObject;
import com.spectralogic.ds3cli.helpers.models.JobResponse;
import com.spectralogic.ds3cli.models.BulkJobType;
import com.spectralogic.ds3cli.models.RecoveryJob;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.util.MemoryObjectChannelBuilder;
import com.spectralogic.ds3cli.util.RecoveryFileManager;
import com.spectralogic.ds3cli.util.SterilizeString;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.commands.GetObjectRequest;
import com.spectralogic.ds3client.commands.GetObjectResponse;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FolderNameFilter;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.utils.ResourceUtils;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

public class FeatureIntegration_Test {
    private static final Logger LOG = LoggerFactory.getLogger(FeatureIntegration_Test.class);

    private static final Ds3Client client = Ds3ClientBuilder.fromEnv().withHttps(false).build();
    private static final Ds3ClientHelpers HELPERS = Ds3ClientHelpers.wrap(client);
    private static final String TEST_ENV_NAME = "FeatureIntegration_Test";
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
    public void putBucket() throws Exception {
        final String bucketName = "test_put_bucket";
        try {
            final String expected = "Success: created bucket " + bucketName + ".";

            final CommandResponse response = Util.createBucket(client, bucketName);
            assertThat(response.getMessage(), is(expected));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void putBucketJson() throws Exception {
        final String bucketName = "test_put_bucket_json";
        try {
            final String expected = "\"Message\" : \"Success: created bucket " + bucketName + ".\"\n}";

            final Arguments args = new Arguments(new String[]{"--http", "-c", "put_bucket", "-b", bucketName, "--output-format", "json"});
            final CommandResponse response = Util.command(client, args);
            assertTrue(response.getMessage().endsWith(expected));
        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void deleteBucket() throws Exception {
        final String bucketName = "test_delete_bucket";

        final String expected = "Success: Deleted " + bucketName + " and all the objects contained in it.";

        Util.createBucket(client, bucketName);
        final CommandResponse response = Util.deleteBucket(client, bucketName);
        assertThat(response.getMessage(), is(expected));
    }

    @Test
    public void deleteBucketJson() throws Exception {
        final String bucketName = "test_delete_bucket_json";

        final String expected = "\"Message\" : \"Success: Deleted " + bucketName + " and all the objects contained in it.\"\n}";

        Util.createBucket(client, bucketName);
        final Arguments args = new Arguments(new String[]{"--http", "-c", "delete_bucket", "-b", bucketName, "--force", "--output-format", "json"});
        final CommandResponse response = Util.command(client, args);
        assertTrue(response.getMessage().endsWith(expected));
    }

    @Test
    public void getObject() throws Exception {
        final String bucketName = "test_get_object";
        try {
            final String expected = "SUCCESS: Finished downloading object.  The object was written to: "
                    + SterilizeString.osSpecificPath(Util.DOWNLOAD_BASE_NAME) + "beowulf.txt";

            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_object", "-b", bucketName,
                    "-o", "beowulf.txt", "-d", Util.DOWNLOAD_BASE_NAME});
            final CommandResponse response = Util.command(client, args);

            assertThat(response.getMessage(), is(expected));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile("beowulf.txt");
        }
    }

    @Test
    public void getPartialObject() throws Exception {
        final String bucketName = "test_get_partial_object";
        try {
            final String expectedRestore = "Gutenberg EBook";
            final String expectedResponse = "SUCCESS: Finished downloading object.  The object was written to: "
                    + SterilizeString.osSpecificPath(Util.DOWNLOAD_BASE_NAME) + "beowulf.txt";

            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_object", "-b", bucketName,
                    "--range-offset", "15", "--range-length", "15",
                    "-o", "beowulf.txt", "-d", Util.DOWNLOAD_BASE_NAME});
            final CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is(expectedResponse));

            // get the right section?
            assertEquals(expectedRestore, Util.readLocalFile("beowulf.txt").iterator().next());
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile("beowulf.txt");
        }
    }

    @Test
    public void getObjectJson() throws Exception {
        final String bucketName = "test_get_object_json";
        try {
            final String expected = "\"Message\" : \"SUCCESS: Finished downloading object.  The object was written to: "
                    + SterilizeString.osSpecificPath(Util.DOWNLOAD_BASE_NAME, true) + "beowulf.txt\"\n}";

            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_object", "-b", bucketName,
                    "-o", "beowulf.txt", "-d", Util.DOWNLOAD_BASE_NAME, "--output-format", "json"});
            final CommandResponse response = Util.command(client, args);

            assertTrue(response.getMessage().endsWith(expected));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile("beowulf.txt");
        }
    }

    @Test
    public void getCompletedJob() throws Exception {
        final String bucketName = "test_get_completed_job";
        final String book = "sherlock_holmes.txt";
        try {
            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Path objFile = ResourceUtils.loadFileResource("books/" + book);
            final long objSize = Files.size(objFile);
            final Ds3Object obj = new Ds3Object(book, objSize);

            final Path dirPath = FileSystems.getDefault().getPath("output");
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }
            final FileChannel channel = FileChannel.open(
                    dirPath.resolve(book),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            final Ds3ClientHelpers.Job readJob = HELPERS.startReadJob(bucketName, Lists.newArrayList(obj));

            final GetObjectResponse readResponse = client.getObject(new GetObjectRequest(bucketName, book, channel, readJob.getJobId(), 0));
            assertThat(readResponse, is(notNullValue()));

            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_job", "-i", readJob.getJobId().toString()});
            final CommandResponse getJobResponse = Util.command(client, args);

            final String expectedBeginning = "JobId: " + readJob.getJobId() + " | Name: ";
            final String expectedMiddle = "Status: COMPLETED | Bucket: " + bucketName + " | Type: GET | Priority:";
            final String expectedEnding = " | Total Size: " + objSize + " | Total Transferred: " ;

            assertTrue(getJobResponse.getMessage().startsWith(expectedBeginning));
            assertTrue(getJobResponse.getMessage().contains(expectedMiddle));
            assertTrue(getJobResponse.getMessage().contains(expectedEnding));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile(book);
        }
    }

    @Test
    public void getCompletedJobJson() throws Exception {
        final String bucketName = "test_get_completed_job_json";
        final String book = "tale_of_two_cities.txt";
        try {
            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Path objFile = ResourceUtils.loadFileResource("books/" + book);
            final long objSize = Files.size(objFile);
            final Ds3Object obj = new Ds3Object(book, objSize);

            final Path dirPath = FileSystems.getDefault().getPath("output");
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }
            final FileChannel channel = FileChannel.open(
                    dirPath.resolve(book),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            final Ds3ClientHelpers.Job readJob = HELPERS.startReadJob(bucketName, Lists.newArrayList(obj));

            final GetObjectResponse readResponse = client.getObject(new GetObjectRequest(bucketName, book, channel, readJob.getJobId(), 0));
            assertThat(readResponse, is(notNullValue()));

            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_job", "-i", readJob.getJobId().toString(), "--output-format", "json"});
            final CommandResponse getJobResponse = Util.command(client, args);

            final JobResponse cliJobResponse = JsonMapper.toModel(getJobResponse.getMessage(), JobResponse.class);

            assertThat(cliJobResponse.getMeta(), is(notNullValue()));

            assertThat(cliJobResponse.getData(), is(notNullValue()));
            assertThat(cliJobResponse.getData().getJobDetails().getNodes(), is(nullValue()));
            assertThat(cliJobResponse.getData().getJobDetails().getOriginalSizeInBytes(), is(objSize));
            assertThat(cliJobResponse.getData().getJobDetails().getBucketName(), is(bucketName));
            assertThat(cliJobResponse.getData().getJobDetails().getJobId(), is(readJob.getJobId()));
            assertThat(cliJobResponse.getData().getJobDetails().getChunkClientProcessingOrderGuarantee(), is("NONE"));
            assertThat(cliJobResponse.getData().getJobDetails().getStatus(), is("COMPLETED"));
            assertThat(cliJobResponse.getData().getJobDetails().getObjects(), is(Collections.<String>emptyList()));
            assertThat(cliJobResponse.getData().getJobDetails().getPriority(), is("HIGH"));
            assertThat(cliJobResponse.getData().getJobDetails().getRequestType(), is("GET"));
            assertThat(cliJobResponse.getData().getJobDetails().getCachedSizeInBytes(), is(objSize));
            assertThat(cliJobResponse.getData().getJobDetails().getCompletedSizeInBytes(), is(objSize));
            assertThat(cliJobResponse.getData().getJobDetails().getAggregating(), is(false));

            assertThat(cliJobResponse.getStatus(), is("OK"));

        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile(book);
        }
    }

    @Test
    public void getObjectWithSync() throws Exception {
        final String bucketName = "test_get_object_with_sync";
        final String objectName = "beowulf.txt";
        try {
            // For a Get with sync, the local file needs to be older than the server copy
            Util.copyFile(objectName, Util.RESOURCE_BASE_NAME, Util.DOWNLOAD_BASE_NAME);
            final File file = new File(Util.RESOURCE_BASE_NAME + File.separator + objectName);
            final DateTime modTime = new DateTime().minusHours(1);
            file.setLastModified(modTime.getMillis());

            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Arguments args = new Arguments(
                    new String[]{
                            "--http",
                            "-c", "get_object",
                            "-b", bucketName,
                            "-o", objectName,
                            "-d", Util.RESOURCE_BASE_NAME,
                            "--sync"});
            final CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: Finished syncing object."));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile(objectName);
        }
    }

    @Test
    public void getObjectWithSyncNoUpdate() throws Exception {
        final String bucketName = "test_get_object_with_sync_no_update";
        final String objectName = "beowulf.txt";
        try {
            // get_object sets the last_modified property to that of the original file, so we need to spoof for comparison
            Util.copyFile(objectName, Util.RESOURCE_BASE_NAME, Util.DOWNLOAD_BASE_NAME);

            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);
            final File newFile = new File(Util.DOWNLOAD_BASE_NAME + File.separator + objectName);
            final DateTime now = new DateTime();
            newFile.setLastModified(now.getMillis());

            final Arguments args = new Arguments(
                    new String[]{
                            "--http",
                            "-c", "get_object",
                            "-b", bucketName,
                            "-o", objectName,
                            "-d", Util.DOWNLOAD_BASE_NAME,
                            "--sync"});
            final CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: No need to sync " + objectName));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile(objectName);
        }
    }

    @Test
    public void getBulkObjectWithPrefixes() throws Exception {
        final String bucketName = "test_get_bulk_with_prefixes";
        try {
            Files.createDirectories(Paths.get(Util.DOWNLOAD_BASE_NAME));
            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Arguments args = new Arguments(
                    new String[]{
                            "--http",
                            "-c", "get_bulk",
                            "-b", bucketName,
                            "-d", Util.DOWNLOAD_BASE_NAME,
                            "-p", "beo", "uly"});
            final CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: Wrote all the objects that start with 'beo uly' from " + bucketName + " to ." + File.separator + "output"));
            assertEquals(Util.countLocalFiles(), 2);
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFiles();
        }
    }

    @Test
    public void getBulkObjectWithSync() throws Exception {
        final String bucketName = "test_get_bulk_object_with_sync_no_update";
        try {
            Files.createDirectories(Paths.get(Util.DOWNLOAD_BASE_NAME));
            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Arguments args = new Arguments(
                    new String[]{
                            "--http",
                            "-c", "get_bulk",
                            "-b", bucketName,
                            "-d", Util.DOWNLOAD_BASE_NAME,
                            "--sync"});
            final CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: Synced all the objects from " + bucketName + " to ." + File.separator + "output"));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFiles();
        }
    }

    @Test
    public void getBulkObjectWithSyncNoUpdate() throws Exception {
        final String bucketName = "test_get_bulk_object_with_sync_no_update";
        try {
            Files.createDirectories(Paths.get(Util.DOWNLOAD_BASE_NAME));
            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Arguments args = new Arguments(
                    new String[]{
                            "--http",
                            "-c", "get_bulk",
                            "-b", bucketName,
                            "-d", Paths.get(Util.DOWNLOAD_BASE_NAME).toString(),
                            "--sync"});
            CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: Synced all the objects from " + bucketName + " to ." + File.separator + "output"));

            // get_bulk sets the last_modified property to that of the original file, so we need to spoof for comparison
            final File destFolder = new File(Util.DOWNLOAD_BASE_NAME);
            final DateTime now = new DateTime();
            for (final File currentFile : destFolder.listFiles()) {
                currentFile.setLastModified(now.getMillis());
            }

            response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: All files are up to date"));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFiles();
        }
    }

    @Test
    public void putObjectWithSync() throws Exception {
        assumeThat(Util.getBlackPearlVersion(client), greaterThan(1.2));

        final String bucketName = "test_put_object_with_sync";
        try {
            Util.createBucket(client, bucketName);
            final Arguments args = new Arguments(new String[]{"--http", "-c", "put_object", "-b", bucketName,
                    "-o", FileUtils.getFileName(Paths.get("."), Paths.get(Util.RESOURCE_BASE_NAME + "beowulf.txt")), "--sync"});
            CommandResponse response = Util.command(client, args);

            assertThat(response.getMessage(), is("Success: Finished syncing file to ds3 appliance."));

            response = Util.command(client, args);
            assertThat(response.getMessage(), is("Success: No need to sync src/test/resources/books/beowulf.txt"));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void putBulkObjectWithSync() throws Exception {
        assumeThat(Util.getBlackPearlVersion(client), greaterThan(1.2));

        final String bucketName = "test_put_bulk_object_with_sync";
        try {

            Util.createBucket(client, bucketName);
            final Arguments args = new Arguments(new String[]{"--http", "-c", "put_bulk", "-b", bucketName, "-d", Util.RESOURCE_BASE_NAME, "--sync"});
            CommandResponse response = Util.command(client, args);

            assertThat(response.getMessage(), is(String.format("SUCCESS: Wrote all the files in %s to bucket %s", "." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "books", bucketName)));

            response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: All files are up to date"));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void putBulkObjectWithPipe() throws Exception {

        final String bucketName = "test_put_bulk_pipe_object";
        try {

            Util.createBucket(client, bucketName);
            final Arguments args = new Arguments(new String[]{"--http", "-c", "put_bulk", "-b", bucketName});
            final String file;
            if (FileUtils.IS_WINDOWS) {
                file = ".\\src\\test\\resources\\books\\beowulf.txt\n.\\src\\test\\resources\\books\\ulysses.txt";
            } else {
                file = "./src/test/resources/books/beowulf.txt\n./src/test/resources/books/ulysses.txt";
            }
            final InputStream testFile = new ByteArrayInputStream(file.getBytes("UTF-8"));
            System.setIn(testFile);

            final CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is(String.format("SUCCESS: Wrote all piped files to bucket %s", bucketName)));
        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void putBulkObjectWithPipeAndSync() throws Exception {
        assumeThat(Util.getBlackPearlVersion(client), greaterThan(1.2));

        final String bucketName = "test_put_bulk_pipe_object";
        try {

            Util.createBucket(client, bucketName);
            final Arguments args = new Arguments(new String[]{"--http", "-c", "put_bulk", "-b", bucketName, "--sync"});
            final String file;
            if (FileUtils.IS_WINDOWS) {
                file = ".\\src\\test\\resources\\books\\beowulf.txt\n.\\src\\test\\resources\\books\\ulysses.txt";
            } else {
                file = "./src/test/resources/books/beowulf.txt\n./src/test/resources/books/ulysses.txt";
            }
            final InputStream testFile = new ByteArrayInputStream(file.getBytes("UTF-8"));
            System.setIn(testFile);

            final CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is(String.format("SUCCESS: Wrote all piped files to bucket %s", bucketName)));

            testFile.reset();
            final CommandResponse response2 = Util.command(client, args);
            assertThat(response2.getMessage(), is("SUCCESS: All files are up to date"));
        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void metadataPut() throws Exception {
        final String bucketName = "test_put_with_metadata";

        try {
            Util.createBucket(client, bucketName);
            final Arguments args = new Arguments(new String[]{"--http", "-c", "put_object", "-b", bucketName, "-o", FileUtils.getFileName(Paths.get("."), Paths.get(Util.RESOURCE_BASE_NAME + "beowulf.txt")), "--metadata", "key:value"});

            final CommandResponse response = Util.command(client, args);

            assertThat(response.getReturnCode(), is(0));

            final Arguments headObjectArgs = new Arguments(new String[]{"--http", "-c", "head_object", "-b", bucketName, "-o", "src/test/resources/books/beowulf.txt", "--output-format", "json"});
            final CommandResponse headResponse = Util.command(client, headObjectArgs);
            assertThat(headResponse.getReturnCode(), is(0));

            final HeadObject headObject = JsonMapper.toModel(headResponse.getMessage(), HeadObject.class);

            assertThat(headObject, is(notNullValue()));

            final ImmutableMultimap<String, String> metadata = headObject.getData().getMetadata();

            assertThat(metadata, is(notNullValue()));
            assertThat(metadata.size(), is(2)); // x-amz-meta-ds3-last-modified is added automatically

            final ImmutableCollection<String> collection = metadata.get("key");

            assertThat(collection.size(), is(1));
            assertThat(collection.asList().get(0), is("value"));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void multiMetadataPut() throws Exception {
        final String bucketName = "test_put_with_multi_metadata";

        try {
            Util.createBucket(client, bucketName);
            final Arguments args = new Arguments(new String[]{"--http", "-c", "put_object", "-b", bucketName, "-o", FileUtils.getFileName(Paths.get("."), Paths.get(Util.RESOURCE_BASE_NAME + "beowulf.txt")), "--metadata", "key:value,key2:value2"});

            final CommandResponse response = Util.command(client, args);

            assertThat(response.getReturnCode(), is(0));

            final Arguments headObjectArgs = new Arguments(new String[]{"--http", "-c", "head_object", "-b", bucketName, "-o", "src/test/resources/books/beowulf.txt", "--output-format", "json"});
            final CommandResponse headResponse = Util.command(client, headObjectArgs);
            assertThat(headResponse.getReturnCode(), is(0));

            final HeadObject headObject = JsonMapper.toModel(headResponse.getMessage(), HeadObject.class);

            assertThat(headObject, is(notNullValue()));

            final ImmutableMultimap<String, String> metadata = headObject.getData().getMetadata();

            assertThat(metadata, is(notNullValue()));
            assertThat(metadata.size(), is(3)); // x-amz-meta-ds3-last-modified is added automatically

            ImmutableCollection<String> collection = metadata.get("key");
            assertThat(collection.size(), is(1));
            assertThat(collection.asList().get(0), is("value"));

            collection = metadata.get("key2");
            assertThat(collection.size(), is(1));
            assertThat(collection.asList().get(0), is("value2"));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void getPhysicalPlacement() throws Exception {
        final String bucketName = "test_physical_placement";
        try {
            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_physical_placement", "-b", bucketName, "-o", "beowulf.txt" });
            final CommandResponse response = Util.command(client, args);
            assertTrue(response.getMessage().contains("| Object Name |                  ID                  | In Cache | Length | Offset | Latest | Version |"));
            assertTrue(response.getMessage().contains("| beowulf.txt |"));
            assertTrue(response.getMessage().contains("| true     | 294059 | 0      | true   | 1       |"));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }

    @Test
    public void getBulkWithPipe() throws Exception {
        assumeThat(Util.getBlackPearlVersion(client), greaterThan(1.2));

        final String bucketName = "test_get_bulk_pipe_object";
        try {
            Files.createDirectories(Paths.get(Util.DOWNLOAD_BASE_NAME));
            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final String pipedInput = "beowulf.txt\nulysses.txt";
            final InputStream testFile = new ByteArrayInputStream(pipedInput.getBytes("UTF-8"));
            System.setIn(testFile);

            final Arguments args = new Arguments(
                    new String[]{
                            "--http",
                            "-c", "get_bulk",
                            "-b", bucketName,
                            "-d", Util.DOWNLOAD_BASE_NAME});

            final CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), startsWith(String.format("SUCCESS: Wrote object names listed in stdin from %s", bucketName)));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFiles();
        }
    }

    @Test(expected = CommandException.class)
    public void getBulkWithPipeMissingFile() throws Exception {
        assumeThat(Util.getBlackPearlVersion(client), greaterThan(1.2));

        final String bucketName = "test_get_bulk_pipe_missing_files";
        try {
            final Path bookDir = Paths.get(Util.RESOURCE_BASE_NAME);
            Files.createDirectories(bookDir);
            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final String pipedInput = "beowulf.txt\nulysses.txt\n50 Shades of Grey.txt";
            final InputStream testFile = new ByteArrayInputStream(pipedInput.getBytes("UTF-8"));
            System.setIn(testFile);

            final Arguments args = new Arguments(
                    new String[]{
                            "--http",
                            "-c", "get_bulk",
                            "-b", bucketName,
                            "-d", Util.DOWNLOAD_BASE_NAME});

            final CommandResponse response = Util.command(client, args);
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFiles();
        }
    }

    @Test (expected = com.spectralogic.ds3client.helpers.JobRecoveryNotActiveException.class)
    public void recoverGetBulk() throws Exception {
        assumeThat(Util.getBlackPearlVersion(client), greaterThan(1.2));

        final String bucketName = "test_recover_get_bulk";
        UUID jobId = null;
        try {
            final Path bookDir = Paths.get(Util.RESOURCE_BASE_NAME);
            Files.createDirectories(bookDir);
            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Contents book1 = new Contents();
            book1.setKey("beowulf.txt");
            book1.setSize(294056L);
            final Contents book2 = new Contents();
            book2.setKey("ulysses.txt");
            book2.setSize(1540095L);

            final Ds3ClientHelpers.Job job = HELPERS.startReadJob(bucketName, HELPERS.toDs3Iterable(ImmutableList.of(book1, book2), FolderNameFilter.filter()));
            jobId = job.getJobId();
            RecoveryJob recoveryJob = new RecoveryJob(BulkJobType.GET_BULK);
            recoveryJob.setBucketName(bucketName);
            recoveryJob.setId(jobId);
            recoveryJob.setDirectory(Util.RESOURCE_BASE_NAME);
            RecoveryFileManager.writeRecoveryJob(recoveryJob);

            job.transfer(new MemoryObjectChannelBuilder());

            final Arguments args = new Arguments(
                    new String[]{
                            "--http",
                            "-c", "recover", "--recover",
                            "-i", jobId.toString()});

            final CommandResponse response = Util.command(client, args);
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFiles();
            RecoveryFileManager.deleteFiles(jobId.toString(), null, null);
        }
    }

    @Test
    public void recoverPutBulk() throws Exception {
        assumeThat(Util.getBlackPearlVersion(client), greaterThan(1.2));

        final String bucketName = "test_recover_put_bulk";
        UUID jobId = null;
        try {
            Util.createBucket(client, bucketName);

            final Contents file1 = new Contents();
            file1.setKey("ThousandBytes.txt");
            file1.setSize(1000L);
            final Contents file2 = new Contents();
            file2.setKey("TwoThousandBytes.txt");
            file2.setSize(2000L);

            final Ds3ClientHelpers.Job job = HELPERS.startWriteJob(bucketName, HELPERS.toDs3Iterable(ImmutableList.of(file1, file2)));
            jobId = job.getJobId();
            RecoveryJob recoveryJob = new RecoveryJob(BulkJobType.PUT_BULK);
            recoveryJob.setBucketName(bucketName);
            recoveryJob.setId(jobId);
            RecoveryFileManager.writeRecoveryJob(recoveryJob);

            job.transfer(new MemoryObjectChannelBuilder());

            // list recovery files
            final Arguments args = new Arguments(
                    new String[]{
                            "--http",
                            "-c", "recover",
                            "-i", jobId.toString()});

            final String expectedBeginning = "PUT_BULK Bucket: " + bucketName;
            final CommandResponse response = Util.command(client, args);
            assertTrue(response.getMessage().startsWith(expectedBeginning));
            assertTrue(response.getMessage().contains(jobId.toString()));
        } finally {
            Util.deleteBucket(client, bucketName);
            RecoveryFileManager.deleteFiles(jobId.toString(), null, null);
        }
    }




}
