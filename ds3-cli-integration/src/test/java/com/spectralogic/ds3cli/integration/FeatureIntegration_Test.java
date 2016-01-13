/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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

import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.integration.helpers.JobResponse;
import com.spectralogic.ds3cli.integration.helpers.JsonMapper;
import com.spectralogic.ds3cli.util.SterilizeString;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.commands.GetObjectRequest;
import com.spectralogic.ds3client.commands.GetObjectResponse;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.utils.ResourceUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FeatureIntegration_Test {

    private static Ds3Client client;

    @BeforeClass
    public static void startup() {
        final Ds3ClientBuilder builder = Ds3ClientBuilder.fromEnv();
        builder.withHttps(false);
        client = builder.build();
    }

    @AfterClass
    public static void teardown() throws IOException {
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
        final String bucketName = "test_get_job";
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
            final Ds3ClientHelpers.Job readJob = Ds3ClientHelpers.wrap(client)
                    .startReadJob(bucketName, Lists.newArrayList(obj));

            final GetObjectResponse readResponse = client.getObject(new GetObjectRequest(bucketName, book, 0, readJob.getJobId(), channel));
            assertThat(readResponse, is(notNullValue()));
            assertThat(readResponse.getStatusCode(), is(equalTo(200)));

            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_job", "-i", readJob.getJobId().toString()});
            final CommandResponse getJobResponse = Util.command(client, args);

            final String expectedBeginning = "JobId: " + readJob.getJobId() + " | Status: COMPLETED | Bucket: " + bucketName
                    + " | Type: GET | Priority: HIGH | User Name: spectra | Creation Date: ";
            final String expectedEnding = " | Total Size: " + objSize + " | Total Transferred: " + objSize;

            assertTrue(getJobResponse.getMessage().startsWith(expectedBeginning));
            assertTrue(getJobResponse.getMessage().endsWith(expectedEnding));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile(book);
        }
    }

    @Test
    public void getCompletedJobJson() throws Exception {
        final String bucketName = "test_get_job_json";
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
            final Ds3ClientHelpers.Job readJob = Ds3ClientHelpers.wrap(client)
                    .startReadJob(bucketName, Lists.newArrayList(obj));

            final GetObjectResponse readResponse = client.getObject(new GetObjectRequest(bucketName, book, 0, readJob.getJobId(), channel));
            assertThat(readResponse, is(notNullValue()));
            assertThat(readResponse.getStatusCode(), is(equalTo(200)));

            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_job", "-i", readJob.getJobId().toString(), "--output-format", "json"});
            final CommandResponse getJobResponse = Util.command(client, args);

            final JobResponse cliJobResponse = JsonMapper.toModel(getJobResponse.getMessage(), JobResponse.class);

           assertThat(cliJobResponse.getMeta(), is(notNullValue()));

            final String expectedMiddle = "\"Data\" : {\n"
                    + "    \"jobDetails\" : {\n"
                    + "      \"Nodes\" : null,\n"
                    + "      \"CachedSizeInBytes\" : " + objSize + ",\n"
                    + "      \"CompletedSizeInBytes\" : " + objSize + ",\n"
                    + "      \"OriginalSizeInBytes\" : " + objSize + ",\n"
                    + "      \"BucketName\" : \"" + bucketName + "\",\n"
                    + "      \"JobId\" : \"" + readJob.getJobId() + "\",\n";

            final String expectedEnding =
                    "      \"ChunkClientProcessingOrderGuarantee\" : \"NONE\",\n"
                            + "      \"Status\" : \"COMPLETED\",\n"
                            + "      \"Objects\" : null\n"
                            + "    }\n"
                            + "  },\n  \"Status\" : \"OK\"\n"
                            + "}";

            System.out.println(getJobResponse.getMessage());


            assertTrue(getJobResponse.getMessage().contains(expectedMiddle));
            assertTrue(getJobResponse.getMessage().endsWith(expectedEnding));
        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile(book);
        }
    }

    @Test
    public void getObjectWithSync() throws Exception {
        final String bucketName = "test_get_object";
        try {

            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            Util.copyFile("beowulf.txt", Util.RESOURCE_BASE_NAME, Util.DOWNLOAD_BASE_NAME);
            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_object", "-b", bucketName,
                    "-o", "beowulf.txt", "-d", Util.DOWNLOAD_BASE_NAME, "--sync"});
            CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: Finished syncing object."));

            response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: No need to sync beowulf.txt"));

        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFile("beowulf.txt");
        }
    }

    @Test
    public void getBulkObjectWithSync() throws Exception {
        final String bucketName = "test_get_object";
        try {

            Util.createBucket(client, bucketName);
            Util.loadBookTestData(client, bucketName);

            final Arguments args = new Arguments(new String[]{"--http", "-c", "get_bulk", "-b", bucketName,
                    "-d", Util.DOWNLOAD_BASE_NAME, "--sync"});
            CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: Synced all the objects from test_get_object to .\\.\\output"));

            response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: All files are up to date"));

        } finally {
            Util.deleteBucket(client, bucketName);
            Util.deleteLocalFiles();
        }
    }

    @Test
    public void putObjectWithSync() throws Exception {
        final String bucketName = "test_put_object";
        try {

            Util.createBucket(client, bucketName);
            final Arguments args = new Arguments(new String[]{"--http", "-c", "put_object", "-b", bucketName,
                    "-o", Utils.getFileName(Paths.get("."), Paths.get(Util.RESOURCE_BASE_NAME + "beowulf.txt")), "--sync"});
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
        final String bucketName = "test_put_object";
        try {

            Util.createBucket(client, bucketName);
            final Arguments args = new Arguments(new String[]{"--http", "-c", "put_bulk", "-b", bucketName, "-d", Util.RESOURCE_BASE_NAME, "--sync"});
            CommandResponse response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: Wrote all the files in .\\src\\test\\resources\\books to bucket test_put_object"));

            response = Util.command(client, args);
            assertThat(response.getMessage(), is("SUCCESS: All files are up to date"));

        } finally {
            Util.deleteBucket(client, bucketName);
        }
    }
}
