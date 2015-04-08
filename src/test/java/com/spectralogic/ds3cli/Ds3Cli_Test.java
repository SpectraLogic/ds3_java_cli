package com.spectralogic.ds3cli;

import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.Error;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.networking.Headers;
import com.spectralogic.ds3client.networking.WebResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Ds3Cli_Test {

    @Test
    public void getService() throws Exception {
        final String expectedString = "Owner: webfile\n" +
                "+-------------+--------------------------+\n" +
                "| Bucket Name |       Creation Date      |\n" +
                "+-------------+--------------------------+\n" +
                "| quotes      | 2006-02-03T16:45:09.000Z |\n" +
                "| samples     | 2006-02-03T16:41:58.000Z |\n" +
                "+-------------+--------------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_service"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final InputStream stream = IOUtils.toInputStream("<ListAllMyBucketsResult>\n" +
                "  <Owner>\n" +
                "    <ID>bcaf1ffd86f461ca5fb16fd081034f</ID>\n" +
                "    <DisplayName>webfile</DisplayName>\n" +
                "  </Owner>\n" +
                "  <Buckets>\n" +
                "    <Bucket>\n" +
                "      <Name>quotes</Name>\n" +
                "      <CreationDate>2006-02-03T16:45:09.000Z</CreationDate>\n" +
                "    </Bucket>\n" +
                "    <Bucket>\n" +
                "      <Name>samples</Name>\n" +
                "      <CreationDate>2006-02-03T16:41:58.000Z</CreationDate>\n" +
                "    </Bucket>\n" +
                "  </Buckets>\n" +
                "</ListAllMyBucketsResult>", "utf-8");

        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(new Headers() {
            @Override
            public String get(final String key) {
                return null;
            }
        });
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetServiceResponse serviceResponse = new GetServiceResponse(webResponse);
        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        when(client.getService(any(GetServiceRequest.class))).thenReturn(serviceResponse);

        final String result = cli.call();
        assertThat(result, is(expectedString));
    }

    @Test
    public void getServiceJson() throws Exception {
        final String expectedString = "  \"Data\" : {\n" +
                "    \"Owner\" : {\n" +
                "      \"ID\" : \"bcaf1ffd86f461ca5fb16fd081034f\",\n" +
                "      \"DisplayName\" : \"webfile\"\n" +
                "    },\n" +
                "    \"Buckets\" : [ {\n" +
                "      \"Name\" : \"quotes\",\n" +
                "      \"CreationDate\" : \"2006-02-03T16:45:09.000Z\"\n" +
                "    }, {\n" +
                "      \"Name\" : \"samples\",\n" +
                "      \"CreationDate\" : \"2006-02-03T16:41:58.000Z\"\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"Status\" : \"OK\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_service", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final InputStream stream = IOUtils.toInputStream("<ListAllMyBucketsResult>\n" +
                "  <Owner>\n" +
                "    <ID>bcaf1ffd86f461ca5fb16fd081034f</ID>\n" +
                "    <DisplayName>webfile</DisplayName>\n" +
                "  </Owner>\n" +
                "  <Buckets>\n" +
                "    <Bucket>\n" +
                "      <Name>quotes</Name>\n" +
                "      <CreationDate>2006-02-03T16:45:09.000Z</CreationDate>\n" +
                "    </Bucket>\n" +
                "    <Bucket>\n" +
                "      <Name>samples</Name>\n" +
                "      <CreationDate>2006-02-03T16:41:58.000Z</CreationDate>\n" +
                "    </Bucket>\n" +
                "  </Buckets>\n" +
                "</ListAllMyBucketsResult>", "utf-8");

        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(new Headers() {
            @Override
            public String get(final String key) {
                return null;
            }
        });
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetServiceResponse serviceResponse = new GetServiceResponse(webResponse);
        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        when(client.getService(any(GetServiceRequest.class))).thenReturn(serviceResponse);

        final String result = cli.call();
        assertTrue(result.endsWith(expectedString));
    }

    @Test
    public void error() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_service"});
        final Ds3Client client = mock(Ds3Client.class);
        when(client.getService(any(GetServiceRequest.class))).thenThrow(new FailedRequestException(new int[]{200}, 500, new Error(), ""));

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final String result = cli.call();
        assertThat(result, is("Failed Get Service"));
    }

    @Test
    public void errorJson() throws Exception {
        final String expected =
                "  \"Data\" : {\n" +
                "    \"ApiErrorMessage\" : \"\",\n" +
                "    \"StatusCode\" : \"500\"\n" +
                "  },\n" +
                "  \"Status\" : \"ERROR\",\n" +
                "  \"Message\" : \"Failed Get Service\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_service", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        when(client.getService(any(GetServiceRequest.class))).thenThrow(new FailedRequestException(new int[]{200}, 500, new Error(),""));

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final String result = cli.call();
        assertTrue(result.endsWith(expected));
    }

    @Test
    public void deleteBucket() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_bucket", "-b", "bucketName"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteBucketResponse deleteBucketResponse = new DeleteBucketResponse(webResponse);
        when(client.deleteBucket(any(DeleteBucketRequest.class))).thenReturn(deleteBucketResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final String result = cli.call();
        assertThat(result, is("Success: Deleted bucket 'bucketName'."));
    }

    @Test
    public void deleteBucketJson() throws Exception {
        final String expected = "  \"Status\" : \"OK\",\n" +
                "  \"Message\" : \"Success: Deleted bucket 'bucketName'.\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_bucket", "-b", "bucketName", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteBucketResponse deleteBucketResponse = new DeleteBucketResponse(webResponse);
        when(client.deleteBucket(any(DeleteBucketRequest.class))).thenReturn(deleteBucketResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final String result = cli.call();
        assertTrue(result.endsWith(expected));
    }

    @Test
    public void testDeleteObject() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_object", "-b", "bucketName", "-o", "obj.txt"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteObjectResponse deleteObjectResponse = new DeleteObjectResponse(webResponse);
        when(client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(deleteObjectResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final String result = cli.call();
        assertThat(result, is("Success: Deleted object 'obj.txt' from bucket 'bucketName'."));
    }

    @Test
    public void testDeleteObjectJson() throws Exception {
        final String expected = "  \"Status\" : \"OK\",\n" +
                "  \"Message\" : \"Success: Deleted object 'obj.txt' from bucket 'bucketName'.\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_object", "-b", "bucketName", "-o", "obj.txt", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteObjectResponse deleteObjectResponse = new DeleteObjectResponse(webResponse);
        when(client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(deleteObjectResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final String result = cli.call();
        assertTrue(result.endsWith(expected));
    }

    @Test
    public void getBucket() throws Exception {

        final String expected = "+--------------------+--------+----------------+--------------------------+------------------------------------+\n" +
                "|      File Name     |  Size  |      Owner     |       Last Modified      |                ETag                |\n" +
                "+--------------------+--------+----------------+--------------------------+------------------------------------+\n" +
                "| my-image.jpg       | 434234 | mtd@amazon.com | 2009-10-12T17:50:30.000Z | \"fba9dede5f27731c9771645a39863328\" |\n" +
                "| my-third-image.jpg |  64994 | mtd@amazon.com | 2009-10-12T17:50:30.000Z | \"1b2cf535f27731c974343645a3985328\" |\n" +
                "+--------------------+--------+----------------+--------------------------+------------------------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bucket", "-b", "bucketName"});
        final String response = "<ListBucketResult>\n" +
                "    <Name>bucket</Name>\n" +
                "    <Prefix/>\n" +
                "    <Marker/>\n" +
                "    <MaxKeys>1000</MaxKeys>\n" +
                "    <IsTruncated>false</IsTruncated>\n" +
                "    <Contents>\n" +
                "        <Key>my-image.jpg</Key>\n" +
                "        <LastModified>2009-10-12T17:50:30.000Z</LastModified>\n" +
                "        <ETag>&quot;fba9dede5f27731c9771645a39863328&quot;</ETag>\n" +
                "        <Size>434234</Size>\n" +
                "        <StorageClass>STANDARD</StorageClass>\n" +
                "        <Owner>\n" +
                "            <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>\n" +
                "            <DisplayName>mtd@amazon.com</DisplayName>\n" +
                "        </Owner>\n" +
                "    </Contents>\n" +
                "    <Contents>\n" +
                "       <Key>my-third-image.jpg</Key>\n" +
                "         <LastModified>2009-10-12T17:50:30.000Z</LastModified>\n" +
                "        <ETag>&quot;1b2cf535f27731c974343645a3985328&quot;</ETag>\n" +
                "        <Size>64994</Size>\n" +
                "        <StorageClass>STANDARD</StorageClass>\n" +
                "        <Owner>\n" +
                "            <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>\n" +
                "            <DisplayName>mtd@amazon.com</DisplayName>\n" +
                "        </Owner>\n" +
                "    </Contents>\n" +
                "</ListBucketResult>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetBucketResponse getBucketResponse = new GetBucketResponse(webResponse);
        when(client.getBucket(any(GetBucketRequest.class))).thenReturn(getBucketResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final String result = cli.call();
        assertThat(result, is(expected));
    }

    @Test
    public void getBucketJson() throws Exception {

        final String expected =
                "  \"Data\" : {\n" +
                "    \"BucketName\" : \"bucketName\",\n" +
                "    \"Objects\" : [ {\n" +
                "      \"Key\" : \"my-image.jpg\",\n" +
                "      \"LastModified\" : \"2009-10-12T17:50:30.000Z\",\n" +
                "      \"ETag\" : \"\\\"fba9dede5f27731c9771645a39863328\\\"\",\n" +
                "      \"Size\" : 434234,\n" +
                "      \"StorageClass\" : \"STANDARD\",\n" +
                "      \"Owner\" : {\n" +
                "        \"ID\" : \"75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a\",\n" +
                "        \"DisplayName\" : \"mtd@amazon.com\"\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"Key\" : \"my-third-image.jpg\",\n" +
                "      \"LastModified\" : \"2009-10-12T17:50:30.000Z\",\n" +
                "      \"ETag\" : \"\\\"1b2cf535f27731c974343645a3985328\\\"\",\n" +
                "      \"Size\" : 64994,\n" +
                "      \"StorageClass\" : \"STANDARD\",\n" +
                "      \"Owner\" : {\n" +
                "        \"ID\" : \"75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a\",\n" +
                "        \"DisplayName\" : \"mtd@amazon.com\"\n" +
                "      }\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"Status\" : \"OK\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bucket", "-b", "bucketName", "--output-format", "json"});
        final String response = "<ListBucketResult>\n" +
                "    <Name>bucket</Name>\n" +
                "    <Prefix/>\n" +
                "    <Marker/>\n" +
                "    <MaxKeys>1000</MaxKeys>\n" +
                "    <IsTruncated>false</IsTruncated>\n" +
                "    <Contents>\n" +
                "        <Key>my-image.jpg</Key>\n" +
                "        <LastModified>2009-10-12T17:50:30.000Z</LastModified>\n" +
                "        <ETag>&quot;fba9dede5f27731c9771645a39863328&quot;</ETag>\n" +
                "        <Size>434234</Size>\n" +
                "        <StorageClass>STANDARD</StorageClass>\n" +
                "        <Owner>\n" +
                "            <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>\n" +
                "            <DisplayName>mtd@amazon.com</DisplayName>\n" +
                "        </Owner>\n" +
                "    </Contents>\n" +
                "    <Contents>\n" +
                "       <Key>my-third-image.jpg</Key>\n" +
                "         <LastModified>2009-10-12T17:50:30.000Z</LastModified>\n" +
                "        <ETag>&quot;1b2cf535f27731c974343645a3985328&quot;</ETag>\n" +
                "        <Size>64994</Size>\n" +
                "        <StorageClass>STANDARD</StorageClass>\n" +
                "        <Owner>\n" +
                "            <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>\n" +
                "            <DisplayName>mtd@amazon.com</DisplayName>\n" +
                "        </Owner>\n" +
                "    </Contents>\n" +
                "</ListBucketResult>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetBucketResponse getBucketResponse = new GetBucketResponse(webResponse);
        when(client.getBucket(any(GetBucketRequest.class))).thenReturn(getBucketResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final String result = cli.call();
        assertTrue(result.endsWith(expected));
    }

    @Test
    public void putBucketView() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bucket", "-b", "bucketName"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);

        final PutBucketResponse response = new PutBucketResponse(webResponse);
        when(client.putBucket(any(PutBucketRequest.class))).thenReturn(response);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final String result = cli.call();
        assertThat(result, is("Success: created bucket bucketName."));
    }

    @Test
    public void putBucketViewJson() throws Exception {
        final String expected = "  \"Status\" : \"OK\",\n" +
                "  \"Message\" : \"Success: created bucket bucketName.\"\n" +
                "}";
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bucket", "-b", "bucketName", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);

        final PutBucketResponse response = new PutBucketResponse(webResponse);
        when(client.putBucket(any(PutBucketRequest.class))).thenReturn(response);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final String result = cli.call();
        assertTrue(result.endsWith(expected));
    }

    @Test
    public void putObject() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_object", "-b", "bucketName", "-o", "obj.txt"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(mockedFileUtils.exists(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.isRegularFile(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.size(any(Path.class))).thenReturn(100L);
        when(helpers.startWriteJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull())).thenReturn(mockedPutJob);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final String result = cli.call();
        assertThat(result, is("Success: Finished writing file to ds3 appliance."));
    }

    @Test
    public void putObjectJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"Success: Finished writing file to ds3 appliance.\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_object", "-b", "bucketName", "-o", "obj.txt", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(mockedFileUtils.exists(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.isRegularFile(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.size(any(Path.class))).thenReturn(100L);
        when(helpers.startWriteJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull())).thenReturn(mockedPutJob);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final String result = cli.call();
        assertTrue(result.endsWith(expected));
    }

    @Test
    public void getObject() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull())).thenReturn(mockedGetJob);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final String result = cli.call();
        assertThat(result, is("SUCCESS: Finished downloading object.  The object was written to: ./obj.txt"));
    }

    @Test
    public void getObjectJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Finished downloading object.  The object was written to: ./obj.txt\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull())).thenReturn(mockedGetJob);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final String result = cli.call();
        assertTrue(result.endsWith(expected));
    }

    @Test
    public void getBulk() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bulk", "-b", "bucketName"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        when(helpers.startReadAllJob(eq("bucketName"), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final String result = cli.call();
        assertThat(result, is("SUCCESS: Wrote all the objects from bucketName to directory ."));
    }

    @Test
    public void getBulkJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Wrote all the objects from bucketName to directory .\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bulk", "-b", "bucketName", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        when(helpers.startReadAllJob(eq("bucketName"), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final String result = cli.call();
        System.out.println(result);
        assertTrue(result.endsWith(expected));
    }

    @Test
    public void putBulk() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));

        when(helpers.listObjectsForDirectory(any(Path.class))).thenReturn(retObj);
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedGetJob);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final String result = cli.call();
        assertThat(result, is("SUCCESS: Wrote all the files in dir to bucket bucketName"));
    }

    @Test
    public void putBulkJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Wrote all the files in dir to bucket bucketName\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));

        when(helpers.listObjectsForDirectory(any(Path.class))).thenReturn(retObj);
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedGetJob);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final String result = cli.call();
        assertTrue(result.endsWith(expected));
    }
}
