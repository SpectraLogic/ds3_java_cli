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

package com.spectralogic.ds3cli;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.Error;
import com.spectralogic.ds3client.models.SystemInformation;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.networking.Headers;
import com.spectralogic.ds3client.networking.WebResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SignatureException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.spectralogic.ds3client.utils.ResponseUtils.toImmutableIntList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@PrepareForTest({Utils.class, SyncUtils.class, BlackPearlUtils.class})
@RunWith(PowerMockRunner.class)
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
            public List<String> get(final String key) {
                return null;
            }

            @Override
            public Set<String> keys() {
                return null;
            }
        });
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetServiceResponse serviceResponse = new GetServiceResponse(webResponse);
        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        when(client.getService(any(GetServiceRequest.class))).thenReturn(serviceResponse);

        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expectedString));
        assertThat(result.getReturnCode(), is(0));
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
            public List<String> get(final String key) {
                return null;
            }

            @Override
            public Set<String> keys() {
                return null;
            }
        });
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetServiceResponse serviceResponse = new GetServiceResponse(webResponse);
        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        when(client.getService(any(GetServiceRequest.class))).thenReturn(serviceResponse);

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void error() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_service"});
        final Ds3Client client = mock(Ds3Client.class);
        when(client.getService(any(GetServiceRequest.class)))
                .thenThrow(new FailedRequestException(toImmutableIntList(new int[]{200}), 500, new Error(), ""));

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("Failed Get Service"));
        assertThat(result.getReturnCode(), is(1));
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
        when(client.getService(any(GetServiceRequest.class)))
                .thenThrow(new FailedRequestException(toImmutableIntList(new int[]{200}), 500, new Error(), ""));

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final CommandResponse result = cli.call();
        assertTrue(SterilizeString.toUnix(result.getMessage()).endsWith(expected));
        assertThat(result.getReturnCode(), is(1));
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
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("Success: Deleted bucket 'bucketName'."));
        assertThat(result.getReturnCode(), is(0));
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

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void deleteFolder() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "delete_folder", "-b", "bucketName", "-d", "folderName"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteFolderResponse deleteFolderResponse = new DeleteFolderResponse(webResponse);
        when(client.deleteFolder(any(DeleteFolderRequest.class))).thenReturn(deleteFolderResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("Success: Deleted folder 'folderName'."));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void deleteFolderJson() throws Exception {
        final String expected = "  \"Status\" : \"OK\",\n" +
                "  \"Message\" : \"Success: Deleted folder 'folderName'.\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "delete_folder", "-b", "bucketName", "-d", "folderName", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteFolderResponse deleteFolderResponse = new DeleteFolderResponse(webResponse);
        when(client.deleteFolder(any(DeleteFolderRequest.class))).thenReturn(deleteFolderResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
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

        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("Success: Deleted object 'obj.txt' from bucket 'bucketName'."));
        assertThat(result.getReturnCode(), is(0));
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

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
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
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
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
        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
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

        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("Success: created bucket bucketName."));
        assertThat(result.getReturnCode(), is(0));
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

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putJob() throws Exception {
        final String jobId = "42b61136-9221-474b-a509-d716d8c554cd";
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "put_job", "-i", jobId, "--priority", "LOW"});

        final String expected = "Success: Modified job with job id '" + jobId + "' with priority LOW.";
        final String response = "<MasterObjectList BucketName=\"test_modify_job\" CachedSizeInBytes=\"0\" ChunkClientProcessingOrderGuarantee=\"IN_ORDER\" CompletedSizeInBytes=\"0\" JobId=\"42b61136-9221-474b-a509-d716d8c554cd\" OriginalSizeInBytes=\"2\" Priority=\"HIGH\" RequestType=\"PUT\" StartDate=\"2015-09-23T20:25:26.000Z\" Status=\"IN_PROGRESS\" UserId=\"c2581493-058c-40d7-a3a1-9a50b20d6d3b\" UserName=\"spectra\" WriteOptimization=\"CAPACITY\"><Nodes><Node EndPoint=\"192.168.56.101\" HttpPort=\"8080\" Id=\"477097a1-5326-11e5-b859-0800271a68bf\"/></Nodes><Objects ChunkId=\"a1004507-24d7-43c8-bdba-19faae3dc349\" ChunkNumber=\"0\"><Object InCache=\"false\" Length=\"2\" Name=\"test\" Offset=\"0\"/></Objects></MasterObjectList>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final ModifyJobResponse modifyJobResponse = new ModifyJobResponse(webResponse);
        when(client.modifyJob(any(ModifyJobRequest.class))).thenReturn(modifyJobResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putJobJson() throws Exception {
        final String jobId = "42b61136-9221-474b-a509-d716d8c554cd";
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "put_job", "-i", jobId, "--priority", "LOW", "--output-format", "json"});

        final String expected = "\"Message\" : \"Success: Modified job with job id '" + jobId + "' with priority LOW.\"\n}";
        final String response = "<MasterObjectList BucketName=\"test_modify_job\" CachedSizeInBytes=\"0\" ChunkClientProcessingOrderGuarantee=\"IN_ORDER\" CompletedSizeInBytes=\"0\" JobId=\"42b61136-9221-474b-a509-d716d8c554cd\" OriginalSizeInBytes=\"2\" Priority=\"HIGH\" RequestType=\"PUT\" StartDate=\"2015-09-23T20:25:26.000Z\" Status=\"IN_PROGRESS\" UserId=\"c2581493-058c-40d7-a3a1-9a50b20d6d3b\" UserName=\"spectra\" WriteOptimization=\"CAPACITY\"><Nodes><Node EndPoint=\"192.168.56.101\" HttpPort=\"8080\" Id=\"477097a1-5326-11e5-b859-0800271a68bf\"/></Nodes><Objects ChunkId=\"a1004507-24d7-43c8-bdba-19faae3dc349\" ChunkNumber=\"0\"><Object InCache=\"false\" Length=\"2\" Name=\"test\" Offset=\"0\"/></Objects></MasterObjectList>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final ModifyJobResponse modifyJobResponse = new ModifyJobResponse(webResponse);
        when(client.modifyJob(any(ModifyJobRequest.class))).thenReturn(modifyJobResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
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

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("Success: Finished writing file to ds3 appliance."));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putObjectWithSync() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_object", "-b", "bucketName", "-o", "obj.txt", "--sync"});

        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Contents c = new Contents();
        c.setKey("obj.txt");
        final Iterable<Contents> retObj = Lists.newArrayList(c);
        when(helpers.listObjects(eq("bucketName"))).thenReturn(retObj);
        when(helpers.startWriteJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull())).thenReturn(mockedPutJob);

        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(mockedFileUtils.exists(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.isRegularFile(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.size(any(Path.class))).thenReturn(100L);

        PowerMockito.mockStatic(SyncUtils.class);
        when(SyncUtils.isSyncSupported(any(Ds3Client.class))).thenReturn(true);
        when(SyncUtils.needToSync(any(Ds3ClientHelpers.class), any(String.class), any(Path.class), any(String.class), any(Boolean.class))).thenReturn(true);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("Success: Finished syncing file to ds3 appliance."));
        assertThat(result.getReturnCode(), is(0));

        when(SyncUtils.needToSync(any(Ds3ClientHelpers.class), any(String.class), any(Path.class), any(String.class), any(Boolean.class))).thenReturn(false);
        result = cli.call();
        assertThat(result.getMessage(), is("Success: No need to sync obj.txt"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putObjectWithSyncNotSupportedVersion() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_object", "-b", "bucketName", "-o", "obj.txt", "--sync"});

        final SystemInformation.BuildInformation buildInformation = mock(SystemInformation.BuildInformation.class);
        when(buildInformation.getVersion()).thenReturn("1.2.0");

        final SystemInformation systemInformation = mock(SystemInformation.class);
        when(systemInformation.getBuildInformation()).thenReturn(buildInformation);

        final GetSystemInformationResponse systemInformationResponse = mock(GetSystemInformationResponse.class);
        when(systemInformationResponse.getSystemInformation()).thenReturn(systemInformation);

        final Ds3Client client = mock(Ds3Client.class);
        when(client.getSystemInformation(any(GetSystemInformationRequest.class))).thenReturn(systemInformationResponse);

        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);

        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(mockedFileUtils.exists(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.isRegularFile(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.size(any(Path.class))).thenReturn(100L);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("Failed: The sync command is not supported with your version of BlackPearl."));
        assertThat(result.getReturnCode(), is(0));
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

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getObject() throws Exception {
        final String expected = "SUCCESS: Finished downloading object.  The object was written to: ." + SterilizeString.getFileDelimiter() + "obj.txt";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull())).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getObjectWithSync() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt", "--sync"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull())).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(Utils.class);
        when(Utils.fileExists(any(Path.class))).thenReturn(false);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: Finished downloading object.  The object was written to: ." + SterilizeString.getFileDelimiter() + "obj.txt"));
        assertThat(result.getReturnCode(), is(0));

        when(Utils.fileExists(any(Path.class))).thenReturn(true);
        final Contents c1 = new Contents();
        c1.setKey("obj.txt");

        final Iterable<Contents> retCont = Lists.newArrayList(c1);
        when(helpers.listObjects(eq("bucketName"))).thenReturn(retCont);

        PowerMockito.mockStatic(SyncUtils.class);
        when(SyncUtils.needToSync(any(Ds3ClientHelpers.class), any(String.class), any(Path.class), any(String.class), any(Boolean.class))).thenReturn(true);

        result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: Finished syncing object."));
        assertThat(result.getReturnCode(), is(0));

        when(SyncUtils.needToSync(any(Ds3ClientHelpers.class), any(String.class), any(Path.class), any(String.class), any(Boolean.class))).thenReturn(false);
        result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: No need to sync obj.txt"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getObjectJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Finished downloading object.  The object was written to: ." + SterilizeString.getFileDelimiter(true) + "obj.txt\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull())).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getCompletedJob() throws Exception {
        final String jobId = "aa5df0cc-b03a-4cb9-b69d-56e7367e917f";
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "get_job", "-i", jobId});

        final String expected = "JobId: " + jobId + " | Status: COMPLETED | Bucket: bucket | Type: GET | Priority: HIGH | User Name: spectra | Creation Date: 2015-09-28T17:30:43.000Z | Total Size: 32 | Total Transferred: 0";
        final String response = "<MasterObjectList BucketName=\"bucket\" CachedSizeInBytes=\"0\" ChunkClientProcessingOrderGuarantee=\"NONE\" CompletedSizeInBytes=\"0\" JobId=\"aa5df0cc-b03a-4cb9-b69d-56e7367e917f\" OriginalSizeInBytes=\"32\" Priority=\"HIGH\" RequestType=\"GET\" StartDate=\"2015-09-28T17:30:43.000Z\" Status=\"COMPLETED\" UserId=\"c2581493-058c-40d7-a3a1-9a50b20d6d3b\" UserName=\"spectra\" WriteOptimization=\"CAPACITY\"></MasterObjectList>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetJobResponse getJobResponse = new GetJobResponse(webResponse);
        when(client.getJob(any(GetJobRequest.class))).thenReturn(getJobResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getCompletedJobJson() throws Exception {
        final String jobId = "aa5df0cc-b03a-4cb9-b69d-56e7367e917f";
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "get_job", "-i", jobId, "--output-format", "json"});

        final String expected = "\"Data\" : {\n"
                + "    \"jobDetails\" : {\n"
                + "      \"Nodes\" : null,\n"
                + "      \"CachedSizeInBytes\" : 0,\n"
                + "      \"CompletedSizeInBytes\" : 0,\n"
                + "      \"OriginalSizeInBytes\" : 32,\n"
                + "      \"BucketName\" : \"bucket\",\n"
                + "      \"JobId\" : \"aa5df0cc-b03a-4cb9-b69d-56e7367e917f\",\n"
                + "      \"UserId\" : \"c2581493-058c-40d7-a3a1-9a50b20d6d3b\",\n"
                + "      \"UserName\" : \"spectra\",\n"
                + "      \"WriteOptimization\" : \"CAPACITY\",\n"
                + "      \"Priority\" : \"HIGH\",\n"
                + "      \"RequestType\" : \"GET\",\n"
                + "      \"StartDate\" : \"2015-09-28T17:30:43.000Z\",\n"
                + "      \"ChunkClientProcessingOrderGuarantee\" : \"NONE\",\n"
                + "      \"Status\" : \"COMPLETED\",\n"
                + "      \"Objects\" : null\n"
                + "    }\n"
                + "  },\n  \"Status\" : \"OK\"\n"
                + "}";

        final String response = "<MasterObjectList BucketName=\"bucket\" CachedSizeInBytes=\"0\" ChunkClientProcessingOrderGuarantee=\"NONE\" CompletedSizeInBytes=\"0\" JobId=\"aa5df0cc-b03a-4cb9-b69d-56e7367e917f\" OriginalSizeInBytes=\"32\" Priority=\"HIGH\" RequestType=\"GET\" StartDate=\"2015-09-28T17:30:43.000Z\" Status=\"COMPLETED\" UserId=\"c2581493-058c-40d7-a3a1-9a50b20d6d3b\" UserName=\"spectra\" WriteOptimization=\"CAPACITY\"></MasterObjectList>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetJobResponse getJobResponse = new GetJobResponse(webResponse);
        when(client.getJob(any(GetJobRequest.class))).thenReturn(getJobResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getBulk() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bulk", "-b", "bucketName"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        when(helpers.startReadAllJob(eq("bucketName"), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all the objects from bucketName to directory ."));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getBulkWithSync() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bulk", "-b", "bucketName", "--sync"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull(), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        final Contents c1 = new Contents();
        c1.setKey("obj1.txt");
        c1.setSize(123L);
        final Contents c2 = new Contents();
        c2.setKey("obj2.txt");
        c2.setSize(123L);

        final Iterable<Contents> retCont = Lists.newArrayList(c1, c2);
        when(helpers.listObjects(eq("bucketName"), any(String.class))).thenReturn(retCont);


        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        PowerMockito.mockStatic(Utils.class);
        when(Utils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(Utils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(Utils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");

        PowerMockito.mockStatic(SyncUtils.class);
        when(SyncUtils.isNewFile(any(Path.class), any(Contents.class), any(Boolean.class))).thenReturn(false);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: All files are up to date"));
        assertThat(result.getReturnCode(), is(0));

        when(SyncUtils.isNewFile(any(Path.class), any(Contents.class), any(Boolean.class))).thenReturn(true);
        result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: Synced all the objects from bucketName to ."));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getBulkJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Wrote all the objects from bucketName to directory .\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bulk", "-b", "bucketName", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        when(helpers.startReadAllJob(eq("bucketName"), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putBulk() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        final UUID jobId = UUID.randomUUID();
        when(mockedGetJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(Utils.class);
        when(Utils.getObjectsForDirectory(eq(retPath), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.getObjectsForDirectory(any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(Utils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(Utils.getFileSize(eq(p1))).thenReturn(1245L);
        when(Utils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(Utils.getFileSize(eq(p2))).thenReturn(12345L);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all the files in dir to bucket bucketName"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putBulkWithSync() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--sync"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245L), new Ds3Object("obj2.txt", 1245L));

        final UUID jobId = UUID.randomUUID();
        when(mockedGetJob.getJobId()).thenReturn(jobId);
        when(helpers.listObjectsForDirectory(any(Path.class))).thenReturn(retObj);
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedGetJob);

        final Contents c1 = new Contents();
        c1.setKey("obj1.txt");
        final Contents c2 = new Contents();
        c2.setKey("obj2.txt");

        final Iterable<Contents> retCont = Lists.newArrayList(c1, c2);
        when(helpers.listObjects(eq("bucketName"), any(String.class))).thenReturn(retCont);

        PowerMockito.mockStatic(SyncUtils.class);
        when(SyncUtils.isSyncSupported(any(Ds3Client.class))).thenReturn(true);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        PowerMockito.mockStatic(Utils.class);
        when(Utils.getObjectsForDirectory(eq(retPath), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.getObjectsForDirectory(any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(Utils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(Utils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);

        when(SyncUtils.isNewFile(any(Path.class), any(Contents.class), any(Boolean.class))).thenReturn(false);
        CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: All files are up to date"));
        assertThat(result.getReturnCode(), is(0));

        when(SyncUtils.isNewFile(any(Path.class), any(Contents.class), any(Boolean.class))).thenReturn(true);
        when(Utils.getFileSize(any(Path.class))).thenReturn(1245L);
        result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all the files in dir to bucket bucketName"));
        assertThat(result.getReturnCode(), is(0));

    }

    @Test
    public void putBulkWithSyncWrongVersion() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--sync"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));

        final UUID jobId = UUID.randomUUID();
        when(mockedGetJob.getJobId()).thenReturn(jobId);
        when(helpers.listObjectsForDirectory(any(Path.class))).thenReturn(retObj);
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedGetJob);

        final Ds3Client client = mock(Ds3Client.class);
        final GetSystemInformationResponse systemInformationResponse = mock(GetSystemInformationResponse.class);
        final SystemInformation systemInformation = mock(SystemInformation.class);
        final SystemInformation.BuildInformation buildInformation = mock(SystemInformation.BuildInformation.class);
        when(buildInformation.getVersion()).thenReturn("1.2.0");
        when(systemInformation.getBuildInformation()).thenReturn(buildInformation);
        when(systemInformationResponse.getSystemInformation()).thenReturn(systemInformation);
        when(client.getSystemInformation(any(GetSystemInformationRequest.class))).thenReturn(systemInformationResponse);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("Failed: The sync command is not supported with your version of BlackPearl."));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putBulkJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Wrote all the files in dir to bucket bucketName\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        PowerMockito.mockStatic(Utils.class);
        when(Utils.getObjectsForDirectory(eq(retPath), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.getObjectsForDirectory(any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(Utils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(Utils.getFileSize(eq(p1))).thenReturn(1245L);
        when(Utils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(Utils.getFileSize(eq(p2))).thenReturn(12345L);

        final UUID jobId = UUID.randomUUID();
        when(mockedGetJob.getJobId()).thenReturn(jobId);
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void deleteTapeDrive() throws Exception {
        final String expected = "Success: Deleted tape drive 'someIdValue'.";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_tape_drive", "-i", "someIdValue"});
        final Ds3Client client = mock(Ds3Client.class);

        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteTapeDriveResponse deleteTapeDriveResponse = new DeleteTapeDriveResponse(webResponse);
        when(client.deleteTapeDrive(any(DeleteTapeDriveRequest.class))).thenReturn(deleteTapeDriveResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void deleteTapeDriveJson() throws Exception {
        final String expected = "  \"Status\" : \"OK\",\n" +
                "  \"Message\" : \"Success: Deleted tape drive 'someIdValue'.\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_tape_drive", "-i", "someIdValue", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteTapeDriveResponse deleteTapeDriveResponse = new DeleteTapeDriveResponse(webResponse);
        when(client.deleteTapeDrive(any(DeleteTapeDriveRequest.class))).thenReturn(deleteTapeDriveResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void deleteTapePartition() throws Exception {
        final String expected = "Success: Deleted tape partition 'someIdValue'.";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_tape_partition", "-i", "someIdValue"});
        final Ds3Client client = mock(Ds3Client.class);

        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteTapePartitionResponse deleteTapePartitionResponse = new DeleteTapePartitionResponse(webResponse);
        when(client.deleteTapePartition(any(DeleteTapePartitionRequest.class))).thenReturn(deleteTapePartitionResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void deleteTapePartitionJson() throws Exception {
        final String expected = "  \"Status\" : \"OK\",\n" +
                "  \"Message\" : \"Success: Deleted tape partition 'someIdValue'.\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_tape_partition", "-i", "someIdValue", "--output-format", "json"});

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteTapePartitionResponse deleteTapePartitionResponse = new DeleteTapePartitionResponse(webResponse);
        when(client.deleteTapePartition(any(DeleteTapePartitionRequest.class))).thenReturn(deleteTapePartitionResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void isCliSupportedTest() throws IOException, SignatureException {
        final Ds3Client client = mock(Ds3Client.class);
        final GetSystemInformationResponse systemInformationResponse = mock(GetSystemInformationResponse.class);
        final SystemInformation systemInformation = mock(SystemInformation.class);
        final SystemInformation.BuildInformation buildInformation = mock(SystemInformation.BuildInformation.class);

        when(systemInformation.getBuildInformation()).thenReturn(buildInformation);
        when(systemInformationResponse.getSystemInformation()).thenReturn(systemInformation);
        when(client.getSystemInformation(any(GetSystemInformationRequest.class))).thenReturn(systemInformationResponse);

        when(buildInformation.getVersion()).thenReturn("1.2.0");
        assertTrue(Utils.isCliSupported(client));

        when(buildInformation.getVersion()).thenReturn("3.0.0");
        assertTrue(Utils.isCliSupported(client));

        when(buildInformation.getVersion()).thenReturn("1.1.0");
        assertFalse(Utils.isCliSupported(client));
    }

    @Test
    public void putBulkWithIgnoreErrors() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--ignore-errors"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final Path p3 = Paths.get("obj3.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2, p3));

        final UUID jobId = UUID.randomUUID();
        when(mockedGetJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(
                new Ds3Object("obj1.txt", 1245),
                new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(Utils.class);
        when(Utils.getObjectsForDirectory(eq(retPath), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.getObjectsForDirectory(any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(Utils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(Utils.getFileSize(eq(p1))).thenReturn(1245L);
        when(Utils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(Utils.getFileSize(eq(p2))).thenReturn(12345L);
        when(Utils.nullGuard(any(String.class))).thenCallRealMethod();

        final IOException ex = new IOException("java.nio.file.NoSuchFileException: obj3.txt");
        when(Utils.getFileSize(eq(p3))).thenThrow(ex);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        final String expected = "WARN: Not all of the files in dir were written to bucket bucketName\n" +
                "+--------------+------------------------------------------------------------------+\n" +
                "| Ignored File |                              Reason                              |\n" +
                "+--------------+------------------------------------------------------------------+\n" +
                "| obj3.txt     | java.io.IOException: java.nio.file.NoSuchFileException: obj3.txt |\n" +
                "+--------------+------------------------------------------------------------------+\n";
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putBulkWithIgnoreErrorsJson() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--ignore-errors", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final Path p3 = Paths.get("obj3.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2, p3));

        final UUID jobId = UUID.randomUUID();
        when(mockedGetJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(
                new Ds3Object("obj1.txt", 1245),
                new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(Utils.class);
        when(Utils.getObjectsForDirectory(eq(retPath), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.getObjectsForDirectory(any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(Utils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(Utils.getFileSize(eq(p1))).thenReturn(1245L);
        when(Utils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(Utils.getFileSize(eq(p2))).thenReturn(12345L);
        when(Utils.nullGuard(any(String.class))).thenCallRealMethod();

        final IOException ex = new IOException("java.nio.file.NoSuchFileException: obj3.txt");
        when(Utils.getFileSize(eq(p3))).thenThrow(ex);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();

        final String startWith = "{\n" +
                "  \"Meta\" : {";

        final String endsWith = "},\n" +
                "  \"Data\" : {\n" +
                "    \"status_message\" : \"WARN: Not all of the files in dir were written to bucket bucketName\",\n" +
                "    \"ignored_files\" : [ {\n" +
                "      \"path\" : \"obj3.txt\",\n" +
                "      \"error_message\" : \"java.io.IOException: java.nio.file.NoSuchFileException: obj3.txt\"\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"Status\" : \"OK\"\n" +
                "}";

        assertTrue(result.getMessage().startsWith(startWith));
        assertTrue(result.getMessage().endsWith(endsWith));
    }
}
