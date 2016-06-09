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
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.exceptions.SyncNotSupportedException;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.*;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.models.Error;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.networking.Headers;
import com.spectralogic.ds3client.networking.WebResponse;
import com.spectralogic.ds3client.serializer.XmlOutput;
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.StringEndsWith;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@PrepareForTest({Utils.class, SyncUtils.class, BlackPearlUtils.class, GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Response.class})
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
                "    <ID>5df00f88-d5b2-11e5-ab30-625662870761</ID>\n" +
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
                "    \"Buckets\" : [ {\n" +
                "      \"CreationDate\" : \"2006-02-03T16:45:09.000Z\",\n" +
                "      \"Name\" : \"quotes\"\n" +
                "    }, {\n" +
                "      \"CreationDate\" : \"2006-02-03T16:41:58.000Z\",\n" +
                "      \"Name\" : \"samples\"\n" +
                "    } ],\n" +
                "    \"Owner\" : {\n" +
                "      \"DisplayName\" : \"webfile\",\n" +
                "      \"ID\" : \"5df00f88-d5b2-11e5-ab30-625662870761\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"Status\" : \"OK\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_service", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final InputStream stream = IOUtils.toInputStream("<ListAllMyBucketsResult>\n" +
                "  <Owner>\n" +
                "    <ID>5df00f88-d5b2-11e5-ab30-625662870761</ID>\n" +
                "    <DisplayName>webfile</DisplayName>\n" +
                "  </Owner>\n" +
                "  <Buckets>\n" +
                "    <Bucket>\n" +
                "      <CreationDate>2006-02-03T16:45:09.000Z</CreationDate>\n" +
                "      <Name>quotes</Name>\n" +
                "    </Bucket>\n" +
                "    <Bucket>\n" +
                "      <CreationDate>2006-02-03T16:41:58.000Z</CreationDate>\n" +
                "      <Name>samples</Name>\n" +
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

        final DeleteFolderRecursivelySpectraS3Response deleteFolderResponse = new DeleteFolderRecursivelySpectraS3Response(webResponse);
        when(client.deleteFolderRecursivelySpectraS3(any(DeleteFolderRecursivelySpectraS3Request.class))).thenReturn(deleteFolderResponse);

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

        final DeleteFolderRecursivelySpectraS3Response deleteFolderResponse = new DeleteFolderRecursivelySpectraS3Response(webResponse);
        when(client.deleteFolderRecursivelySpectraS3(any(DeleteFolderRecursivelySpectraS3Request.class))).thenReturn(deleteFolderResponse);

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
                "            <ID>5df00f88-d5b2-11e5-ab30-625662870761</ID>\n" +
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
                "            <ID>5df00f88-d5b2-11e5-ab30-625662870761</ID>\n" +
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
                        "      \"etag\" : \"\\\"fba9dede5f27731c9771645a39863328\\\"\",\n" + // TODO investigate double printing of etag
                        "      \"ETag\" : \"\\\"fba9dede5f27731c9771645a39863328\\\"\",\n" +
                        "      \"Key\" : \"my-image.jpg\",\n" +
                        "      \"LastModified\" : \"2009-10-12T17:50:30.000Z\",\n" +
                        "      \"Owner\" : {\n" +
                        "        \"DisplayName\" : \"mtd@amazon.com\",\n" +
                        "        \"ID\" : \"5df00f88-d5b2-11e5-ab30-625662870761\"\n" +
                        "      },\n" +
                        "      \"Size\" : 434234,\n" +
                        "      \"StorageClass\" : \"STANDARD\"\n" +
                        "    }, {\n" +
                        "      \"etag\" : \"\\\"1b2cf535f27731c974343645a3985328\\\"\",\n" + // TODO investigate double printing of etag
                        "      \"ETag\" : \"\\\"1b2cf535f27731c974343645a3985328\\\"\",\n" +
                        "      \"Key\" : \"my-third-image.jpg\",\n" +
                        "      \"LastModified\" : \"2009-10-12T17:50:30.000Z\",\n" +
                        "      \"Owner\" : {\n" +
                        "        \"DisplayName\" : \"mtd@amazon.com\",\n" +
                        "        \"ID\" : \"5df00f88-d5b2-11e5-ab30-625662870761\"\n" +
                        "      },\n" +
                        "      \"Size\" : 64994,\n" +
                        "      \"StorageClass\" : \"STANDARD\"\n" +
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
                "            <ID>5df00f88-d5b2-11e5-ab30-625662870761</ID>\n" +
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
                "            <ID>5df00f88-d5b2-11e5-ab30-625662870761</ID>\n" +
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
        final ModifyJobSpectraS3Response modifyJobResponse = new ModifyJobSpectraS3Response(webResponse);
        when(client.modifyJobSpectraS3(any(ModifyJobSpectraS3Request.class))).thenReturn(modifyJobResponse);

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
        final ModifyJobSpectraS3Response modifyJobResponse = new ModifyJobSpectraS3Response(webResponse);
        when(client.modifyJobSpectraS3(any(ModifyJobSpectraS3Request.class))).thenReturn(modifyJobResponse);

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
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);

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
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);

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

    @Test(expected = SyncNotSupportedException.class)
    public void putObjectWithSyncNotSupportedVersion() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_object", "-b", "bucketName", "-o", "obj.txt", "--sync"});

        final BuildInformation buildInformation = mock(BuildInformation.class);
        when(buildInformation.getVersion()).thenReturn("1.2.0");

        final SystemInformation systemInformation = mock(SystemInformation.class);
        when(systemInformation.getBuildInformation()).thenReturn(buildInformation);

        final GetSystemInformationSpectraS3Response systemInformationResponse = mock(GetSystemInformationSpectraS3Response.class);
        when(systemInformationResponse.getSystemInformationResult()).thenReturn(systemInformation);

        final Ds3Client client = mock(Ds3Client.class);
        when(client.getSystemInformationSpectraS3(any(GetSystemInformationSpectraS3Request.class))).thenReturn(systemInformationResponse);

        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);

        final FileUtils mockedFileUtils = mock(FileUtils.class);
        when(mockedFileUtils.exists(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.isRegularFile(any(Path.class))).thenReturn(true);
        when(mockedFileUtils.size(any(Path.class))).thenReturn(100L);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, helpers), args, mockedFileUtils);
        cli.call(); //should throw SyncNotSupportedException
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
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);

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

        PowerMockito.mockStatic(SyncUtils.class);
        when(SyncUtils.isSyncSupported(any(Ds3Client.class))).thenReturn(true);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: Finished downloading object.  The object was written to: ." + SterilizeString.getFileDelimiter() + "obj.txt"));
        assertThat(result.getReturnCode(), is(0));

        when(Utils.fileExists(any(Path.class))).thenReturn(true);
        final Contents c1 = new Contents();
        c1.setKey("obj.txt");

        final Iterable<Contents> retCont = Lists.newArrayList(c1);
        when(helpers.listObjects(eq("bucketName"))).thenReturn(retCont);

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
        final GetJobSpectraS3Response getJobResponse = new GetJobSpectraS3Response(webResponse);
        when(client.getJobSpectraS3(any(GetJobSpectraS3Request.class))).thenReturn(getJobResponse);

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
                + "      \"aggregating\" : false,\n"
                + "      \"bucketName\" : \"bucket\",\n"
                + "      \"cachedSizeInBytes\" : 0,\n"
                + "      \"chunkClientProcessingOrderGuarantee\" : \"NONE\",\n"
                + "      \"completedSizeInBytes\" : 0,\n"
                + "      \"jobId\" : \"aa5df0cc-b03a-4cb9-b69d-56e7367e917f\",\n"
                + "      \"naked\" : false,\n"
                + "      \"name\" : null,\n"
                + "      \"originalSizeInBytes\" : 32,\n"
                + "      \"priority\" : \"HIGH\",\n"
                + "      \"requestType\" : \"GET\",\n"
                + "      \"startDate\" : \"2015-09-28T17:30:43.000Z\",\n"
                + "      \"status\" : \"COMPLETED\",\n"
                + "      \"userId\" : \"c2581493-058c-40d7-a3a1-9a50b20d6d3b\",\n"
                + "      \"userName\" : \"spectra\",\n"
                + "      \"Nodes\" : [ ],\n"
                + "      \"Objects\" : [ ]\n"
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
        final GetJobSpectraS3Response getJobResponse = new GetJobSpectraS3Response(webResponse);
        when(client.getJobSpectraS3(any(GetJobSpectraS3Request.class))).thenReturn(getJobResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), StringEndsWith.endsWith(expected));
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
        when(SyncUtils.isSyncSupported(any(Ds3Client.class))).thenReturn(true);
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
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);

        PowerMockito.mockStatic(Utils.class);
        when(Utils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
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
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245L), new Ds3Object("obj2.txt", 1245L));

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        when(helpers.listObjectsForDirectory(any(Path.class))).thenReturn(retObj);
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);

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
        when(Utils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
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

    @Test(expected = SyncNotSupportedException.class)
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
        final GetSystemInformationSpectraS3Response systemInformationResponse = mock(GetSystemInformationSpectraS3Response.class);
        final SystemInformation systemInformation = mock(SystemInformation.class);
        final BuildInformation buildInformation = mock(BuildInformation.class);
        when(buildInformation.getVersion()).thenReturn("1.2.0");
        when(systemInformation.getBuildInformation()).thenReturn(buildInformation);
        when(systemInformationResponse.getSystemInformationResult()).thenReturn(systemInformation);
        when(client.getSystemInformationSpectraS3(any(GetSystemInformationSpectraS3Request.class))).thenReturn(systemInformationResponse);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, helpers), args, mockedFileUtils);
        cli.call(); //should throw SyncNotSupportedException
    }

    @Test
    public void putBulkJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Wrote all the files in dir to bucket bucketName\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        PowerMockito.mockStatic(Utils.class);
        when(Utils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(Utils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(Utils.getFileSize(eq(p1))).thenReturn(1245L);
        when(Utils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(Utils.getFileSize(eq(p2))).thenReturn(12345L);

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void deleteTapeDrive() throws Exception {
        final String expected = "Success: Deleted tape drive 'c2581493-058c-40d7-a3a1-9a50b20d6d3b'.";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_tape_drive", "-i", "c2581493-058c-40d7-a3a1-9a50b20d6d3b"});
        final Ds3Client client = mock(Ds3Client.class);

        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteTapeDriveSpectraS3Response deleteTapeDriveResponse = new DeleteTapeDriveSpectraS3Response(webResponse);
        when(client.deleteTapeDriveSpectraS3(any(DeleteTapeDriveSpectraS3Request.class))).thenReturn(deleteTapeDriveResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void deleteTapeDriveJson() throws Exception {
        final String expected = "  \"Status\" : \"OK\",\n" +
                "  \"Message\" : \"Success: Deleted tape drive 'c2581493-058c-40d7-a3a1-9a50b20d6d3b'.\"\n" +
                "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "delete_tape_drive", "-i", "c2581493-058c-40d7-a3a1-9a50b20d6d3b", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final DeleteTapeDriveSpectraS3Response deleteTapeDriveResponse = new DeleteTapeDriveSpectraS3Response(webResponse);
        when(client.deleteTapeDriveSpectraS3(any(DeleteTapeDriveSpectraS3Request.class))).thenReturn(deleteTapeDriveResponse);

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

        final DeleteTapePartitionSpectraS3Response deleteTapePartitionResponse = new DeleteTapePartitionSpectraS3Response(webResponse);
        when(client.deleteTapePartitionSpectraS3(any(DeleteTapePartitionSpectraS3Request.class))).thenReturn(deleteTapePartitionResponse);

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

        final DeleteTapePartitionSpectraS3Response deleteTapePartitionResponse = new DeleteTapePartitionSpectraS3Response(webResponse);
        when(client.deleteTapePartitionSpectraS3(any(DeleteTapePartitionSpectraS3Request.class))).thenReturn(deleteTapePartitionResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);

        final CommandResponse result = cli.call();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void isCliSupportedTest() throws IOException, SignatureException {
        final Ds3Client client = mock(Ds3Client.class);
        final GetSystemInformationSpectraS3Response systemInformationResponse = mock(GetSystemInformationSpectraS3Response.class);
        final SystemInformation systemInformation = mock(SystemInformation.class);
        final BuildInformation buildInformation = mock(BuildInformation.class);

        when(systemInformation.getBuildInformation()).thenReturn(buildInformation);
        when(systemInformationResponse.getSystemInformationResult()).thenReturn(systemInformation);
        when(client.getSystemInformationSpectraS3(any(GetSystemInformationSpectraS3Request.class))).thenReturn(systemInformationResponse);

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
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final Path p3 = Paths.get("obj3.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2, p3));

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(
                new Ds3Object("obj1.txt", 1245),
                new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);

        PowerMockito.mockStatic(Utils.class);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(Utils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
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
        final String expected = "WARN: Not all of the files were written to bucket bucketName\n" +
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
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final Path p3 = Paths.get("obj3.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2, p3));

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(
                new Ds3Object("obj1.txt", 1245),
                new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);

        PowerMockito.mockStatic(Utils.class);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(Utils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
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
                "    \"status_message\" : \"WARN: Not all of the files were written to bucket bucketName\",\n" +
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

    @Test
    public void putBulkWithPipe() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        PowerMockito.mockStatic(Utils.class);
        when(Utils.normalizeObjectName(any(String.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                return (String) args[0];
            }
        });
        when(Utils.isPipe()).thenReturn(true);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(Utils.getPipedFilesFromStdin(any(FileUtils.class))).thenReturn(retPath);
        when(Utils.getObjectsToPut((Iterable<Path>) isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.getFileName(any(Path.class), eq(p1))).thenReturn(p1.toString());
        when(Utils.getFileSize(eq(p1))).thenReturn(1245L);
        when(Utils.getFileName(any(Path.class), eq(p2))).thenReturn(p2.toString());
        when(Utils.getFileSize(eq(p2))).thenReturn(12345L);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all piped files to bucket bucketName"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putBulkWithPipeAndSync() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "--sync"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);

        final Iterable<Contents> retCont = Lists.newArrayList();
        when(helpers.listObjects(eq("bucketName"), any(String.class))).thenReturn(retCont);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        PowerMockito.mockStatic(Utils.class);
        when(Utils.normalizeObjectName(any(String.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                return (String) args[0];
            }
        });

        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(Utils.isPipe()).thenReturn(true);
        when(Utils.getPipedFilesFromStdin(any(FileUtils.class))).thenReturn(retPath);
        when(Utils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(Utils.getFileName(any(Path.class), eq(p1))).thenReturn(p1.toString());
        when(Utils.getFileSize(eq(p1))).thenReturn(1245L);
        when(Utils.getFileName(any(Path.class), eq(p2))).thenReturn(p2.toString());
        when(Utils.getFileSize(eq(p2))).thenReturn(12345L);

        PowerMockito.mockStatic(SyncUtils.class);
        when(SyncUtils.isSyncSupported(any(Ds3Client.class))).thenReturn(true);

        PowerMockito.mockStatic(BlackPearlUtils.class);


        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all piped files to bucket bucketName"));
        assertThat(result.getReturnCode(), is(0));

        final Contents c1 = new Contents();
        c1.setKey("obj1.txt");
        final Contents c2 = new Contents();
        c2.setKey("obj2.txt");

        final Iterable<Contents> retCont2 = Lists.newArrayList(c1, c2);
        when(helpers.listObjects(eq("bucketName"), any(String.class))).thenReturn(retCont2);
        final CommandResponse result2 = cli.call();
        assertThat(result2.getMessage(), is("SUCCESS: All files are up to date"));
        assertThat(result2.getReturnCode(), is(0));
    }

    @Test
    public void putBulkWithPipeAndOtherArgs() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final FileUtils mockedFileUtils = mock(FileUtils.class);

        PowerMockito.mockStatic(Utils.class);
        when(Utils.isPipe()).thenReturn(true);

        PowerMockito.mockStatic(BlackPearlUtils.class);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args, mockedFileUtils);
        try {
            cli.call();
        } catch (final BadArgumentException ex) {
            assertEquals("-d, -o and -p arguments are not supported when using piped input", ex.getMessage());
        } catch (final Exception ex) {
            fail(); //This is the wrong exception
        }

        final Arguments args2 = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-o", "obj"});
        final Ds3Cli cli2 = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args2, mockedFileUtils);
        try {
            cli2.call();
        } catch (final BadArgumentException ex) {
            assertEquals("-d, -o and -p arguments are not supported when using piped input", ex.getMessage());
        } catch (final Exception ex) {
            fail(); //This is the wrong exception
        }

        final Arguments args3 = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-p", "prefix"});
        final Ds3Cli cli3 = new Ds3Cli(new Ds3ProviderImpl(null, helpers), args3, mockedFileUtils);
        try {
            cli3.call();
        } catch (final BadArgumentException ex) {
            assertEquals("-d, -o and -p arguments are not supported when using piped input", ex.getMessage());
        } catch (final Exception ex) {
            fail(); //This is the wrong exception
        }
    }

    @Test
    public void testGetPhysicalPlacementOnTape() throws Exception {
        final Tape tape1 = new Tape();
        tape1.setAssignedToStorageDomain(false);
        tape1.setAvailableRawCapacity(10000L);
        tape1.setBarCode("121557L6");
        tape1.setFullOfData(false);
        final UUID tape1Id = UUID.randomUUID();
        tape1.setId(tape1Id);
        final UUID tape1PartitionId = UUID.randomUUID();
        tape1.setPartitionId(tape1PartitionId);
        tape1.setState(TapeState.PENDING_INSPECTION);
        final UUID tape1StorageDomainId = UUID.randomUUID();
        tape1.setStorageDomainId(tape1StorageDomainId);
        tape1.setTakeOwnershipPending(false);
        tape1.setTotalRawCapacity(20000L);
        tape1.setType(TapeType.LTO6);
        tape1.setWriteProtected(false);
        tape1.setEjectLabel("Tape1EjectLabel");
        tape1.setEjectLocation("Tape1EjectLocation");

        final Tape tape2 = new Tape();
        tape2.setAssignedToStorageDomain(false);
        tape2.setAvailableRawCapacity(10000L);
        tape2.setBarCode("421555L7");
        tape2.setFullOfData(false);
        final UUID tape2Id = UUID.randomUUID();
        tape2.setId(tape2Id);
        final UUID tape2PartitionId = UUID.randomUUID();
        tape2.setPartitionId(tape2PartitionId);
        tape2.setState(TapeState.PENDING_INSPECTION);
        final UUID tape2StorageDomainId = UUID.randomUUID();
        tape2.setStorageDomainId(tape2StorageDomainId);
        tape2.setTakeOwnershipPending(false);
        tape2.setTotalRawCapacity(20000L);
        tape2.setType(TapeType.LTO7);
        tape2.setWriteProtected(false);
        tape2.setEjectLabel("Tape2EjectLabel");
        tape2.setEjectLocation("Tape2EjectLocation");

        final BulkObject testObject = new BulkObject();
        testObject.setName("testObject");
        testObject.setLength(1024L);
        final PhysicalPlacement physicalPlacement = new PhysicalPlacement();
        physicalPlacement.setPools(null);
        physicalPlacement.setTapes(Lists.newArrayList(tape1, tape2));
        testObject.setPhysicalPlacement(physicalPlacement);
        final BulkObjectList bulkObjectList = new BulkObjectList();
        bulkObjectList.setObjects(Lists.newArrayList(testObject));

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(XmlOutput.toXml(bulkObjectList), "utf-8"));

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_physical_placement", "-b", "bucketName", "-o", "testObject"});

        final GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Response response = PowerMockito.spy(new GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Response(webResponse));
        PowerMockito.doNothing().when(response, "processResponse");

        when(response.getBulkObjectListResult()).thenReturn(bulkObjectList);
        when(client.getPhysicalPlacementForObjectsWithFullDetailsSpectraS3(any(GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Request.class))).thenReturn(response);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final CommandResponse result = cli.call();

        assertTrue(result.getMessage().contains("| Object Name | ID | In Cache | Length | Offset | Latest | Version |"));
        assertTrue(result.getMessage().contains("| testObject  |    | Unknown  | 1024   | 0      | false  | 0       |"));

        assertTrue(result.getMessage().contains("| Tape Bar Code |        State       | Type | Description |   Eject Label   |   Eject Location   |"));
        assertTrue(result.getMessage().contains("| 121557L6      | PENDING_INSPECTION | LTO6 | N/A         | Tape1EjectLabel | Tape1EjectLocation |"));
        assertTrue(result.getMessage().contains("| 421555L7      | PENDING_INSPECTION | LTO7 | N/A         | Tape2EjectLabel | Tape2EjectLocation |"));

        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void testGetPhysicalPlacementOnPool() throws Exception {
        final Pool pool1 = new Pool();
        final UUID pool1Id = UUID.randomUUID();
        pool1.setId(pool1Id);
        pool1.setAssignedToStorageDomain(false);
        pool1.setHealth(PoolHealth.OK);
        pool1.setAvailableCapacity(42000L);
        pool1.setMountpoint("mountpoint-1");
        pool1.setName("pool1");
        pool1.setPoweredOn(true);
        pool1.setQuiesced(Quiesced.NO);
        pool1.setReservedCapacity(0L);
        pool1.setState(PoolState.NORMAL);
        final UUID pool1StorageDomainId = UUID.randomUUID();
        pool1.setStorageDomainId(pool1StorageDomainId);
        pool1.setTotalCapacity(420000L);
        pool1.setType(PoolType.NEARLINE);
        pool1.setUsedCapacity(6L*7L);

        final Pool pool2 = new Pool();
        final UUID pool2Id = UUID.randomUUID();
        pool2.setId(pool2Id);
        pool2.setAssignedToStorageDomain(false);
        pool2.setHealth(PoolHealth.OK);
        pool2.setAvailableCapacity(42000L);
        pool2.setMountpoint("mountpoint-2");
        pool2.setName("pool2");
        pool2.setPoweredOn(true);
        pool2.setQuiesced(Quiesced.NO);
        pool2.setReservedCapacity(0L);
        pool2.setState(PoolState.NORMAL);
        final UUID pool2StorageDomainId = UUID.randomUUID();
        pool2.setStorageDomainId(pool2StorageDomainId);
        pool2.setTotalCapacity(420000L);
        pool2.setType(PoolType.NEARLINE);
        pool2.setUsedCapacity(6L*7L);

        final BulkObject testObject = new BulkObject();
        testObject.setName("testObject");
        testObject.setLength(1024L);
        final PhysicalPlacement physicalPlacement = new PhysicalPlacement();
        physicalPlacement.setPools(Lists.newArrayList(pool1, pool2));
        physicalPlacement.setTapes(null);
        testObject.setPhysicalPlacement(physicalPlacement);
        final BulkObjectList bulkObjectList = new BulkObjectList();
        bulkObjectList.setObjects(Lists.newArrayList(testObject));

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(XmlOutput.toXml(bulkObjectList), "utf-8"));

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_physical_placement", "-b", "bucketName", "-o", "testObject"});

        final GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Response response = PowerMockito.spy(new GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Response(webResponse));
        PowerMockito.doNothing().when(response, "processResponse");

        when(response.getBulkObjectListResult()).thenReturn(bulkObjectList);
        when(client.getPhysicalPlacementForObjectsWithFullDetailsSpectraS3(any(GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Request.class))).thenReturn(response);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final CommandResponse result = cli.call();

        assertTrue(result.getMessage().contains("| Object Name | ID | In Cache | Length | Offset | Latest | Version |"));
        assertTrue(result.getMessage().contains("| testObject  |    | Unknown  | 1024   | 0      | false  | 0       |"));

        assertTrue(result.getMessage().contains("| Pool Name |                  ID                  | Bucket ID |  State | Health |   Type   | Partition ID |"));
        assertTrue(result.getMessage().contains("| pool1     | " + pool1Id.toString() +            " |           | NORMAL | OK     | NEARLINE |              |"));
        assertTrue(result.getMessage().contains("| pool2     | " + pool2Id.toString() +            " |           | NORMAL | OK     | NEARLINE |              |"));

        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getDataPolicies() throws Exception {

        final String expected = "+------+--------------------------+------------+---------------+-------------------------+------------------+-------------------+--------------------------+--------------------------+-----------------------------+--------------------------------------+--------------------+\n"
                +"| Name |          Created         | Versioning | Checksum Type | End-to-End CRC Required | Blobbing Enabled | Default Blob Size | Default Get Job Priority | Default Put Job Priority | Default Verify Job Priority |                  Id                  | LTFS Object Naming |\n"
                +"+------+--------------------------+------------+---------------+-------------------------+------------------+-------------------+--------------------------+--------------------------+-----------------------------+--------------------------------------+--------------------+\n"
                +"| fred | 2016-04-26T14:17:04.000Z |       NONE |           MD5 |                   false |            false |        1073741824 |                     HIGH |                   NORMAL |                         LOW | d3e6e795-fc85-4163-9d2f-4bc271d995d0 |               true |\n"
                +"+------+--------------------------+------------+---------------+-------------------------+------------------+-------------------+--------------------------+--------------------------+-----------------------------+--------------------------------------+--------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_data_policies"});
        final String response = "<Data><DataPolicy>" +
                "<BlobbingEnabled>false</BlobbingEnabled>" +
                "<ChecksumType>MD5</ChecksumType>" +
                "<CreationDate>2016-04-26T14:17:04.000Z</CreationDate>" +
                "<DefaultBlobSize>1073741824</DefaultBlobSize>" +
                "<DefaultGetJobPriority>HIGH</DefaultGetJobPriority>" +
                "<DefaultPutJobPriority>NORMAL</DefaultPutJobPriority>" +
                "<DefaultVerifyJobPriority>LOW</DefaultVerifyJobPriority>" +
                "<EndToEndCrcRequired>false</EndToEndCrcRequired>" +
                "<Id>d3e6e795-fc85-4163-9d2f-4bc271d995d0</Id>" +
                "<LtfsObjectNamingAllowed>true</LtfsObjectNamingAllowed>" +
                "<Name>fred</Name>" +
                "<RebuildPriority>LOW</RebuildPriority>" +
                "<Versioning>NONE</Versioning></DataPolicy></Data>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetDataPoliciesSpectraS3Response GetDataPoliciesResponse = new GetDataPoliciesSpectraS3Response(webResponse);
        when(client.getDataPoliciesSpectraS3(any(GetDataPoliciesSpectraS3Request.class))).thenReturn(GetDataPoliciesResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getDataPolicy() throws Exception {

        final String expected = "+------+--------------------------+------------+---------------+-------------------------+------------------+-------------------+--------------------------+--------------------------+-----------------------------+--------------------------------------+--------------------+\n"
                +"| Name |          Created         | Versioning | Checksum Type | End-to-End CRC Required | Blobbing Enabled | Default Blob Size | Default Get Job Priority | Default Put Job Priority | Default Verify Job Priority |                  Id                  | LTFS Object Naming |\n"
                +"+------+--------------------------+------------+---------------+-------------------------+------------------+-------------------+--------------------------+--------------------------+-----------------------------+--------------------------------------+--------------------+\n"
                +"| fake | 2016-04-26T14:17:04.000Z |       NONE |           MD5 |                   false |            false |        1073741824 |                     HIGH |                   NORMAL |                         LOW | d3e6e795-fc85-4163-9d2f-4bc271d995d0 |               true |\n"
                +"+------+--------------------------+------------+---------------+-------------------------+------------------+-------------------+--------------------------+--------------------------+-----------------------------+--------------------------------------+--------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_data_policy", "-i", "fake"});
        final String response = "<Data>" +
                "<BlobbingEnabled>false</BlobbingEnabled>" +
                "<ChecksumType>MD5</ChecksumType>" +
                "<CreationDate>2016-04-26T14:17:04.000Z</CreationDate>" +
                "<DefaultBlobSize>1073741824</DefaultBlobSize>" +
                "<DefaultGetJobPriority>HIGH</DefaultGetJobPriority>" +
                "<DefaultPutJobPriority>NORMAL</DefaultPutJobPriority>" +
                "<DefaultVerifyJobPriority>LOW</DefaultVerifyJobPriority>" +
                "<EndToEndCrcRequired>false</EndToEndCrcRequired>" +
                "<Id>d3e6e795-fc85-4163-9d2f-4bc271d995d0</Id>" +
                "<LtfsObjectNamingAllowed>true</LtfsObjectNamingAllowed>" +
                "<Name>fake</Name>" +
                "<RebuildPriority>LOW</RebuildPriority>" +
                "<Versioning>NONE</Versioning></Data>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetDataPolicySpectraS3Response GetDataPolicyResponse = new GetDataPolicySpectraS3Response(webResponse);
        when(client.getDataPolicySpectraS3(any(GetDataPolicySpectraS3Request.class))).thenReturn(GetDataPolicyResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void modifyDataPolicy() throws Exception {

        final String expected = "+------+--------------------------+------------+---------------+-------------------------+------------------+-------------------+--------------------------+--------------------------+-----------------------------+--------------------------------------+--------------------+\n"
                +"| Name |          Created         | Versioning | Checksum Type | End-to-End CRC Required | Blobbing Enabled | Default Blob Size | Default Get Job Priority | Default Put Job Priority | Default Verify Job Priority |                  Id                  | LTFS Object Naming |\n"
                +"+------+--------------------------+------------+---------------+-------------------------+------------------+-------------------+--------------------------+--------------------------+-----------------------------+--------------------------------------+--------------------+\n"
                +"| fred | 2016-04-26T14:17:04.000Z |       NONE |           MD5 |                   false |            false |        1073741824 |                     HIGH |                   NORMAL |                         LOW | d3e6e795-fc85-4163-9d2f-4bc271d995d0 |               true |\n"
                +"+------+--------------------------+------------+---------------+-------------------------+------------------+-------------------+--------------------------+--------------------------+-----------------------------+--------------------------------------+--------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "modify_data_policy", "-i", "fake",
                "--metadata",  "name:fred,blobbing_enabled:false,default_blob_size:1073741824,default_get_job_priority:HIGH,end_to_end_crc_required:false,rebuild_priority:LOW,versioning:NONE"});
        final String response = "<Data>" +
                "<BlobbingEnabled>false</BlobbingEnabled>" +
                "<ChecksumType>MD5</ChecksumType>" +
                "<CreationDate>2016-04-26T14:17:04.000Z</CreationDate>" +
                "<DefaultBlobSize>1073741824</DefaultBlobSize>" +
                "<DefaultGetJobPriority>HIGH</DefaultGetJobPriority>" +
                "<DefaultPutJobPriority>NORMAL</DefaultPutJobPriority>" +
                "<DefaultVerifyJobPriority>LOW</DefaultVerifyJobPriority>" +
                "<EndToEndCrcRequired>false</EndToEndCrcRequired>" +
                "<Id>d3e6e795-fc85-4163-9d2f-4bc271d995d0</Id>" +
                "<LtfsObjectNamingAllowed>true</LtfsObjectNamingAllowed>" +
                "<Name>fred</Name>" +
                "<RebuildPriority>LOW</RebuildPriority>" +
                "<Versioning>NONE</Versioning>" +
                "</Data>";

        final Ds3Client client = mock(Ds3Client.class);

        // mock client for "get" call
        final WebResponse webResponse1 = mock(WebResponse.class);
        final Headers headers1 = mock(Headers.class);
        when(webResponse1.getStatusCode()).thenReturn(200);
        when(webResponse1.getHeaders()).thenReturn(headers1);
        when(webResponse1.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetDataPolicySpectraS3Response GetDataPolicyResponse = new GetDataPolicySpectraS3Response(webResponse1);

        // mock client for "modofy" call
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));

        final ModifyDataPolicySpectraS3Response ModifyDataPolicyResponse = new ModifyDataPolicySpectraS3Response(webResponse);
        when(client.modifyDataPolicySpectraS3(any(ModifyDataPolicySpectraS3Request.class))).thenReturn(ModifyDataPolicyResponse);
        when(client.getDataPolicySpectraS3(any(GetDataPolicySpectraS3Request.class))).thenReturn(GetDataPolicyResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void modifyDataPolicyWithBadParam() throws Exception {

        final String expected = "Unrecognized Data Policy parameter: cat";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "modify_data_policy", "-i", "fake",
                "--metadata",  "name:fred,blobbing_enabled:false,default_blob_size:1073741824,default_get_job_priority:HIGH,end_to_end_crc_required:false,rebuild_priority:LOW,versioning:NONE,cat:dog"});

        // set up the mock to retrieve the policy to modify
        final String response = "<Data>" +
                "<BlobbingEnabled>false</BlobbingEnabled>" +
                "<ChecksumType>MD5</ChecksumType>" +
                "<CreationDate>2016-04-26T14:17:04.000Z</CreationDate>" +
                "<DefaultBlobSize>1073741824</DefaultBlobSize>" +
                "<DefaultGetJobPriority>HIGH</DefaultGetJobPriority>" +
                "<DefaultPutJobPriority>NORMAL</DefaultPutJobPriority>" +
                "<DefaultVerifyJobPriority>LOW</DefaultVerifyJobPriority>" +
                "<EndToEndCrcRequired>false</EndToEndCrcRequired>" +
                "<Id>d3e6e795-fc85-4163-9d2f-4bc271d995d0</Id>" +
                "<LtfsObjectNamingAllowed>true</LtfsObjectNamingAllowed>" +
                "<Name>fred</Name>" +
                "<RebuildPriority>LOW</RebuildPriority>" +
                "<Versioning>NONE</Versioning>" +
                "</Data>";

        final Ds3Client client = mock(Ds3Client.class);

        // mock client for "get" call
        final WebResponse webResponse1 = mock(WebResponse.class);
        final Headers headers1 = mock(Headers.class);
        when(webResponse1.getStatusCode()).thenReturn(200);
        when(webResponse1.getHeaders()).thenReturn(headers1);
        when(webResponse1.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetDataPolicySpectraS3Response GetDataPolicyResponse = new GetDataPolicySpectraS3Response(webResponse1);

        // mock client for "modofy" call
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));

        final ModifyDataPolicySpectraS3Response ModifyDataPolicyResponse = new ModifyDataPolicySpectraS3Response(webResponse);
        when(client.modifyDataPolicySpectraS3(any(ModifyDataPolicySpectraS3Request.class))).thenReturn(ModifyDataPolicyResponse);
        when(client.getDataPolicySpectraS3(any(GetDataPolicySpectraS3Request.class))).thenReturn(GetDataPolicyResponse);

        final Ds3Cli cli = new Ds3Cli(new Ds3ProviderImpl(client, null), args, null);
        final CommandResponse result = cli.call();
        assertThat(result.getMessage(), is(expected));
    }

}
