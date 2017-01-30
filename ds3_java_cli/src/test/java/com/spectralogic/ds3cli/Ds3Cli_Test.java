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
import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.command.CliCommandFactory;
import com.spectralogic.ds3cli.command.GetDetailedObjects;
import com.spectralogic.ds3cli.command.GetDetailedObjectsPhysical;
import com.spectralogic.ds3cli.exceptions.*;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
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
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.UnrecognizedOptionException;
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
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3client.utils.ResponseUtils.toImmutableIntList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@PrepareForTest({CliUtils.class, SyncUtils.class, FileUtils.class, Guard.class, GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Response.class})
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
        when(client.getService(any(GetServiceRequest.class))).thenReturn(serviceResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void enumToStrings() throws Exception {
        final String expected = "CRITICAL, URGENT, HIGH, NORMAL, LOW, BACKGROUND";
        final String actual = CliUtils.printEnumOptions(com.spectralogic.ds3client.models.Priority.values());
        assertEquals(actual, expected);
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
        when(client.getService(any(GetServiceRequest.class))).thenReturn(serviceResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test(expected = FailedRequestException.class)
    public void error() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_service"});
        final Ds3Client client = mock(Ds3Client.class);
        when(client.getService(any(GetServiceRequest.class)))
                .thenThrow(new FailedRequestException(toImmutableIntList(new int[]{200}), 500, new Error(), ""));

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
    }

    @Test(expected = FailedRequestException.class)
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getBucket() throws Exception {

        final String expected =
                "+-------------+--------------------------------------+--------------------------------------+--------------------------+--------------------------------------+---------------+\n" +
                        "|     Name    |                  Id                  |                User Id               |       Creation Date      |            Data Policy Id            | Used Capacity |\n" +
                        "+-------------+--------------------------------------+--------------------------------------+--------------------------+--------------------------------------+---------------+\n" +
                        "| jktwocopies | c5ed6a28-1499-432d-85e5-e0b2d866ec65 | c3eb82a5-574a-4a54-9083-60894866ea5f | 2016-09-22T23:09:31.000Z | c3bdbfc5-57e3-4dea-afb4-86ace65017fa | 132096040     |\n" +
                        "+-------------+--------------------------------------+--------------------------------------+--------------------------+--------------------------------------+---------------+\n\n" +
                        "+--------------------+--------+----------------+--------------------------+------------------------------------+\n" +
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

        final String detailResponse = "<Data>" +
                "<CreationDate>2016-09-22T23:09:31.000Z</CreationDate>" +
                "<DataPolicyId>c3bdbfc5-57e3-4dea-afb4-86ace65017fa</DataPolicyId>" +
                "<Id>c5ed6a28-1499-432d-85e5-e0b2d866ec65</Id>" +
                "<LastPreferredChunkSizeInBytes>28500002078</LastPreferredChunkSizeInBytes>" +
                "<LogicalUsedCapacity>132096040</LogicalUsedCapacity>" +
                "<Name>jktwocopies</Name>" +
                "<UserId>c3eb82a5-574a-4a54-9083-60894866ea5f</UserId>" +
                "</Data>\n";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetBucketResponse getBucketResponse = new GetBucketResponse(webResponse);
        when(client.getBucket(any(GetBucketRequest.class))).thenReturn(getBucketResponse);

        final WebResponse detailWebResponse = mock(WebResponse.class);
        when(detailWebResponse.getStatusCode()).thenReturn(200);
        when(detailWebResponse.getHeaders()).thenReturn(headers);
        when(detailWebResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(detailResponse));
        final GetBucketSpectraS3Response getBucketDetailResponse = new GetBucketSpectraS3Response(detailWebResponse);
        when(client.getBucketSpectraS3(any(GetBucketSpectraS3Request.class))).thenReturn(getBucketDetailResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getBucketJson() throws Exception {

        final String expected =
                "  \"Data\" : {\n" +
                        "    \"bucket\" : {\n" +
                        "      \"CreationDate\" : \"2016-11-18T15:48:08.000Z\",\n" +
                        "      \"DataPolicyId\" : \"8a5d5e56-8d54-4098-b790-6002730b3d96\",\n" +
                        "      \"Id\" : \"07cbc080-16ae-46ea-a275-ec8cb27e178c\",\n" +
                        "      \"LastPreferredChunkSizeInBytes\" : 19004340787,\n" +
                        "      \"LogicalUsedCapacity\" : 1928234,\n" +
                        "      \"Name\" : \"mountain\",\n" +
                        "      \"UserId\" : \"5079e312-bcff-43c7-bd54-d8148af0a515\"\n" +
                        "    },\n" +
                        "    \"result\" : [ {\n" +
                        "      \"etag\" : \"3d1bc5d88e795c5b23da7f812c073870\",\n" +
                        "      \"ETag\" : \"3d1bc5d88e795c5b23da7f812c073870\",\n" +
                        "      \"Key\" : \"YouDontKnowMe_295x166.jpg\",\n" +
                        "      \"LastModified\" : \"2016-11-18T15:48:10.000Z\",\n" +
                        "      \"Owner\" : {\n" +
                        "        \"DisplayName\" : \"jk\",\n" +
                        "        \"ID\" : \"5079e312-bcff-43c7-bd54-d8148af0a515\"\n" +
                        "      },\n" +
                        "      \"Size\" : 10634,\n" +
                        "      \"StorageClass\" : null\n" +
                        "    }, {\n" +
                        "      \"etag\" : \"ef751d03b7fe4fb2013be56c5a8da26e\",\n" +
                        "      \"ETag\" : \"ef751d03b7fe4fb2013be56c5a8da26e\",\n" +
                        "      \"Key\" : \"sky_bandana.jpg\",\n" +
                        "      \"LastModified\" : \"2016-11-18T15:48:10.000Z\",\n" +
                        "      \"Owner\" : {\n" +
                        "        \"DisplayName\" : \"jk\",\n" +
                        "        \"ID\" : \"5079e312-bcff-43c7-bd54-d8148af0a515\"\n" +
                        "      },\n" +
                        "      \"Size\" : 142164,\n" +
                        "      \"StorageClass\" : null\n" +
                        "    }, {\n" +
                        "      \"etag\" : \"a2750043425399804e83288f5f97d112\",\n" +
                        "      \"ETag\" : \"a2750043425399804e83288f5f97d112\",\n" +
                        "      \"Key\" : \"sky_point,web.jpg\",\n" +
                        "      \"LastModified\" : \"2016-11-18T15:48:10.000Z\",\n" +
                        "      \"Owner\" : {\n" +
                        "        \"DisplayName\" : \"jk\",\n" +
                        "        \"ID\" : \"5079e312-bcff-43c7-bd54-d8148af0a515\"\n" +
                        "      },\n" +
                        "      \"Size\" : 17120,\n" +
                        "      \"StorageClass\" : null\n" +
                        "    }, {\n" +
                        "      \"etag\" : \"71e93f1026d0362aa0b7dccedf031d8c\",\n" +
                        "      \"ETag\" : \"71e93f1026d0362aa0b7dccedf031d8c\",\n" +
                        "      \"Key\" : \"skylark,l5ct2.bmp\",\n" +
                        "      \"LastModified\" : \"2016-11-18T15:48:10.000Z\",\n" +
                        "      \"Owner\" : {\n" +
                        "        \"DisplayName\" : \"jk\",\n" +
                        "        \"ID\" : \"5079e312-bcff-43c7-bd54-d8148af0a515\"\n" +
                        "      },\n" +
                        "      \"Size\" : 921654,\n" +
                        "      \"StorageClass\" : null\n" +
                        "    }, {\n" +
                        "      \"etag\" : \"777fd3670853d4f197c52cfa6a21f773\",\n" +
                        "      \"ETag\" : \"777fd3670853d4f197c52cfa6a21f773\",\n" +
                        "      \"Key\" : \"skylark--car 001.jpg\",\n" +
                        "      \"LastModified\" : \"2016-11-18T15:48:10.000Z\",\n" +
                        "      \"Owner\" : {\n" +
                        "        \"DisplayName\" : \"jk\",\n" +
                        "        \"ID\" : \"5079e312-bcff-43c7-bd54-d8148af0a515\"\n" +
                        "      },\n" +
                        "      \"Size\" : 391998,\n" +
                        "      \"StorageClass\" : null\n" +
                        "    }, {\n" +
                        "      \"etag\" : \"6dc0c5e59418d651777c8432e13e9539\",\n" +
                        "      \"ETag\" : \"6dc0c5e59418d651777c8432e13e9539\",\n" +
                        "      \"Key\" : \"skylark004.jpg\",\n" +
                        "      \"LastModified\" : \"2016-11-18T15:48:10.000Z\",\n" +
                        "      \"Owner\" : {\n" +
                        "        \"DisplayName\" : \"jk\",\n" +
                        "        \"ID\" : \"5079e312-bcff-43c7-bd54-d8148af0a515\"\n" +
                        "      },\n" +
                        "      \"Size\" : 444664,\n" +
                        "      \"StorageClass\" : null\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"Status\" : \"OK\"\n" +
                        "}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bucket", "-b", "bucketName", "--output-format", "json"});
        final String response = "<ListBucketResult><Contents><ETag>3d1bc5d88e795c5b23da7f812c073870</ETag><Key>YouDontKnowMe_295x166.jpg</Key><LastModified>2016-11-18T15:48:10.000Z</LastModified><Owner><DisplayName>jk</DisplayName><ID>5079e312-bcff-43c7-bd54-d8148af0a515</ID></Owner><Size>10634</Size><StorageClass/></Contents><Contents><ETag>ef751d03b7fe4fb2013be56c5a8da26e</ETag><Key>sky_bandana.jpg</Key><LastModified>2016-11-18T15:48:10.000Z</LastModified><Owner><DisplayName>jk</DisplayName><ID>5079e312-bcff-43c7-bd54-d8148af0a515</ID></Owner><Size>142164</Size><StorageClass/></Contents><Contents><ETag>a2750043425399804e83288f5f97d112</ETag><Key>sky_point,web.jpg</Key><LastModified>2016-11-18T15:48:10.000Z</LastModified><Owner><DisplayName>jk</DisplayName><ID>5079e312-bcff-43c7-bd54-d8148af0a515</ID></Owner><Size>17120</Size><StorageClass/></Contents><Contents><ETag>71e93f1026d0362aa0b7dccedf031d8c</ETag><Key>skylark,l5ct2.bmp</Key><LastModified>2016-11-18T15:48:10.000Z</LastModified><Owner><DisplayName>jk</DisplayName><ID>5079e312-bcff-43c7-bd54-d8148af0a515</ID></Owner><Size>921654</Size><StorageClass/></Contents><Contents><ETag>777fd3670853d4f197c52cfa6a21f773</ETag><Key>skylark--car 001.jpg</Key><LastModified>2016-11-18T15:48:10.000Z</LastModified><Owner><DisplayName>jk</DisplayName><ID>5079e312-bcff-43c7-bd54-d8148af0a515</ID></Owner><Size>391998</Size><StorageClass/></Contents><Contents><ETag>6dc0c5e59418d651777c8432e13e9539</ETag><Key>skylark004.jpg</Key><LastModified>2016-11-18T15:48:10.000Z</LastModified><Owner><DisplayName>jk</DisplayName><ID>5079e312-bcff-43c7-bd54-d8148af0a515</ID></Owner><Size>444664</Size><StorageClass/></Contents><CreationDate>2016-11-18T15:48:08.000Z</CreationDate><Delimiter/><IsTruncated>false</IsTruncated><Marker/><MaxKeys>1000</MaxKeys><Name>mountain</Name><NextMarker/><Prefix/></ListBucketResult>";

        final String detailResponse = "<Data>" +
                "<CreationDate>2016-11-18T15:48:08.000Z</CreationDate>" +
                "<DataPolicyId>8a5d5e56-8d54-4098-b790-6002730b3d96</DataPolicyId><Id>07cbc080-16ae-46ea-a275-ec8cb27e178c</Id>" +
                "<LastPreferredChunkSizeInBytes>19004340787</LastPreferredChunkSizeInBytes>" +
                "<LogicalUsedCapacity>1928234</LogicalUsedCapacity><Name>mountain</Name>" +
                "<UserId>5079e312-bcff-43c7-bd54-d8148af0a515</UserId>" +
            "</Data>\n";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetBucketResponse getBucketResponse = new GetBucketResponse(webResponse);
        when(client.getBucket(any(GetBucketRequest.class))).thenReturn(getBucketResponse);

        final WebResponse detailWebResponse = mock(WebResponse.class);
        when(detailWebResponse.getStatusCode()).thenReturn(200);
        when(detailWebResponse.getHeaders()).thenReturn(headers);
        when(detailWebResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(detailResponse));
        final GetBucketSpectraS3Response getBucketDetailResponse = new GetBucketSpectraS3Response(detailWebResponse);
        when(client.getBucketSpectraS3(any(GetBucketSpectraS3Request.class))).thenReturn(getBucketDetailResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getBucketDetails() throws Exception {

        final String expected =
            "+-------------+--------------------------------------+--------------------------------------+--------------------------+--------------------------------------+---------------+\n" +
            "|     Name    |                  Id                  |                User Id               |       Creation Date      |            Data Policy Id            | Used Capacity |\n" +
            "+-------------+--------------------------------------+--------------------------------------+--------------------------+--------------------------------------+---------------+\n" +
            "| coffeehouse | 25df1bcd-f4a3-4ba5-9a91-1b39bcf8f1b2 | 5079e312-bcff-43c7-bd54-d8148af0a515 | 2016-08-30T21:50:25.000Z | 8a5d5e56-8d54-4098-b790-6002730b3d96 | 349642821     |\n" +
            "+-------------+--------------------------------------+--------------------------------------+--------------------------+--------------------------------------+---------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bucket_details", "-b", "coffeehouse"});
        final String detailResponse = "<Data>" +
                "<CreationDate>2016-08-30T21:50:25.000Z</CreationDate>" +
                "<DataPolicyId>8a5d5e56-8d54-4098-b790-6002730b3d96</DataPolicyId>" +
                "<Id>25df1bcd-f4a3-4ba5-9a91-1b39bcf8f1b2</Id>" +
                "<LastPreferredChunkSizeInBytes>19004340787</LastPreferredChunkSizeInBytes>" +
                "<LogicalUsedCapacity>349642821</LogicalUsedCapacity>" +
                "<Name>coffeehouse</Name>" +
                "<UserId>5079e312-bcff-43c7-bd54-d8148af0a515</UserId>" +
            "</Data>\n";

        final Ds3Client client = mock(Ds3Client.class);
        final Headers headers = mock(Headers.class);
        final WebResponse detailWebResponse = mock(WebResponse.class);
        when(detailWebResponse.getStatusCode()).thenReturn(200);
        when(detailWebResponse.getHeaders()).thenReturn(headers);
        when(detailWebResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(detailResponse));
        final GetBucketSpectraS3Response getBucketDetailResponse = new GetBucketSpectraS3Response(detailWebResponse);
        when(client.getBucketSpectraS3(any(GetBucketSpectraS3Request.class))).thenReturn(getBucketDetailResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putObject() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_object", "-b", "bucketName", "-o", "obj.txt"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        when(mockedFileSystemProvider.exists(any(Path.class))).thenReturn(true);
        when(mockedFileSystemProvider.isRegularFile(any(Path.class))).thenReturn(true);
        when(mockedFileSystemProvider.size(any(Path.class))).thenReturn(100L);
        when(helpers.startWriteJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull(), any(WriteJobOptions.class))).thenReturn(mockedPutJob);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(mockedPutJob.withMaxParallelRequests(any(int.class))).thenReturn(mockedPutJob);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
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
        when(helpers.startWriteJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull(), any(WriteJobOptions.class))).thenReturn(mockedPutJob);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(mockedPutJob.withMaxParallelRequests(any(int.class))).thenReturn(mockedPutJob);

        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        when(mockedFileSystemProvider.exists(any(Path.class))).thenReturn(true);
        when(mockedFileSystemProvider.isRegularFile(any(Path.class))).thenReturn(true);
        when(mockedFileSystemProvider.size(any(Path.class))).thenReturn(100L);

        PowerMockito.mockStatic(SyncUtils.class);
        when(SyncUtils.isSyncSupported(any(Ds3Client.class))).thenReturn(true);
        when(SyncUtils.needToSync(any(Ds3ClientHelpers.class), any(String.class), any(Path.class), any(String.class), any(Boolean.class))).thenReturn(true);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        CommandResponse result = command.render();
        assertThat(result.getMessage(), is("Success: Finished syncing file to ds3 appliance."));
        assertThat(result.getReturnCode(), is(0));

        when(SyncUtils.needToSync(any(Ds3ClientHelpers.class), any(String.class), any(Path.class), any(String.class), any(Boolean.class))).thenReturn(false);
        result= command.render();
        assertThat(result.getMessage(), is("Success: No need to sync obj.txt"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putObjectWithSyncNotSupportedVersion() throws Exception {
        final String expected = "Failed: The sync command is not supported with your version of BlackPearl.";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_object", "-b", "bucketName", "-o", "obj.txt", "--sync"});
        final Ds3Client client = mock(Ds3Client.class);

        final BuildInformation buildInformation = mock(BuildInformation.class);
        when(buildInformation.getVersion()).thenReturn("1.2.0");

        final SystemInformation systemInformation = mock(SystemInformation.class);
        when(systemInformation.getBuildInformation()).thenReturn(buildInformation);

        final GetSystemInformationSpectraS3Response systemInformationResponse = mock(GetSystemInformationSpectraS3Response.class);
        when(systemInformationResponse.getSystemInformationResult()).thenReturn(systemInformation);

        when(client.getSystemInformationSpectraS3(any(GetSystemInformationSpectraS3Request.class))).thenReturn(systemInformationResponse);

        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);

        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        when(mockedFileSystemProvider.exists(any(Path.class))).thenReturn(true);
        when(mockedFileSystemProvider.isRegularFile(any(Path.class))).thenReturn(true);
        when(mockedFileSystemProvider.size(any(Path.class))).thenReturn(100L);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
    }

    @Test
    public void putObjectJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"Success: Finished writing file to ds3 appliance.\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_object", "-b", "bucketName", "-o", "obj.txt", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        when(mockedFileSystemProvider.exists(any(Path.class))).thenReturn(true);
        when(mockedFileSystemProvider.isRegularFile(any(Path.class))).thenReturn(true);
        when(mockedFileSystemProvider.size(any(Path.class))).thenReturn(100L);
        when(helpers.startWriteJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull(), any(WriteJobOptions.class))).thenReturn(mockedPutJob);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(mockedPutJob.withMaxParallelRequests(any(int.class))).thenReturn(mockedPutJob);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getObject() throws Exception {
        final String expected = "SUCCESS: Finished downloading object.  The object was written to: ." + SterilizeString.getFileDelimiter() + "obj.txt";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull(), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getObjectWithSync() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt", "--sync"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull(), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.fileExists(any(Path.class))).thenReturn(false);

        PowerMockito.mockStatic(SyncUtils.class);
        when(SyncUtils.isSyncSupported(any(Ds3Client.class))).thenReturn(true);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        CommandResponse result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: Finished downloading object.  The object was written to: ." + SterilizeString.getFileDelimiter() + "obj.txt"));
        assertThat(result.getReturnCode(), is(0));

        when(FileUtils.fileExists(any(Path.class))).thenReturn(true);
        final Contents c1 = new Contents();
        c1.setKey("obj.txt");

        final Iterable<Contents> retCont = Lists.newArrayList(c1);
        when(helpers.listObjects(eq("bucketName"))).thenReturn(retCont);

        when(SyncUtils.needToSync(any(Ds3ClientHelpers.class), any(String.class), any(Path.class), any(String.class), any(Boolean.class))).thenReturn(true);

        result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: Finished syncing object."));
        assertThat(result.getReturnCode(), is(0));

        when(SyncUtils.needToSync(any(Ds3ClientHelpers.class), any(String.class), any(Path.class), any(String.class), any(Boolean.class))).thenReturn(false);
        result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: No need to sync obj.txt"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getObjectJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Finished downloading object.  The object was written to: ." + SterilizeString.getFileDelimiter(true) + "obj.txt\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull(), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getObjectPartial() throws Exception {
        final String expected = "SUCCESS: Finished downloading object.  The object was written to: ." + SterilizeString.getFileDelimiter() + "obj.txt";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "--range-offset", "0", "--range-length", "100", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        when(helpers.startReadJob(eq("bucketName"), (Iterable<Ds3Object>) isNotNull(), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test(expected = MissingOptionException.class)
    public void getObjectPartialBadArgs() throws Exception {
        final String expected = "Error (MissingArgumentException): Partial recovery must provide values for both range-start and range-length";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "--range-offset", "0", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_object", "-b", "bucketName", "-o", "obj.txt"});

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, null), null);
        command.init(args);
    }

    @Test
    public void modifyJob() throws Exception {
        final String jobId = "081bbb4f-fb42-4871-b3af-d5180f0c6569";
        final String jobName = "Good Job";
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "modify_job", "-i", jobId, "--priority", "HIGH",  "--job-name", jobName});

        final String expected = "JobId: " + jobId + " | Name: " + jobName + " | Status: IN_PROGRESS | Bucket: coffeehouse | Type: PUT | Priority: HIGH | User Name: jk | Creation Date: 2016-12-01T18:51:09.000Z | Total Size: 343479386 | Total Transferred: 0";
        final String response = "<MasterObjectList Aggregating=\"false\" BucketName=\"coffeehouse\" CachedSizeInBytes=\"343479386\" ChunkClientProcessingOrderGuarantee=\"IN_ORDER\" CompletedSizeInBytes=\"0\" EntirelyInCache=\"true\" " +
                "JobId=\"" + jobId + "\" Naked=\"false\" Name=\"" + jobName + "\" OriginalSizeInBytes=\"343479386\" Priority=\"HIGH\" RequestType=\"PUT\" StartDate=\"2016-12-01T18:51:09.000Z\" Status=\"IN_PROGRESS\" UserId=\"c1fdd654-5e00-4adf-a5d5-bafeba1bb237\" UserName=\"jk\"></MasterObjectList>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final ModifyJobSpectraS3Response modifyJobResponse = new ModifyJobSpectraS3Response(webResponse);
        when(client.modifyJobSpectraS3(any(ModifyJobSpectraS3Request.class))).thenReturn(modifyJobResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getCompletedJob() throws Exception {
        final String jobId = "aa5df0cc-b03a-4cb9-b69d-56e7367e917f";
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "get_job", "-i", jobId});

        final String expected = "JobId: " + jobId + " | Name: Good Job | Status: COMPLETED | Bucket: bucket | Type: GET | Priority: HIGH | User Name: spectra | Creation Date: 2015-09-28T17:30:43.000Z | Total Size: 32 | Total Transferred: 0";
        final String response = "<MasterObjectList BucketName=\"bucket\" CachedSizeInBytes=\"0\" ChunkClientProcessingOrderGuarantee=\"NONE\" CompletedSizeInBytes=\"0\" JobId=\"aa5df0cc-b03a-4cb9-b69d-56e7367e917f\" OriginalSizeInBytes=\"32\" Priority=\"HIGH\" RequestType=\"GET\" StartDate=\"2015-09-28T17:30:43.000Z\" Status=\"COMPLETED\" UserId=\"c2581493-058c-40d7-a3a1-9a50b20d6d3b\" UserName=\"spectra\" WriteOptimization=\"CAPACITY\" Name=\"Good Job\"></MasterObjectList>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetJobSpectraS3Response getJobResponse = new GetJobSpectraS3Response(webResponse);
        when(client.getJobSpectraS3(any(GetJobSpectraS3Request.class))).thenReturn(getJobResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getCompletedJobJson() throws Exception {
        final String jobId = "aa5df0cc-b03a-4cb9-b69d-56e7367e917f";
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "get_job", "-i", jobId, "--output-format", "json"});

        final String expected = "\"Data\" : {\n" +
                "    \"result\" : {\n" +
                "      \"aggregating\" : false,\n" +
                "      \"bucketName\" : \"bucket\",\n" +
                "      \"cachedSizeInBytes\" : 0,\n" +
                "      \"chunkClientProcessingOrderGuarantee\" : \"NONE\",\n" +
                "      \"completedSizeInBytes\" : 0,\n" +
                "      \"entirelyInCache\" : false,\n" +
                "      \"jobId\" : \"aa5df0cc-b03a-4cb9-b69d-56e7367e917f\",\n" +
                "      \"naked\" : false,\n" +
                "      \"name\" : null,\n" +
                "      \"originalSizeInBytes\" : 32,\n" +
                "      \"priority\" : \"HIGH\",\n" +
                "      \"requestType\" : \"GET\",\n" +
                "      \"startDate\" : \"2015-09-28T17:30:43.000Z\",\n" +
                "      \"status\" : \"COMPLETED\",\n" +
                "      \"userId\" : \"c2581493-058c-40d7-a3a1-9a50b20d6d3b\",\n" +
                "      \"userName\" : \"spectra\",\n" +
                "      \"Nodes\" : [ ],\n" +
                "      \"Objects\" : [ ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"Status\" : \"OK\"\n" +
                "}";

        final String response = "<MasterObjectList BucketName=\"bucket\" CachedSizeInBytes=\"0\" ChunkClientProcessingOrderGuarantee=\"NONE\" CompletedSizeInBytes=\"0\" JobId=\"aa5df0cc-b03a-4cb9-b69d-56e7367e917f\" OriginalSizeInBytes=\"32\" Priority=\"HIGH\" RequestType=\"GET\" StartDate=\"2015-09-28T17:30:43.000Z\" Status=\"COMPLETED\" UserId=\"c2581493-058c-40d7-a3a1-9a50b20d6d3b\" UserName=\"spectra\" WriteOptimization=\"CAPACITY\"></MasterObjectList>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetJobSpectraS3Response getJobResponse = new GetJobSpectraS3Response(webResponse);
        when(client.getJobSpectraS3(any(GetJobSpectraS3Request.class))).thenReturn(getJobResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), StringEndsWith.endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getBulk() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bulk", "-b", "bucketName"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);

        when(helpers.startReadAllJob(eq("bucketName"), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all the objects from bucketName to directory ."));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test(expected = CommandException.class)
    public void getBulkWithBadArgs() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bulk", "-b", "bucketName", "-d", "targetdir", "--discard"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
    }

    @Test
    public void getBulkJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Wrote all the objects from bucketName to directory .\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_bulk", "-b", "bucketName", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);

        when(helpers.startReadAllJob(eq("bucketName"), any(ReadJobOptions.class))).thenReturn(mockedGetJob);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putBulk() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);

        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(FileUtils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(FileUtils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(FileUtils.getFileSize(eq(p1))).thenReturn(1245L);
        when(FileUtils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(FileUtils.getFileSize(eq(p2))).thenReturn(12345L);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all the files in dir to bucket bucketName"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putBulkWithIgnoreConflicts() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!",
                "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--ignore-naming-conflicts"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.of(p1, p2);

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = ImmutableList.of(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);

        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(FileUtils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(FileUtils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(FileUtils.getFileSize(eq(p1))).thenReturn(1245L);
        when(FileUtils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(FileUtils.getFileSize(eq(p2))).thenReturn(12345L);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all the files in dir to bucket bucketName"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putBulkWithSync() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--sync"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
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

        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(FileUtils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(FileUtils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(FileUtils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(SyncUtils.isNewFile(any(Path.class), any(Contents.class), any(Boolean.class))).thenReturn(false);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        CommandResponse result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: All files are up to date"));
        assertThat(result.getReturnCode(), is(0));

        when(SyncUtils.isNewFile(any(Path.class), any(Contents.class), any(Boolean.class))).thenReturn(true);
        when(FileUtils.getFileSize(any(Path.class))).thenReturn(1245L);
        result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all the files in dir to bucket bucketName"));
        assertThat(result.getReturnCode(), is(0));

    }

    @Test(expected = SyncNotSupportedException.class)
    public void putBulkWithSyncWrongVersion() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--sync"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedGetJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(client, helpers), mockedFileSystemProvider);
        command.init(args);
        command.render();
    }

    @Test(expected = BadArgumentException.class)
    public void putBulkMissingDirectory() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
    }

    @Test
    public void putBulkJson() throws Exception {
        final String expected = "\"Status\" : \"OK\",\n  \"Message\" : \"SUCCESS: Wrote all the files in dir to bucket bucketName\"\n}";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--output-format", "json"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(FileUtils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(FileUtils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(FileUtils.getFileSize(eq(p1))).thenReturn(1245L);
        when(FileUtils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(FileUtils.getFileSize(eq(p2))).thenReturn(12345L);

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void isVersionSupported() throws CommandException, IOException {
        final Ds3Client client = mock(Ds3Client.class);
        final GetSystemInformationSpectraS3Response systemInformationResponse = mock(GetSystemInformationSpectraS3Response.class);
        final SystemInformation systemInformation = mock(SystemInformation.class);
        final BuildInformation buildInformation = mock(BuildInformation.class);

        when(systemInformation.getBuildInformation()).thenReturn(buildInformation);
        when(systemInformationResponse.getSystemInformationResult()).thenReturn(systemInformation);
        when(client.getSystemInformationSpectraS3(any(GetSystemInformationSpectraS3Request.class))).thenReturn(systemInformationResponse);

        when(buildInformation.getVersion()).thenReturn("1.2.0");
        assertTrue(CliUtils.isVersionSupported(client));

        when(buildInformation.getVersion()).thenReturn("3.0.0");
        assertTrue(CliUtils.isVersionSupported(client));

        when(buildInformation.getVersion()).thenReturn("1.1.0");
        assertFalse(CliUtils.isVersionSupported(client));
    }

    @Test
    public void isCustomVersionSupported() throws CommandException, IOException {
        final Ds3Client client = mock(Ds3Client.class);
        final GetSystemInformationSpectraS3Response systemInformationResponse = mock(GetSystemInformationSpectraS3Response.class);
        final SystemInformation systemInformation = mock(SystemInformation.class);
        final BuildInformation buildInformation = mock(BuildInformation.class);

        when(systemInformation.getBuildInformation()).thenReturn(buildInformation);
        when(systemInformationResponse.getSystemInformationResult()).thenReturn(systemInformation);
        when(client.getSystemInformationSpectraS3(any(GetSystemInformationSpectraS3Request.class))).thenReturn(systemInformationResponse);

        when(buildInformation.getVersion()).thenReturn("3.2.3");
        assertTrue(CliUtils.isVersionSupported(client, "3.2.3"));

        when(buildInformation.getVersion()).thenReturn("3.2.2");
        assertFalse(CliUtils.isVersionSupported(client, "3.2.3"));

        when(buildInformation.getVersion()).thenReturn("3.2.4");
        assertTrue(CliUtils.isVersionSupported(client, "3.2.3"));
    }

    @Test
    public void putBulkWithIgnoreErrors() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir", "--ignore-errors"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);

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

        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(Guard.class);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(FileUtils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(FileUtils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(FileUtils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(FileUtils.getFileSize(eq(p1))).thenReturn(1245L);
        when(FileUtils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(FileUtils.getFileSize(eq(p2))).thenReturn(12345L);
        when(Guard.nullGuard(any(String.class))).thenCallRealMethod();

        final IOException ex = new IOException("java.nio.file.NoSuchFileException: obj3.txt");
        when(FileUtils.getFileSize(eq(p3))).thenThrow(ex);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
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
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);

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

        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(CliUtils.class);
        PowerMockito.mockStatic(Guard.class);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(FileUtils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(FileUtils.listObjectsForDirectory(any(Path.class))).thenReturn(retPath);
        when(FileUtils.getFileName(any(Path.class), eq(p1))).thenReturn("obj1.txt");
        when(FileUtils.getFileSize(eq(p1))).thenReturn(1245L);
        when(FileUtils.getFileName(any(Path.class), eq(p2))).thenReturn("obj2.txt");
        when(FileUtils.getFileSize(eq(p2))).thenReturn(12345L);
        when(Guard.nullGuard(any(String.class))).thenCallRealMethod();

        final IOException ex = new IOException("java.nio.file.NoSuchFileException: obj3.txt");
        when(FileUtils.getFileSize(eq(p3))).thenThrow(ex);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();

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
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        PowerMockito.mockStatic(CliUtils.class);
        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.normalizeObjectName(any(String.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                return (String) args[0];
            }
        });
        when(CliUtils.isPipe()).thenReturn(true);
        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(FileUtils.getPipedFilesFromStdin(any(FileSystemProvider.class))).thenReturn(retPath);
        when(FileUtils.getObjectsToPut((Iterable<Path>) isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(FileUtils.getFileName(any(Path.class), eq(p1))).thenReturn(p1.toString());
        when(FileUtils.getFileSize(eq(p1))).thenReturn(1245L);
        when(FileUtils.getFileName(any(Path.class), eq(p2))).thenReturn(p2.toString());
        when(FileUtils.getFileSize(eq(p2))).thenReturn(12345L);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all piped files to bucket bucketName"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void putBulkWithPipeAndSync() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "--sync"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final Ds3ClientHelpers.Job mockedPutJob = mock(Ds3ClientHelpers.Job.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);

        final UUID jobId = UUID.randomUUID();
        when(mockedPutJob.getJobId()).thenReturn(jobId);
        final Iterable<Ds3Object> retObj = Lists.newArrayList(new Ds3Object("obj1.txt", 1245), new Ds3Object("obj2.txt", 12345));
        when(helpers.startWriteJob(eq("bucketName"), eq(retObj), any(WriteJobOptions.class))).thenReturn(mockedPutJob);

        final Iterable<Contents> retCont = Lists.newArrayList();
        when(helpers.listObjects(eq("bucketName"), any(String.class))).thenReturn(retCont);

        final Path p1 = Paths.get("obj1.txt");
        final Path p2 = Paths.get("obj2.txt");
        final ImmutableList<Path> retPath = ImmutableList.copyOf(Lists.newArrayList(p1, p2));

        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(CliUtils.class);
        when(FileUtils.normalizeObjectName(any(String.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                return (String) args[0];
            }
        });

        when(mockedPutJob.withMetadata((Ds3ClientHelpers.MetadataAccess) isNotNull())).thenReturn(mockedPutJob);
        when(CliUtils.isPipe()).thenReturn(true);
        when(FileUtils.getPipedFilesFromStdin(any(FileSystemProvider.class))).thenReturn(retPath);
        when(FileUtils.getObjectsToPut((Iterable<Path>)isNotNull(), any(Path.class), any(Boolean.class))).thenCallRealMethod();
        when(FileUtils.getFileName(any(Path.class), eq(p1))).thenReturn(p1.toString());
        when(FileUtils.getFileSize(eq(p1))).thenReturn(1245L);
        when(FileUtils.getFileName(any(Path.class), eq(p2))).thenReturn(p2.toString());
        when(FileUtils.getFileSize(eq(p2))).thenReturn(12345L);

        PowerMockito.mockStatic(SyncUtils.class);
        when(SyncUtils.isSyncSupported(any(Ds3Client.class))).thenReturn(true);


        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is("SUCCESS: Wrote all piped files to bucket bucketName"));
        assertThat(result.getReturnCode(), is(0));

        final Contents c1 = new Contents();
        c1.setKey("obj1.txt");
        final Contents c2 = new Contents();
        c2.setKey("obj2.txt");

        final Iterable<Contents> retCont2 = Lists.newArrayList(c1, c2);
        when(helpers.listObjects(eq("bucketName"), any(String.class))).thenReturn(retCont2);
        final CommandResponse result2 = command.render();
        assertThat(result2.getMessage(), is("SUCCESS: All files are up to date"));
        assertThat(result2.getReturnCode(), is(0));
    }

    @Test
    public void putBulkWithPipeAndOtherArgs() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-d", "dir"});
        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);
        final FileSystemProvider mockedFileSystemProvider = mock(FileSystemProvider.class);

        PowerMockito.mockStatic(CliUtils.class);
        when(CliUtils.isPipe()).thenReturn(true);

        try {
            final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
            command.init(args);
            final CommandResponse result = command.render();
        } catch (final BadArgumentException ex) {
            assertEquals("-d, -o and -p arguments are not supported when using piped input", ex.getMessage());
        } catch (final Exception ex) {
            fail(); //This is the wrong exception
        }

        final Arguments args2 = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-o", "obj"});
        try {
            final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
            command.init(args2);
            final CommandResponse result = command.render();
        } catch (final UnrecognizedOptionException ex) {
            assertEquals("Unrecognized option: -o", ex.getMessage());
        } catch (final Exception ex) {
            fail(); //This is the wrong exception
        }

        final Arguments args3 = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "put_bulk", "-b", "bucketName", "-p", "prefix"});
        try {
            final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(null, helpers), mockedFileSystemProvider);
            command.init(args3);
            final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();

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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();

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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
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
                "--modify-params",  "name:fred,blobbing_enabled:false,default_blob_size:1073741824,default_get_job_priority:HIGH,end_to_end_crc_required:false,rebuild_priority:LOW,versioning:NONE"});
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

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void modifyDataPolicyWithBadParam() throws Exception {

        final String expected = "Error (CommandException): Unrecognized Data Policy parameter: cat";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "modify_data_policy", "-i", "fake",
                "--modify-params",  "name:fred,blobbing_enabled:false,default_blob_size:1073741824,default_get_job_priority:HIGH,end_to_end_crc_required:false,rebuild_priority:LOW,versioning:NONE,cat:dog"});

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

        // mock client for "modify" call
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));

        final ModifyDataPolicySpectraS3Response ModifyDataPolicyResponse = new ModifyDataPolicySpectraS3Response(webResponse);
        when(client.modifyDataPolicySpectraS3(any(ModifyDataPolicySpectraS3Request.class))).thenReturn(ModifyDataPolicyResponse);
        when(client.getDataPolicySpectraS3(any(GetDataPolicySpectraS3Request.class))).thenReturn(GetDataPolicyResponse);

        try {
            final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
            command.init(args);
            final CommandResponse result = command.render();
        } catch (final CommandException e) {
            // exception is expected -- test handler / formatter
            final String formattedException = ExceptionFormatter.format(e);
            assertThat(formattedException, is(expected));
        }
    }

    @Test
    public void getUser() throws Exception {

        final String expected = "+---------+------------+--------------------------------------+--------------------------------------+------------------+\n" +
                "|   Name  | Secret Key |                  Id                  |        Default Data Policy Id        | Authorization Id |\n" +
                "+---------+------------+--------------------------------------+--------------------------------------+------------------+\n" +
                "| testguy | QBVe7jAu   | a1e149b9-3dfa-49c2-b7d0-25e831932fff | a85aa599-7a58-4141-adbe-79bfd1d42e48 |     dGVzdGd1eQ== |\n" +
                "+---------+------------+--------------------------------------+--------------------------------------+------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_user", "-i", "testguy"});
        final String response = "<Data>" +
                "<AuthId>dGVzdGd1eQ==</AuthId>" +
                "<DefaultDataPolicyId>a85aa599-7a58-4141-adbe-79bfd1d42e48</DefaultDataPolicyId>" +
                "<Id>a1e149b9-3dfa-49c2-b7d0-25e831932fff</Id>" +
                "<Name>testguy</Name>" +
                "<SecretKey>QBVe7jAu</SecretKey>" +
                "</Data>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetUserSpectraS3Response GetUserResponse = new GetUserSpectraS3Response(webResponse);
        when(client.getUserSpectraS3(any(GetUserSpectraS3Request.class))).thenReturn(GetUserResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getUsers() throws Exception {

        final String expected = "+---------+------------+--------------------------------------+--------------------------------------+------------------+\n" +
                "|   Name  | Secret Key |                  Id                  |        Default Data Policy Id        | Authorization Id |\n" +
                "+---------+------------+--------------------------------------+--------------------------------------+------------------+\n" +
                "| jk      | QRfhLkgU   | 0f4e6e4a-bc48-427d-820e-9c0a050064be | d3e6e795-fc85-4163-9d2f-4bc271d995d0 |             ams= |\n" +
                "| spectra | L28VgwAr   | dcea9717-4326-49bb-bc46-7150b1c515bd | d3e6e795-fc85-4163-9d2f-4bc271d995d0 |     c3BlY3RyYQ== |\n" +
                "| testguy | QBVe7jAu   | a1e149b9-3dfa-49c2-b7d0-25e831932fff | a85aa599-7a58-4141-adbe-79bfd1d42e48 |     dGVzdGd1eQ== |\n" +
                "+---------+------------+--------------------------------------+--------------------------------------+------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_users"});
        final String response = "<Data>" +
                "<User><AuthId>ams=</AuthId>" +
                    "<DefaultDataPolicyId>d3e6e795-fc85-4163-9d2f-4bc271d995d0</DefaultDataPolicyId>" +
                    "<Id>0f4e6e4a-bc48-427d-820e-9c0a050064be</Id>" +
                    "<Name>jk</Name>" +
                    "<SecretKey>QRfhLkgU</SecretKey></User>" +
                "<User><AuthId>c3BlY3RyYQ==</AuthId>" +
                    "<DefaultDataPolicyId>d3e6e795-fc85-4163-9d2f-4bc271d995d0</DefaultDataPolicyId>" +
                    "<Id>dcea9717-4326-49bb-bc46-7150b1c515bd</Id>" +
                    "<Name>spectra</Name>" +
                    "<SecretKey>L28VgwAr</SecretKey></User>" +
                "<User><AuthId>dGVzdGd1eQ==</AuthId>" +
                    "<DefaultDataPolicyId>a85aa599-7a58-4141-adbe-79bfd1d42e48</DefaultDataPolicyId>" +
                    "<Id>a1e149b9-3dfa-49c2-b7d0-25e831932fff</Id>" +
                    "<Name>testguy</Name>" +
                    "<SecretKey>QBVe7jAu</SecretKey></User>" +
                "</Data>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetUsersSpectraS3Response GetUsersResponse = new GetUsersSpectraS3Response(webResponse);
        when(client.getUsersSpectraS3(any(GetUsersSpectraS3Request.class))).thenReturn(GetUsersResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void modifyUser() throws Exception {

        final String expected = "+---------+------------+--------------------------------------+--------------------------------------+------------------+\n" +
                "|   Name  | Secret Key |                  Id                  |        Default Data Policy Id        | Authorization Id |\n" +
                "+---------+------------+--------------------------------------+--------------------------------------+------------------+\n" +
                "| testguy | QBVe7jAu   | a1e149b9-3dfa-49c2-b7d0-25e831932fff | a85aa599-7a58-4141-adbe-79bfd1d42e48 |     dGVzdGd1eQ== |\n" +
                "+---------+------------+--------------------------------------+--------------------------------------+------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "modify_user",
                "-i", "testguy", "--modify-params",  "default_data_policy_id:a85aa599-7a58-4141-adbe-79bfd1d42e48"});
        final String response = "<Data>" +
                "<AuthId>dGVzdGd1eQ==</AuthId>" +
                "<DefaultDataPolicyId>a85aa599-7a58-4141-adbe-79bfd1d42e48</DefaultDataPolicyId>" +
                "<Id>a1e149b9-3dfa-49c2-b7d0-25e831932fff</Id>" +
                "<Name>testguy</Name>" +
                "<SecretKey>QBVe7jAu</SecretKey>" +
                "</Data>";

        final Ds3Client client = mock(Ds3Client.class);

        // mock client for "modify" call
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));

        final ModifyUserSpectraS3Response ModifyUserResponse = new ModifyUserSpectraS3Response(webResponse);
        when(client.modifyUserSpectraS3(any(ModifyUserSpectraS3Request.class))).thenReturn(ModifyUserResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test(expected = FailedRequestException.class)
    public void getUserNonExisting() throws Exception {

        final String expected = "Unknown user: nosuchuser";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_user", "-i", "nosuchuser"});

        final String response = "<Error>" +
                "<Code>NotFound</Code>" +
                "<HttpErrorCode>404</HttpErrorCode>" +
                "<Message>interface com.spectralogic.s3.common.dao.domain.ds3.User not found via identifier / bean property value &apos;nosuchuser&apos;.</Message>" +
                "<Resource>/_rest_/user/nosuchuser</Resource><ResourceId>578</ResourceId>" +
                "</Error>";

        final Ds3Client client = mock(Ds3Client.class);

        // mock client for "get" call0
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(404);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));

        final GetUserSpectraS3Response GetUserResponse = new GetUserSpectraS3Response(webResponse);
        when(client.getUserSpectraS3(any(GetUserSpectraS3Request.class))).thenReturn(GetUserResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
    }

    @Test
    public void getBlobsOnTape() throws Exception {

        final String expected = "+-------------------------------------------------+-------------+-----------+--------------------------------------+\n" +
                "|                       Name                      |    Bucket   |    Size   |                  Id                  |\n" +
                "+-------------------------------------------------+-------------+-----------+--------------------------------------+\n" +
                "| 123456789.txt                                   | coffeehouse |         9 | 53452a07-699a-4c27-8de5-95aa0a431df1 |\n" +
                "| Always_295x166.jpg                              | coffeehouse |      9172 | 7989ad4a-47a5-41ac-8814-3746e4e20679 |\n" +
                "| Chapter 9.docx                                  | coffeehouse |     29895 | 6649c2cb-6e83-4c58-9fb8-9b4aec8b014b |\n" +
                "| RedRiverValley_295x166.jpg                      | coffeehouse |      9114 | f725ef08-7e6f-4fe0-a256-798e561d878f |\n" +
                "| Softphone Install.docx                          | coffeehouse |    774741 | dff0cbed-5b7f-480f-aa94-8adea7c59a3e |\n" +
                "| ThinkingOutLoud_295x166.jpg                     | coffeehouse |     11059 | ffd8266d-cdc5-4e49-81d4-d08314fcee5a |\n" +
                "| UnforgetWonderful_295x166.jpg                   | coffeehouse |     10724 | 897b7e5b-59d8-4645-bc7a-f5c4b8154a0f |\n" +
                "| YouDontKnowMe_295x166.jpg                       | coffeehouse |     10634 | 7bb970d3-113f-413b-87d5-00b072059451 |\n" +
                "| beowulf.txt                                     | coffeehouse |    294056 | 1e293dc9-3257-4277-9c40-b50a6e63b71e |\n" +
                "| coffeehouse/im_in_the_mood.mp3                  | coffeehouse |   3309717 | d759f10d-05c6-498c-b4ce-2475027fbeae |\n" +
                "| coffeehouse/jk/ColumbinesGrow.m4a               | coffeehouse |  45872985 | d9b342ae-311c-4cbc-a000-75686c174471 |\n" +
                "| coffeehouse/jk/ColumbinesGrow.mp3               | coffeehouse |   5050747 | c85fc175-116a-4bcf-a77a-5ea240a5de3a |\n" +
                "| coffeehouse/jk/Columbines_295x166.jpg           | coffeehouse |     10528 | b70bd4ab-90d2-41fd-83d2-572fb3d1c8ca |\n" +
                "| coffeehouse/jk/Misty_2015.m4a                   | coffeehouse |  10396369 | e4769cd2-3aa6-4628-887c-ad51768656c5 |\n" +
                "| coffeehouse/jk/RedRiverValley.m4a               | coffeehouse |  77080710 | 9ffa7e9c-6939-4808-996e-e42fcf8bacb5 |\n" +
                "| coffeehouse/jk/RedRiverValley.mp3               | coffeehouse |   6363965 | 564a1bc1-33a0-41f3-af28-fbf79f331d0e |\n" +
                "| coffeehouse/jk/UnforgetWonderful_295x166.jpg    | coffeehouse |     10724 | b2671db7-1a4a-4577-8419-f17ead63d321 |\n" +
                "| coffeehouse/jk/Unforgettable-WonderfulWorld.m4a | coffeehouse | 110054089 | 71807ee9-2db9-4145-b01d-3d2aaae37061 |\n" +
                "| coffeehouse/jk/Unforgettable-WonderfulWorld.mp3 | coffeehouse |   7520930 | e50d5fc8-8fbf-4206-b495-05bb8be539ec |\n" +
                "| coffeehouse/jk/WhereOrWhen.m4a                  | coffeehouse |  51272203 | 9156aab6-88fa-49b0-a0e1-c230d247957e |\n" +
                "| coffeehouse/jk/WhereOrWhen.mp3                  | coffeehouse |   5647581 | 0f5541b9-8c4d-4ed8-bd1d-9e62173bdf4a |\n" +
                "| coffeehouse/jk/WhereOrWhen_295x166.jpg          | coffeehouse |     11263 | 03b2e1c7-f80c-437a-912d-b09015dba484 |\n" +
                "| coffeehouse/jk/im_in_the_mood.m4a               | coffeehouse |  11207247 | 667d94f6-b341-45f7-bd91-706af52d8e77 |\n" +
                "| coffeehouse/jk/im_in_the_mood_200.jpg           | coffeehouse |      8621 | f7f65e20-4ea2-4629-9c22-ddf9cbc76b99 |\n" +
                "| coffeehouse/witchcraft.mp3                      | coffeehouse |   6409093 | 92a40cff-63a6-4520-81a9-80afa03a1973 |\n" +
                "+-------------------------------------------------+-------------+-----------+--------------------------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_objects_on_tape", "-i", "7badec16-d6f2-4912-a120-dcfe9a6b4c3c"});
        final String response = "<Data>" +
                "<Object Bucket=\"coffeehouse\" Id=\"53452a07-699a-4c27-8de5-95aa0a431df1\" Latest=\"true\" Length=\"9\" Name=\"123456789.txt\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"7989ad4a-47a5-41ac-8814-3746e4e20679\" Latest=\"true\" Length=\"9172\" Name=\"Always_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"6649c2cb-6e83-4c58-9fb8-9b4aec8b014b\" Latest=\"true\" Length=\"29895\" Name=\"Chapter 9.docx\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"f725ef08-7e6f-4fe0-a256-798e561d878f\" Latest=\"true\" Length=\"9114\" Name=\"RedRiverValley_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"dff0cbed-5b7f-480f-aa94-8adea7c59a3e\" Latest=\"true\" Length=\"774741\" Name=\"Softphone Install.docx\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"ffd8266d-cdc5-4e49-81d4-d08314fcee5a\" Latest=\"true\" Length=\"11059\" Name=\"ThinkingOutLoud_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"897b7e5b-59d8-4645-bc7a-f5c4b8154a0f\" Latest=\"true\" Length=\"10724\" Name=\"UnforgetWonderful_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"7bb970d3-113f-413b-87d5-00b072059451\" Latest=\"true\" Length=\"10634\" Name=\"YouDontKnowMe_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"1e293dc9-3257-4277-9c40-b50a6e63b71e\" Latest=\"true\" Length=\"294056\" Name=\"beowulf.txt\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"d759f10d-05c6-498c-b4ce-2475027fbeae\" Latest=\"true\" Length=\"3309717\" Name=\"coffeehouse/im_in_the_mood.mp3\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"d9b342ae-311c-4cbc-a000-75686c174471\" Latest=\"true\" Length=\"45872985\" Name=\"coffeehouse/jk/ColumbinesGrow.m4a\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"c85fc175-116a-4bcf-a77a-5ea240a5de3a\" Latest=\"true\" Length=\"5050747\" Name=\"coffeehouse/jk/ColumbinesGrow.mp3\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"b70bd4ab-90d2-41fd-83d2-572fb3d1c8ca\" Latest=\"true\" Length=\"10528\" Name=\"coffeehouse/jk/Columbines_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"e4769cd2-3aa6-4628-887c-ad51768656c5\" Latest=\"true\" Length=\"10396369\" Name=\"coffeehouse/jk/Misty_2015.m4a\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"9ffa7e9c-6939-4808-996e-e42fcf8bacb5\" Latest=\"true\" Length=\"77080710\" Name=\"coffeehouse/jk/RedRiverValley.m4a\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"564a1bc1-33a0-41f3-af28-fbf79f331d0e\" Latest=\"true\" Length=\"6363965\" Name=\"coffeehouse/jk/RedRiverValley.mp3\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"b2671db7-1a4a-4577-8419-f17ead63d321\" Latest=\"true\" Length=\"10724\" Name=\"coffeehouse/jk/UnforgetWonderful_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"71807ee9-2db9-4145-b01d-3d2aaae37061\" Latest=\"true\" Length=\"110054089\" Name=\"coffeehouse/jk/Unforgettable-WonderfulWorld.m4a\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"e50d5fc8-8fbf-4206-b495-05bb8be539ec\" Latest=\"true\" Length=\"7520930\" Name=\"coffeehouse/jk/Unforgettable-WonderfulWorld.mp3\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"9156aab6-88fa-49b0-a0e1-c230d247957e\" Latest=\"true\" Length=\"51272203\" Name=\"coffeehouse/jk/WhereOrWhen.m4a\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"0f5541b9-8c4d-4ed8-bd1d-9e62173bdf4a\" Latest=\"true\" Length=\"5647581\" Name=\"coffeehouse/jk/WhereOrWhen.mp3\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"03b2e1c7-f80c-437a-912d-b09015dba484\" Latest=\"true\" Length=\"11263\" Name=\"coffeehouse/jk/WhereOrWhen_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"667d94f6-b341-45f7-bd91-706af52d8e77\" Latest=\"true\" Length=\"11207247\" Name=\"coffeehouse/jk/im_in_the_mood.m4a\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"f7f65e20-4ea2-4629-9c22-ddf9cbc76b99\" Latest=\"true\" Length=\"8621\" Name=\"coffeehouse/jk/im_in_the_mood_200.jpg\" Offset=\"0\" Version=\"1\"/>" +
                "<Object Bucket=\"coffeehouse\" Id=\"92a40cff-63a6-4520-81a9-80afa03a1973\" Latest=\"true\" Length=\"6409093\" Name=\"coffeehouse/witchcraft.mp3\" Offset=\"0\" Version=\"1\"/>" +
                "</Data>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetBlobsOnTapeSpectraS3Response blobsResponse = new GetBlobsOnTapeSpectraS3Response(webResponse);
        when(client.getBlobsOnTapeSpectraS3(any(GetBlobsOnTapeSpectraS3Request.class))).thenReturn(blobsResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getBlobsOnTapeEmptyTape() throws Exception {

        final String expected = "No objects were reported in tape 'a4d7cef1-80fa-4552-ad3f-4de716f515ea'";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "ccess", "-c", "get_objects_on_tape", "-i", "a4d7cef1-80fa-4552-ad3f-4de716f515ea"});
        final String response = "<Data></Data>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetBlobsOnTapeSpectraS3Response blobsResponse = new GetBlobsOnTapeSpectraS3Response(webResponse);
        when(client.getBlobsOnTapeSpectraS3(any(GetBlobsOnTapeSpectraS3Request.class))).thenReturn(blobsResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test(expected = FailedRequestException.class)
    public void getBlobsOnTapeMissingTape() throws Exception {

        final String expected = "Unknown tape: b4d7cef1-80fa-4552-ad3f-4de716f515ea";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_objects_on_tape", "-i", "b4d7cef1-80fa-4552-ad3f-4de716f515ea"});
        final String response = "<Error>" +
                "<Code>NotFound</Code>" +
                "<HttpErrorCode>404</HttpErrorCode>" +
                "<Message>NotFound[404]: interface com.spectralogic.s3.common.dao.domain.tape.Tape not found via identifier / bean property value &apos;b4d7cef1-80fa-4552-ad3f-4de716f515ea&apos;.</Message>" +
                "<Resource>/_rest_/tape/b4d7cef1-80fa-4552-ad3f-4de716f515ea</Resource>" +
                "<ResourceId>1984</ResourceId>" +
                "</Error>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(404);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final GetBlobsOnTapeSpectraS3Response blobsResponse = new GetBlobsOnTapeSpectraS3Response(webResponse);
        when(client.getBlobsOnTapeSpectraS3(any(GetBlobsOnTapeSpectraS3Request.class))).thenReturn(blobsResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

   @Test
    public void verifyBulkJob() throws Exception {

       final String expected =
                        "+-------+-------------------------------------------------+-----------+---------+\n" +
                        "| Chunk |                       Name                      |    Size   | Version |\n" +
                        "+-------+-------------------------------------------------+-----------+---------+\n" +
                        "| 0     | 123456789.txt                                   |         9 |       1 |\n" +
                        "| 0     | Always_295x166.jpg                              |      9172 |       1 |\n" +
                        "| 0     | Chapter 9.docx                                  |     29895 |       1 |\n" +
                        "| 0     | RedRiverValley_295x166.jpg                      |      9114 |       1 |\n" +
                        "| 0     | Softphone Install.docx                          |    774741 |       1 |\n" +
                        "| 0     | ThinkingOutLoud_295x166.jpg                     |     11059 |       1 |\n" +
                        "| 0     | UnforgetWonderful_295x166.jpg                   |     10724 |       1 |\n" +
                        "| 0     | YouDontKnowMe_295x166.jpg                       |     10634 |       1 |\n" +
                        "| 0     | beowulf.txt                                     |    294056 |       1 |\n" +
                        "| 0     | coffeehouse/im_in_the_mood.mp3                  |   3309717 |       1 |\n" +
                        "| 0     | coffeehouse/jk/ColumbinesGrow.m4a               |  45872985 |       1 |\n" +
                        "| 0     | coffeehouse/jk/ColumbinesGrow.mp3               |   5050747 |       1 |\n" +
                        "| 0     | coffeehouse/jk/Columbines_295x166.jpg           |     10528 |       1 |\n" +
                        "| 0     | coffeehouse/jk/Misty_2015.m4a                   |  10396369 |       1 |\n" +
                        "| 0     | coffeehouse/jk/RedRiverValley.m4a               |  77080710 |       1 |\n" +
                        "| 0     | coffeehouse/jk/RedRiverValley.mp3               |   6363965 |       1 |\n" +
                        "| 0     | coffeehouse/jk/UnforgetWonderful_295x166.jpg    |     10724 |       1 |\n" +
                        "| 0     | coffeehouse/jk/Unforgettable-WonderfulWorld.m4a | 110054089 |       1 |\n" +
                        "| 0     | coffeehouse/jk/Unforgettable-WonderfulWorld.mp3 |   7520930 |       1 |\n" +
                        "| 0     | coffeehouse/jk/WhereOrWhen.m4a                  |  51272203 |       1 |\n" +
                        "| 0     | coffeehouse/jk/WhereOrWhen.mp3                  |   5647581 |       1 |\n" +
                        "| 0     | coffeehouse/jk/WhereOrWhen_295x166.jpg          |     11263 |       1 |\n" +
                        "| 0     | coffeehouse/jk/im_in_the_mood.m4a               |  11207247 |       1 |\n" +
                        "| 0     | coffeehouse/jk/im_in_the_mood_200.jpg           |      8621 |       1 |\n" +
                        "| 0     | coffeehouse/witchcraft.mp3                      |   6409093 |       1 |\n" +
                        "+-------+-------------------------------------------------+-----------+---------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "verify_bulk_job", "-b", "coffeehouse" });

        final String response = "<MasterObjectList Aggregating=\"false\" BucketName=\"coffeehouse\" CachedSizeInBytes=\"0\" ChunkClientProcessingOrderGuarantee=\"NONE\" CompletedSizeInBytes=\"0\" EntirelyInCache=\"false\" JobId=\"e0db4a7e-9957-4cf6-81c5-d3c320f8d56d\" Naked=\"false\" Name=\"VERIFY by 192.168.20.19\" OriginalSizeInBytes=\"341376176\" Priority=\"LOW\" RequestType=\"VERIFY\" StartDate=\"2016-06-16T18:13:34.000Z\" Status=\"IN_PROGRESS\" UserId=\"67235923-f684-4621-a958-1815e0bbf895\" UserName=\"spectra\">" +
                "<Nodes><Node EndPoint=\"10.1.20.88\" HttpPort=\"80\" HttpsPort=\"443\" Id=\"b272e757-31b0-11e6-948b-0007432b8090\"/></Nodes>" +
                "<Objects ChunkId=\"db94b108-6d0e-4f46-993c-b2f459e4b88f\" ChunkNumber=\"0\" NodeId=\"b272e757-31b0-11e6-948b-0007432b8090\">" +
                    "<Object Id=\"53452a07-699a-4c27-8de5-95aa0a431df1\" InCache=\"true\" Latest=\"true\" Length=\"9\" Name=\"123456789.txt\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"7989ad4a-47a5-41ac-8814-3746e4e20679\" InCache=\"true\" Latest=\"true\" Length=\"9172\" Name=\"Always_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"6649c2cb-6e83-4c58-9fb8-9b4aec8b014b\" InCache=\"true\" Latest=\"true\" Length=\"29895\" Name=\"Chapter 9.docx\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"f725ef08-7e6f-4fe0-a256-798e561d878f\" InCache=\"true\" Latest=\"true\" Length=\"9114\" Name=\"RedRiverValley_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"dff0cbed-5b7f-480f-aa94-8adea7c59a3e\" InCache=\"true\" Latest=\"true\" Length=\"774741\" Name=\"Softphone Install.docx\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"ffd8266d-cdc5-4e49-81d4-d08314fcee5a\" InCache=\"true\" Latest=\"true\" Length=\"11059\" Name=\"ThinkingOutLoud_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"897b7e5b-59d8-4645-bc7a-f5c4b8154a0f\" InCache=\"true\" Latest=\"true\" Length=\"10724\" Name=\"UnforgetWonderful_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"7bb970d3-113f-413b-87d5-00b072059451\" InCache=\"true\" Latest=\"true\" Length=\"10634\" Name=\"YouDontKnowMe_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"1e293dc9-3257-4277-9c40-b50a6e63b71e\" InCache=\"true\" Latest=\"true\" Length=\"294056\" Name=\"beowulf.txt\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"d759f10d-05c6-498c-b4ce-2475027fbeae\" InCache=\"true\" Latest=\"true\" Length=\"3309717\" Name=\"coffeehouse/im_in_the_mood.mp3\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"d9b342ae-311c-4cbc-a000-75686c174471\" InCache=\"true\" Latest=\"true\" Length=\"45872985\" Name=\"coffeehouse/jk/ColumbinesGrow.m4a\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"c85fc175-116a-4bcf-a77a-5ea240a5de3a\" InCache=\"true\" Latest=\"true\" Length=\"5050747\" Name=\"coffeehouse/jk/ColumbinesGrow.mp3\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"b70bd4ab-90d2-41fd-83d2-572fb3d1c8ca\" InCache=\"true\" Latest=\"true\" Length=\"10528\" Name=\"coffeehouse/jk/Columbines_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"e4769cd2-3aa6-4628-887c-ad51768656c5\" InCache=\"true\" Latest=\"true\" Length=\"10396369\" Name=\"coffeehouse/jk/Misty_2015.m4a\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"9ffa7e9c-6939-4808-996e-e42fcf8bacb5\" InCache=\"true\" Latest=\"true\" Length=\"77080710\" Name=\"coffeehouse/jk/RedRiverValley.m4a\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"564a1bc1-33a0-41f3-af28-fbf79f331d0e\" InCache=\"true\" Latest=\"true\" Length=\"6363965\" Name=\"coffeehouse/jk/RedRiverValley.mp3\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"b2671db7-1a4a-4577-8419-f17ead63d321\" InCache=\"true\" Latest=\"true\" Length=\"10724\" Name=\"coffeehouse/jk/UnforgetWonderful_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"71807ee9-2db9-4145-b01d-3d2aaae37061\" InCache=\"true\" Latest=\"true\" Length=\"110054089\" Name=\"coffeehouse/jk/Unforgettable-WonderfulWorld.m4a\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"e50d5fc8-8fbf-4206-b495-05bb8be539ec\" InCache=\"true\" Latest=\"true\" Length=\"7520930\" Name=\"coffeehouse/jk/Unforgettable-WonderfulWorld.mp3\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"9156aab6-88fa-49b0-a0e1-c230d247957e\" InCache=\"true\" Latest=\"true\" Length=\"51272203\" Name=\"coffeehouse/jk/WhereOrWhen.m4a\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"0f5541b9-8c4d-4ed8-bd1d-9e62173bdf4a\" InCache=\"true\" Latest=\"true\" Length=\"5647581\" Name=\"coffeehouse/jk/WhereOrWhen.mp3\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"03b2e1c7-f80c-437a-912d-b09015dba484\" InCache=\"true\" Latest=\"true\" Length=\"11263\" Name=\"coffeehouse/jk/WhereOrWhen_295x166.jpg\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"667d94f6-b341-45f7-bd91-706af52d8e77\" InCache=\"true\" Latest=\"true\" Length=\"11207247\" Name=\"coffeehouse/jk/im_in_the_mood.m4a\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"f7f65e20-4ea2-4629-9c22-ddf9cbc76b99\" InCache=\"true\" Latest=\"true\" Length=\"8621\" Name=\"coffeehouse/jk/im_in_the_mood_200.jpg\" Offset=\"0\" Version=\"1\"/>" +
                    "<Object Id=\"92a40cff-63a6-4520-81a9-80afa03a1973\" InCache=\"true\" Latest=\"true\" Length=\"6409093\" Name=\"coffeehouse/witchcraft.mp3\" Offset=\"0\" Version=\"1\"/>" +
                "</Objects>" +
            "</MasterObjectList>";

        final Ds3ClientHelpers helpers = mock(Ds3ClientHelpers.class);

        // just have helpers return something
        final Contents cont1 = new Contents();
        cont1.setKey("1234");
        cont1.setSize(1234L);
        final Iterable<Contents> retObj = Lists.newArrayList(cont1);
        when(helpers.listObjects(eq("coffeehouse"), eq(""))).thenReturn(retObj);

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final VerifyBulkJobSpectraS3Response verifyResponse = new VerifyBulkJobSpectraS3Response(webResponse);
        when(client.verifyBulkJobSpectraS3(any(VerifyBulkJobSpectraS3Request.class))).thenReturn(verifyResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, helpers), null);
        command.init(args);
        final CommandResponse result = command.render();

        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test(expected = FailedRequestException.class)
    public void verifyBulkJobMissingBucket() throws Exception {

        final String expected = "Cannot locate bucket: fredcoffeehouse";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_objects_on_tape", "-i", "b4d7cef1-80fa-4552-ad3f-4de716f515ea"});
        final String response = "<Error>" +
                "<Code>NoSuchBucket</Code>" +
                "<HttpErrorCode>404</HttpErrorCode>" +
                "<Message>NoSuchBucket[404]: Bucket does not exist where SQL: name = &apos;fredcoffeehouse&apos;</Message>" +
                "<Resource>/fredcoffeehouse</Resource>" +
                "<ResourceId>2097</ResourceId>" +
                "</Error>";

        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(404);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));
        final VerifyBulkJobSpectraS3Response verifyResponse
                = new VerifyBulkJobSpectraS3Response(webResponse);
        when(client.verifyBulkJobSpectraS3(any(VerifyBulkJobSpectraS3Request.class))).thenReturn(verifyResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void reclaimCache() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "reclaim_cache"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final ForceFullCacheReclaimSpectraS3Response cacheResponse = new ForceFullCacheReclaimSpectraS3Response(webResponse);
        when(client.forceFullCacheReclaimSpectraS3(any(ForceFullCacheReclaimSpectraS3Request.class))).thenReturn(cacheResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is("Success: Forced Reclaim of Cache"));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void ejectStorageDomain() throws Exception {
        final String expected
                = "Scheduled Eject of Storage Domain 9ffa7e9c-6939-4808-996e-e42fcf8bacb5\nBucket: buckety\nEject label: 1234\nEject location: 5678";
        final Arguments args
                = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a",
                "access", "-c", "eject_storage_domain", "-i", "9ffa7e9c-6939-4808-996e-e42fcf8bacb5",
                "--eject-label", "1234", "--eject-location", "5678", "-b", "buckety"});
        final Ds3Client client = mock(Ds3Client.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(204);
        when(webResponse.getHeaders()).thenReturn(headers);

        final EjectStorageDomainSpectraS3Response ejectResponse = new EjectStorageDomainSpectraS3Response(webResponse);
        when(client.ejectStorageDomainSpectraS3(any(EjectStorageDomainSpectraS3Request.class))).thenReturn(ejectResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test(expected = MissingOptionException.class)
    public void ejectStorageDomainBadArgs() throws Exception {
        final Arguments args
                = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a",
                "access", "-c", "eject_storage_domain", "-i", "9ffa7e9c-6939-4808-996e-e42fcf8bacb5",
                "--eject-label", "1234", "--eject-location", "5678"});
        final Ds3Client client = mock(Ds3Client.class);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        command.render();
    }

    @Test
    public void getStorageDomains() throws Exception {

        final String expected =
            "+-----------------------+--------------------------------------+-------------+-------------------------+--------------------+\n" +
            "|          Name         |                  ID                  | LTFS Naming |          Flags          | Write Optimization |\n" +
            "+-----------------------+--------------------------------------+-------------+-------------------------+--------------------+\n" +
            "| Db Backup Second Copy | a4420271-ab93-4446-8f03-7d2ba8f94529 | OBJECT_ID   | Secure Media Allocation | CAPACITY           |\n" +
            "| Tape Second Copy      | 79bb4290-86c7-4aab-801d-392418591c7d | OBJECT_ID   |                         | CAPACITY           |\n" +
            "| eject_test            | d7751014-e2eb-4cbc-9613-cabb7907f60f | OBJECT_ID   |                         | CAPACITY           |\n" +
            "| smoke_test_sd         | f2903e2b-0f0d-430c-8176-b46076681823 | OBJECT_ID   |                         | CAPACITY           |\n" +
            "+-----------------------+--------------------------------------+-------------+-------------------------+--------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_storage_domains",
                "--writeOptimization", "capacity"});
        final String response = "<Data>" +
                "<StorageDomain><AutoEjectMediaFullThreshold/><AutoEjectUponCron/><AutoEjectUponJobCancellation>false</AutoEjectUponJobCancellation><AutoEjectUponJobCompletion>false</AutoEjectUponJobCompletion><AutoEjectUponMediaFull>false</AutoEjectUponMediaFull><Id>a4420271-ab93-4446-8f03-7d2ba8f94529</Id><LtfsFileNaming>OBJECT_ID</LtfsFileNaming><MaxTapeFragmentationPercent>65</MaxTapeFragmentationPercent><MaximumAutoVerificationFrequencyInDays/><MediaEjectionAllowed>true</MediaEjectionAllowed><Name>Db Backup Second Copy</Name><SecureMediaAllocation>true</SecureMediaAllocation><VerifyPriorToAutoEject/><WriteOptimization>CAPACITY</WriteOptimization></StorageDomain>" +
                "<StorageDomain><AutoEjectMediaFullThreshold/><AutoEjectUponCron/><AutoEjectUponJobCancellation>false</AutoEjectUponJobCancellation><AutoEjectUponJobCompletion>false</AutoEjectUponJobCompletion><AutoEjectUponMediaFull>false</AutoEjectUponMediaFull><Id>79bb4290-86c7-4aab-801d-392418591c7d</Id><LtfsFileNaming>OBJECT_ID</LtfsFileNaming><MaxTapeFragmentationPercent>65</MaxTapeFragmentationPercent><MaximumAutoVerificationFrequencyInDays/><MediaEjectionAllowed>true</MediaEjectionAllowed><Name>Tape Second Copy</Name><SecureMediaAllocation>false</SecureMediaAllocation><VerifyPriorToAutoEject/><WriteOptimization>CAPACITY</WriteOptimization></StorageDomain>" +
                "<StorageDomain><AutoEjectMediaFullThreshold/><AutoEjectUponCron/><AutoEjectUponJobCancellation>false</AutoEjectUponJobCancellation><AutoEjectUponJobCompletion>false</AutoEjectUponJobCompletion><AutoEjectUponMediaFull>false</AutoEjectUponMediaFull><Id>d7751014-e2eb-4cbc-9613-cabb7907f60f</Id><LtfsFileNaming>OBJECT_ID</LtfsFileNaming><MaxTapeFragmentationPercent>65</MaxTapeFragmentationPercent><MaximumAutoVerificationFrequencyInDays/><MediaEjectionAllowed>false</MediaEjectionAllowed><Name>eject_test</Name><SecureMediaAllocation>false</SecureMediaAllocation><VerifyPriorToAutoEject/><WriteOptimization>CAPACITY</WriteOptimization></StorageDomain>" +
                "<StorageDomain><AutoEjectMediaFullThreshold/><AutoEjectUponCron/><AutoEjectUponJobCancellation>false</AutoEjectUponJobCancellation><AutoEjectUponJobCompletion>false</AutoEjectUponJobCompletion><AutoEjectUponMediaFull>false</AutoEjectUponMediaFull><Id>f2903e2b-0f0d-430c-8176-b46076681823</Id><LtfsFileNaming>OBJECT_ID</LtfsFileNaming><MaxTapeFragmentationPercent>65</MaxTapeFragmentationPercent><MaximumAutoVerificationFrequencyInDays/><MediaEjectionAllowed>true</MediaEjectionAllowed><Name>smoke_test_sd</Name><SecureMediaAllocation>false</SecureMediaAllocation><VerifyPriorToAutoEject/><WriteOptimization>CAPACITY</WriteOptimization></StorageDomain>" +
                "</Data>";

        final Ds3Client client = mock(Ds3Client.class);

        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(IOUtils.toInputStream(response));

        final GetStorageDomainsSpectraS3Response storageDomainResponse = new GetStorageDomainsSpectraS3Response(webResponse);
        when(client.getStorageDomainsSpectraS3(any(GetStorageDomainsSpectraS3Request.class))).thenReturn(storageDomainResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expected));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getTapes() throws Exception {
        final String expectedString =
                "+----------+--------------------------------------+--------+--------------------------+------------------------+--------------------------------------+----------------------------+---------------+-------------------+----------------+------------------+\n" +
                        "| Bar Code |                  ID                  |  State |       Last Modified      | Available Raw Capacity |               BucketID               | Assigned to Storage Domain | Ejection Date | Ejection Location | Ejection Label | Ejection Pending |\n" +
                        "+----------+--------------------------------------+--------+--------------------------+------------------------+--------------------------------------+----------------------------+---------------+-------------------+----------------+------------------+\n" +
                        "| 121552L6 | 52741a53-24d5-4391-87a9-9cce703d7ed7 | NORMAL | 2016-06-29T20:24:35.000Z | 2408082046976          | N/A                                  | false                      | N/A           | N/A               | N/A            | N/A              |\n" +
                        "| 121553L6 | e9e2e2c8-813b-4adf-9ed9-c6f788084656 | NORMAL | 2016-07-18T03:04:30.000Z | 2407684636672          | 5f02264b-b344-4bdd-88bd-7e87133bb0c9 | true                       | N/A           | N/A               | N/A            | N/A              |\n" +
                        "| 121555L6 | 8cb037d1-39aa-4f42-b27c-acbdf8b4c3c7 | NORMAL | 2016-06-29T20:18:44.000Z | 2408082046976          | N/A                                  | false                      | N/A           | N/A               | N/A            | N/A              |\n" +
                        "| 122104L6 | b16a8737-8801-4658-971c-c67d6ae44773 | NORMAL | 2016-07-18T03:07:26.000Z | 2407688830976          | 5f02264b-b344-4bdd-88bd-7e87133bb0c9 | true                       | N/A           | N/A               | N/A            | N/A              |\n" +
                        "+----------+--------------------------------------+--------+--------------------------+------------------------+--------------------------------------+----------------------------+---------------+-------------------+----------------+------------------+\n";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_tapes"});
        final Ds3Client client = mock(Ds3Client.class);
        final Headers headers = mock(Headers.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final InputStream stream = IOUtils.toInputStream("<Data><Tape>" +
                "<AssignedToStorageDomain>false</AssignedToStorageDomain>" +
                "<AvailableRawCapacity>2408082046976</AvailableRawCapacity>" +
                "<BarCode>121552L6</BarCode><BucketId/><DescriptionForIdentification/>" +
                "<EjectDate/><EjectLabel/><EjectLocation/><EjectPending/><FullOfData>false</FullOfData>" +
                "<Id>52741a53-24d5-4391-87a9-9cce703d7ed7</Id><LastAccessed>2016-06-29T20:28:45.000Z</LastAccessed>" +
                "<LastCheckpoint>6fc8a8c6-0b14-4ef6-a4ec-b7246028fa8e:2</LastCheckpoint><LastModified>2016-06-29T20:24:35.000Z</LastModified><LastVerified/>" +
                "<PartitionId>f3a7b5dd-af2d-4dc3-84d9-6ac69fab135c</PartitionId><PreviousState/>" +
                "<SerialNumber>HP-Y140125415</SerialNumber><State>NORMAL</State><StorageDomainId/>" +
                "<TakeOwnershipPending>false</TakeOwnershipPending><TotalRawCapacity>2408088338432</TotalRawCapacity>" +
                "<Type>LTO6</Type><VerifyPending/><WriteProtected>false</WriteProtected></Tape>" +
                "<Tape>" +
                "<AssignedToStorageDomain>true</AssignedToStorageDomain>" +
                "<AvailableRawCapacity>2407684636672</AvailableRawCapacity>" +
                "<BarCode>121553L6</BarCode><BucketId>5f02264b-b344-4bdd-88bd-7e87133bb0c9</BucketId>" +
                "<DescriptionForIdentification/><EjectDate/><EjectLabel/><EjectLocation/><EjectPending/>" +
                "<FullOfData>false</FullOfData><Id>e9e2e2c8-813b-4adf-9ed9-c6f788084656</Id>" +
                "<LastAccessed>2016-07-18T03:04:30.000Z</LastAccessed><LastCheckpoint>d6942856-5f43-4ccd-9508-06e6d9e32acd:16</LastCheckpoint>" +
                "<LastModified>2016-07-18T03:04:30.000Z</LastModified><LastVerified/>" +
                "<PartitionId>f3a7b5dd-af2d-4dc3-84d9-6ac69fab135c</PartitionId><PreviousState/>" +
                "<SerialNumber>HP-Y140125438</SerialNumber><State>NORMAL</State>" +
                "<StorageDomainId>93032a20-0751-4bb9-8b00-585aed548f55</StorageDomainId>" +
                "<TakeOwnershipPending>false</TakeOwnershipPending><TotalRawCapacity>2408088338432</TotalRawCapacity>" +
                "<Type>LTO6</Type><VerifyPending/><WriteProtected>false</WriteProtected></Tape>" +
                "<Tape>" +
                "<AssignedToStorageDomain>false</AssignedToStorageDomain>" +
                "<AvailableRawCapacity>2408082046976</AvailableRawCapacity>" +
                "<BarCode>121555L6</BarCode><BucketId/><DescriptionForIdentification/><EjectDate/><EjectLabel/><EjectLocation/><EjectPending/>" +
                "<FullOfData>false</FullOfData><Id>8cb037d1-39aa-4f42-b27c-acbdf8b4c3c7</Id>" +
                "<LastAccessed>2016-06-29T20:22:54.000Z</LastAccessed>" +
                "<LastCheckpoint>723aeba4-2fbe-43e2-a14c-cd8975d56c0c:2</LastCheckpoint>" +
                "<LastModified>2016-06-29T20:18:44.000Z</LastModified><LastVerified/>" +
                "<PartitionId>f3a7b5dd-af2d-4dc3-84d9-6ac69fab135c</PartitionId><PreviousState/>" +
                "<SerialNumber>HP-S140125234</SerialNumber><State>NORMAL</State><StorageDomainId/>" +
                "<TakeOwnershipPending>false</TakeOwnershipPending><TotalRawCapacity>2408088338432</TotalRawCapacity>" +
                "<Type>LTO6</Type><VerifyPending/><WriteProtected>false</WriteProtected></Tape>" +
                "<Tape>" +
                "<AssignedToStorageDomain>true</AssignedToStorageDomain>" +
                "<AvailableRawCapacity>2407688830976</AvailableRawCapacity>" +
                "<BarCode>122104L6</BarCode>" +
                "<BucketId>5f02264b-b344-4bdd-88bd-7e87133bb0c9</BucketId>" +
                "<DescriptionForIdentification/><EjectDate/><EjectLabel/><EjectLocation/>" +
                "<EjectPending/><FullOfData>false</FullOfData>" +
                "<Id>b16a8737-8801-4658-971c-c67d6ae44773</Id>" +
                "<LastAccessed>2016-07-18T03:07:26.000Z</LastAccessed>" +
                "<LastCheckpoint>3e88b82e-d8f4-4229-8159-bbdd175d3f0b:16</LastCheckpoint>" +
                "<LastModified>2016-07-18T03:07:26.000Z</LastModified><LastVerified/>" +
                "<PartitionId>f3a7b5dd-af2d-4dc3-84d9-6ac69fab135c</PartitionId><PreviousState/>" +
                "<SerialNumber>HP-X131014007</SerialNumber>" +
                "<State>NORMAL</State>" +
                "<StorageDomainId>cf08b1e3-12d7-407a-acfe-83afc7b835f4</StorageDomainId>" +
                "<TakeOwnershipPending>false</TakeOwnershipPending>" +
                "<TotalRawCapacity>2408088338432</TotalRawCapacity>" +
                "<Type>LTO6</Type><VerifyPending/>" +
                "<WriteProtected>false</WriteProtected>" +
                "</Tape></Data>", "utf-8");

        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetTapesSpectraS3Response tapesResponse = new GetTapesSpectraS3Response(webResponse);
        when(client.getTapesSpectraS3(any(GetTapesSpectraS3Request.class))).thenReturn(tapesResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void ejectTape() throws Exception {
        final String expectedString =
                "Tape Bar Code: 362449L5, ID: 190da533-a254-4fbd-ae71-c2aed0580448, Tape Type: LTO5, " +
                        "Serial Number: HP-G140314442, State: FORMAT_IN_PROGRESS, " +
                        "Partition Id 9bccaba8-440a-431f-9357-5a66c48d09f2, Available Space: 1424834428928, " +
                        "Full: false, Write Protected: false, " +
                        "Last Modified: 2017-01-19T22:16:09.000Z, Last Verification: N/A";

        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "eject_tape", "-i", "52741a53-24d5-4391-87a9-9cce703d7ed7"});
        final Ds3Client client = mock(Ds3Client.class);
        final Headers headers = mock(Headers.class);
        final WebResponse webResponse = mock(WebResponse.class);
        final InputStream stream = IOUtils.toInputStream("<Data>" +
                "<AssignedToStorageDomain>false</AssignedToStorageDomain>" +
                "<AvailableRawCapacity>1424834428928</AvailableRawCapacity>" +
                "<BarCode>362449L5</BarCode><BucketId/>" +
                "<DescriptionForIdentification>67e68d2e-eaf0-4430-a2c1-05991b522cc3:BlackPearl@59ee0e90-a3e5-46e7-932b-0d9ce2096025</DescriptionForIdentification>" +
                "<EjectDate/><EjectLabel/><EjectLocation/><EjectPending>2017-01-19T22:17:03.000Z</EjectPending>" +
                "<FullOfData>false</FullOfData><Id>190da533-a254-4fbd-ae71-c2aed0580448</Id>" +
                "<LastAccessed>2017-01-19T22:16:09.000Z</LastAccessed><LastCheckpoint/>" +
                "<LastModified>2017-01-19T22:16:09.000Z</LastModified><LastVerified/>" +
                "<PartiallyVerifiedEndOfTape/><PartitionId>9bccaba8-440a-431f-9357-5a66c48d09f2</PartitionId>" +
                "<PreviousState>FOREIGN</PreviousState><SerialNumber>HP-G140314442</SerialNumber>" +
                "<State>FORMAT_IN_PROGRESS</State><StorageDomainId/>" +
                "<TakeOwnershipPending>false</TakeOwnershipPending><TotalRawCapacity>1425000103936</TotalRawCapacity>" +
                "<Type>LTO5</Type><VerifyPending/><WriteProtected>false</WriteProtected>" +
                "</Data>", "utf-8");

        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final EjectTapeSpectraS3Response tapesResponse = new EjectTapeSpectraS3Response(webResponse);
        when(client.ejectTapeSpectraS3(any(EjectTapeSpectraS3Request.class))).thenReturn(tapesResponse);

        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand())
                .withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void headObject() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "head_object", "-b", "bucketName", "-o", "ulysses.txt"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final WebResponse webResponse = mock(WebResponse.class);
        final Headers headers = mock(Headers.class);

        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);

        final HeadObjectResponse response = new HeadObjectResponse(webResponse);
        when(client.headObject(any(HeadObjectRequest.class))).thenReturn(response);

        final CommandResponse result = command.render();
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getCacheState() throws Exception {
        final String expectedString =
                "+-----------------------------------------+--------------------+---------------+----------------------+----------------+--------------+------------------------+-----------------+-----------------+--------------------------------------+--------------------------------------+\n" +
                        "|                   Path                  | Available Capacity | Used Capacity | Unavailable Capacity | Total Capacity | Max Capacity | Auto Reclaim Threshold | Burst Threshold | Max Utilization |                  ID                  |                Node ID               |\n" +
                        "+-----------------------------------------+--------------------+---------------+----------------------+----------------+--------------+------------------------+-----------------+-----------------+--------------------------------------+--------------------------------------+\n" +
                        "| /usr/local/bluestorm/frontend/cachedir/ | 13652840915953     | 171154354395  | 0                    | 13823995270348 | N/A          | 0.82                   | 0.85            | 0.9             | a1c27433-74f2-11e6-8d1e-002590c31f18 | a1c27433-74f2-11e6-8d1e-002590c31f18 |\n" +
                        "+-----------------------------------------+--------------------+---------------+----------------------+----------------+--------------+------------------------+-----------------+-----------------+--------------------------------------+--------------------------------------+\n";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_cache_state"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        final WebResponse webResponse = mock(WebResponse.class);

        final InputStream stream = IOUtils.toInputStream("<Data><Filesystems>" +
                "<AvailableCapacityInBytes>13652840915953</AvailableCapacityInBytes>" +
                "<CacheFilesystem>" +
                "<AutoReclaimInitiateThreshold>0.82</AutoReclaimInitiateThreshold>" +
                "<AutoReclaimTerminateThreshold>0.72</AutoReclaimTerminateThreshold>" +
                "<BurstThreshold>0.85</BurstThreshold>" +
                "<Id>a1c27433-74f2-11e6-8d1e-002590c31f18</Id>" +
                "<MaxCapacityInBytes/><MaxPercentUtilizationOfFilesystem>0.9</MaxPercentUtilizationOfFilesystem>" +
                "<NodeId>a1c27433-74f2-11e6-8d1e-002590c31f18</NodeId>" +
                "<Path>/usr/local/bluestorm/frontend/cachedir/</Path>" +
                "</CacheFilesystem>" +
                "<Summary>12378 blobs allocated by cache (1475975 max): 159 GB allocated, 12715 GB available (0 B pending reclaim), 12874 GB total.  Cache throttling starts at 10943 GB.  Auto-reclaims start at 10557 GB and stop early once usage drops below 9269 GB.</Summary>" +
                "<TotalCapacityInBytes>13823995270348</TotalCapacityInBytes>" +
                "<UnavailableCapacityInBytes>0</UnavailableCapacityInBytes>" +
                "<UsedCapacityInBytes>171154354395</UsedCapacityInBytes>" +
                "</Filesystems></Data>", "utf-8");

        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetCacheStateSpectraS3Response cacheStateResponse = new GetCacheStateSpectraS3Response(webResponse);

        when(client.getCacheStateSpectraS3(any(GetCacheStateSpectraS3Request.class))).thenReturn(cacheStateResponse);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getCacheStateJson() throws Exception {
        final String expectedString =
                "  \"Data\" : [ {\n" +
                        "    \"AvailableCapacityInBytes\" : 13652840915953,\n" +
                        "    \"CacheFilesystem\" : {\n" +
                        "      \"AutoReclaimInitiateThreshold\" : 0.82,\n" +
                        "      \"AutoReclaimTerminateThreshold\" : 0.72,\n" +
                        "      \"BurstThreshold\" : 0.85,\n" +
                        "      \"Id\" : \"a1c27433-74f2-11e6-8d1e-002590c31f18\",\n" +
                        "      \"MaxCapacityInBytes\" : null,\n" +
                        "      \"MaxPercentUtilizationOfFilesystem\" : 0.9,\n" +
                        "      \"NodeId\" : \"a1c27433-74f2-11e6-8d1e-002590c31f18\",\n" +
                        "      \"Path\" : \"/usr/local/bluestorm/frontend/cachedir/\"\n" +
                        "    },\n" +
                        "    \"Entries\" : [ ],\n" +
                        "    \"Summary\" : \"12378 blobs allocated by cache (1475975 max): 159 GB allocated, 12715 GB available (0 B pending reclaim), 12874 GB total.  Cache throttling starts at 10943 GB.  Auto-reclaims start at 10557 GB and stop early once usage drops below 9269 GB.\",\n" +
                        "    \"TotalCapacityInBytes\" : 13823995270348,\n" +
                        "    \"UnavailableCapacityInBytes\" : 0,\n" +
                        "    \"UsedCapacityInBytes\" : 171154354395\n" +
                        "  } ],\n" +
                        "  \"Status\" : \"OK\"\n" +
                        "}";


        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_cache_state", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        final WebResponse webResponse = mock(WebResponse.class);

        final InputStream stream = IOUtils.toInputStream("<Data><Filesystems>" +
                "<AvailableCapacityInBytes>13652840915953</AvailableCapacityInBytes>" +
                "<CacheFilesystem>" +
                "<AutoReclaimInitiateThreshold>0.82</AutoReclaimInitiateThreshold>" +
                "<AutoReclaimTerminateThreshold>0.72</AutoReclaimTerminateThreshold>" +
                "<BurstThreshold>0.85</BurstThreshold>" +
                "<Id>a1c27433-74f2-11e6-8d1e-002590c31f18</Id>" +
                "<MaxCapacityInBytes/><MaxPercentUtilizationOfFilesystem>0.9</MaxPercentUtilizationOfFilesystem>" +
                "<NodeId>a1c27433-74f2-11e6-8d1e-002590c31f18</NodeId>" +
                "<Path>/usr/local/bluestorm/frontend/cachedir/</Path>" +
                "</CacheFilesystem>" +
                "<Summary>12378 blobs allocated by cache (1475975 max): 159 GB allocated, 12715 GB available (0 B pending reclaim), 12874 GB total.  Cache throttling starts at 10943 GB.  Auto-reclaims start at 10557 GB and stop early once usage drops below 9269 GB.</Summary>" +
                "<TotalCapacityInBytes>13823995270348</TotalCapacityInBytes>" +
                "<UnavailableCapacityInBytes>0</UnavailableCapacityInBytes>" +
                "<UsedCapacityInBytes>171154354395</UsedCapacityInBytes>" +
                "</Filesystems></Data>", "utf-8");

        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetCacheStateSpectraS3Response cacheStateResponse = new GetCacheStateSpectraS3Response(webResponse);

        when(client.getCacheStateSpectraS3(any(GetCacheStateSpectraS3Request.class))).thenReturn(cacheStateResponse);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getCapacitySummary() throws Exception {
        final String expectedString =
                "+-------------+-----------+------+------+\n" +
                        "|  Container  | Allocated | Used | Free |\n" +
                        "+-------------+-----------+------+------+\n" +
                        "| Pool        | 9         | 7    | 2    |\n" +
                        "| Tape        | 8         | 5    | 3    |\n" +
                        "+-------------+-----------+------+------+\n";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_capacity_summary"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        final WebResponse webResponse = mock(WebResponse.class);

        final InputStream stream = IOUtils.toInputStream("<Data>" +
                "<Pool>" +
                "<PhysicalAllocated>9</PhysicalAllocated>" +
                "<PhysicalAvailable>0</PhysicalAvailable>" +
                "<PhysicalFree>2</PhysicalFree>" +
                "<PhysicalUsed>7</PhysicalUsed>" +
                "</Pool>" +
                "<Tape>" +
                "<PhysicalAllocated>8</PhysicalAllocated>" +
                "<PhysicalAvailable>0</PhysicalAvailable>" +
                "<PhysicalFree>3</PhysicalFree>" +
                "<PhysicalUsed>5</PhysicalUsed>" +
                "</Tape>" +
                "</Data>", "utf-8");

        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetSystemCapacitySummarySpectraS3Response response = new GetSystemCapacitySummarySpectraS3Response(webResponse);

        when(client.getSystemCapacitySummarySpectraS3(any(GetSystemCapacitySummarySpectraS3Request.class))).thenReturn(response);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getCapacitySummaryJson() throws Exception {
        final String expectedString =
                "  \"Data\" : {\n" +
                        "    \"Pool\" : {\n" +
                        "      \"PhysicalAllocated\" : 9,\n" +
                        "      \"PhysicalFree\" : 2,\n" +
                        "      \"PhysicalUsed\" : 7\n" +
                        "    },\n" +
                        "    \"Tape\" : {\n" +
                        "      \"PhysicalAllocated\" : 8,\n" +
                        "      \"PhysicalFree\" : 3,\n" +
                        "      \"PhysicalUsed\" : 5\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"Status\" : \"OK\"\n" +
                        "}";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_capacity_summary", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        final WebResponse webResponse = mock(WebResponse.class);

        final InputStream stream = IOUtils.toInputStream("<Data>" +
                "<Pool>" +
                "<PhysicalAllocated>9</PhysicalAllocated>" +
                "<PhysicalAvailable>0</PhysicalAvailable>" +
                "<PhysicalFree>2</PhysicalFree>" +
                "<PhysicalUsed>7</PhysicalUsed>" +
                "</Pool>" +
                "<Tape>" +
                "<PhysicalAllocated>8</PhysicalAllocated>" +
                "<PhysicalAvailable>0</PhysicalAvailable>" +
                "<PhysicalFree>3</PhysicalFree>" +
                "<PhysicalUsed>5</PhysicalUsed>" +
                "</Tape>" +
                "</Data>", "utf-8");

        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetSystemCapacitySummarySpectraS3Response response = new GetSystemCapacitySummarySpectraS3Response(webResponse);

        when(client.getSystemCapacitySummarySpectraS3(any(GetSystemCapacitySummarySpectraS3Request.class))).thenReturn(response);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getDataPathBackend() throws Exception {
        final String expectedString =
                "+-----------+--------------+--------------+---------------------+--------------------------------------+--------------------------+--------------------------+-----------------------------+----------------------------------+\n" +
                        "| Activated | Auto Timeout | Auto Inspect | Conflict Resolution |                  ID                  |      Last Heartbeat      | Unavailable Media Policy | Unavailable Pool Retry Mins | Unavailable Partition Retry Mins |\n" +
                        "+-----------+--------------+--------------+---------------------+--------------------------------------+--------------------------+--------------------------+-----------------------------+----------------------------------+\n" +
                        "| true      | 30           | DEFAULT      | CANCEL              | 5d45ab7a-b83f-4dc1-95d5-a45b59e48718 | 2016-09-07T22:09:55.000Z | DISALLOW                 | 20                          | 20                               |\n" +
                        "+-----------+--------------+--------------+---------------------+--------------------------------------+--------------------------+--------------------------+-----------------------------+----------------------------------+\n";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_data_path_backend"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        final WebResponse webResponse = mock(WebResponse.class);

        final InputStream stream = IOUtils.toInputStream("<Data>" +
                "<Activated>true</Activated>" +
                "<AutoActivateTimeoutInMins>30</AutoActivateTimeoutInMins>" +
                "<AutoInspect>DEFAULT</AutoInspect>" +
                "<DefaultImportConflictResolutionMode>CANCEL</DefaultImportConflictResolutionMode>" +
                "<Id>5d45ab7a-b83f-4dc1-95d5-a45b59e48718</Id>" +
                "<InstanceId>5d45ab7a-b83f-4dc1-95d5-a45b59e48718</InstanceId>" +
                "<LastHeartbeat>2016-09-07T22:09:55.000Z</LastHeartbeat>" +
                "<PartiallyVerifyLastPercentOfTapes/>" +
                "<UnavailableMediaPolicy>DISALLOW</UnavailableMediaPolicy>" +
                "<UnavailablePoolMaxJobRetryInMins>20</UnavailablePoolMaxJobRetryInMins>" +
                "<UnavailableTapePartitionMaxJobRetryInMins>20</UnavailableTapePartitionMaxJobRetryInMins>" +
                "</Data>", "utf-8");

        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetDataPathBackendSpectraS3Response response = new GetDataPathBackendSpectraS3Response(webResponse);

        when(client.getDataPathBackendSpectraS3(any(GetDataPathBackendSpectraS3Request.class))).thenReturn(response);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getDataPathBackendJson() throws Exception {
        final String expectedString =
                "  \"Data\" : {\n" +
                        "    \"Activated\" : true,\n" +
                        "    \"AutoActivateTimeoutInMins\" : 30,\n" +
                        "    \"AutoInspect\" : \"DEFAULT\",\n" +
                        "    \"DefaultImportConflictResolutionMode\" : \"CANCEL\",\n" +
                        "    \"Id\" : \"5d45ab7a-b83f-4dc1-95d5-a45b59e48718\",\n" +
                        "    \"InstanceId\" : \"5d45ab7a-b83f-4dc1-95d5-a45b59e48718\",\n" +
                        "    \"LastHeartbeat\" : \"2016-09-07T22:09:55.000Z\",\n" +
                        "    \"PartiallyVerifyLastPercentOfTapes\" : null,\n" +
                        "    \"UnavailableMediaPolicy\" : \"DISALLOW\",\n" +
                        "    \"UnavailablePoolMaxJobRetryInMins\" : 20,\n" +
                        "    \"UnavailableTapePartitionMaxJobRetryInMins\" : 20\n" +
                        "  },\n" +
                        "  \"Status\" : \"OK\"\n" +
                        "}";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_data_path_backend", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        final WebResponse webResponse = mock(WebResponse.class);

        final InputStream stream = IOUtils.toInputStream("<Data>" +
                "<Activated>true</Activated>" +
                "<AutoActivateTimeoutInMins>30</AutoActivateTimeoutInMins>" +
                "<AutoInspect>DEFAULT</AutoInspect>" +
                "<DefaultImportConflictResolutionMode>CANCEL</DefaultImportConflictResolutionMode>" +
                "<Id>5d45ab7a-b83f-4dc1-95d5-a45b59e48718</Id>" +
                "<InstanceId>5d45ab7a-b83f-4dc1-95d5-a45b59e48718</InstanceId>" +
                "<LastHeartbeat>2016-09-07T22:09:55.000Z</LastHeartbeat>" +
                "<PartiallyVerifyLastPercentOfTapes/>" +
                "<UnavailableMediaPolicy>DISALLOW</UnavailableMediaPolicy>" +
                "<UnavailablePoolMaxJobRetryInMins>20</UnavailablePoolMaxJobRetryInMins>" +
                "<UnavailableTapePartitionMaxJobRetryInMins>20</UnavailableTapePartitionMaxJobRetryInMins>" +
                "</Data>", "utf-8");

        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetDataPathBackendSpectraS3Response response = new GetDataPathBackendSpectraS3Response(webResponse);

        when(client.getDataPathBackendSpectraS3(any(GetDataPathBackendSpectraS3Request.class))).thenReturn(response);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getJobs() throws Exception {
        final String expectedString =
                "+-------------+--------------------------------------+--------------------------+-----------+----------+-------------+\n" +
                        "| Bucket Name |                Job Id                |       Creation Date      | User Name | Job Type |    Status   |\n" +
                        "+-------------+--------------------------------------+--------------------------+-----------+----------+-------------+\n" +
                        "| coffeehouse | 52dc72a9-7876-4024-9034-d2f6e886f7e7 | 2016-08-30T22:14:49.000Z | jk        | PUT      | IN_PROGRESS |\n" +
                        "+-------------+--------------------------------------+--------------------------+-----------+----------+-------------+\n";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_jobs"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        final WebResponse webResponse = mock(WebResponse.class);

        final InputStream stream = IOUtils.toInputStream("<Jobs>" +
                "<Job Aggregating=\"false\" BucketName=\"coffeehouse\" " +
                "CachedSizeInBytes=\"343479386\" " +
                "ChunkClientProcessingOrderGuarantee=\"IN_ORDER\" " +
                "CompletedSizeInBytes=\"0\" " +
                "JobId=\"52dc72a9-7876-4024-9034-d2f6e886f7e7\" " +
                "Naked=\"false\" Name=\"PUT by 192.168.1.12\" " +
                "OriginalSizeInBytes=\"343479386\" Priority=\"NORMAL\" " +
                "RequestType=\"PUT\" StartDate=\"2016-08-30T22:14:49.000Z\" " +
                "Status=\"IN_PROGRESS\" UserId=\"5079e312-bcff-43c7-bd54-d8148af0a515\" " +
                "UserName=\"jk\"><Nodes/></Job></Jobs>", "utf-8");

        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetJobsSpectraS3Response response = new GetJobsSpectraS3Response(webResponse);

        when(client.getJobsSpectraS3(any(GetJobsSpectraS3Request.class))).thenReturn(response);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getJobsWithCompleted() throws Exception {
        final String expectedString =
                "+-------------+--------------------------------------+--------------------------+-----------+----------+-------------+\n" +
                        "| Bucket Name |                Job Id                |       Creation Date      | User Name | Job Type |    Status   |\n" +
                        "+-------------+--------------------------------------+--------------------------+-----------+----------+-------------+\n" +
                        "| coffeehouse | 52dc72a9-7876-4024-9034-d2f6e886f7e7 | 2016-08-30T22:14:49.000Z | jk        | PUT      | IN_PROGRESS |\n" +
                        "| coffeehouse | 2e9cc564-95d4-4f25-abe2-acee0746b5a7 | 2016-08-30T21:55:09.000Z | jk        | PUT      | CANCELED    |\n" +
                        "+-------------+--------------------------------------+--------------------------+-----------+----------+-------------+\n";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_jobs", "--completed"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        final WebResponse webResponse = mock(WebResponse.class);

        final InputStream stream = IOUtils.toInputStream("<Jobs>" +
                "<Job Aggregating=\"false\" BucketName=\"coffeehouse\" " +
                "CachedSizeInBytes=\"343479386\" " +
                "ChunkClientProcessingOrderGuarantee=\"IN_ORDER\" " +
                "CompletedSizeInBytes=\"0\" " +
                "JobId=\"52dc72a9-7876-4024-9034-d2f6e886f7e7\" " +
                "Naked=\"false\" Name=\"PUT by 192.168.1.12\" " +
                "OriginalSizeInBytes=\"343479386\" Priority=\"NORMAL\" " +
                "RequestType=\"PUT\" StartDate=\"2016-08-30T22:14:49.000Z\" " +
                "Status=\"IN_PROGRESS\" UserId=\"5079e312-bcff-43c7-bd54-d8148af0a515\" " +
                "UserName=\"jk\"><Nodes/></Job>" +
                "<Job Aggregating=\"false\" BucketName=\"coffeehouse\" " +
                "CachedSizeInBytes=\"343509281\" " +
                "ChunkClientProcessingOrderGuarantee=\"IN_ORDER\" " +
                "CompletedSizeInBytes=\"0\" " +
                "JobId=\"2e9cc564-95d4-4f25-abe2-acee0746b5a7\" " +
                "Naked=\"false\" Name=\"PUT by 192.168.1.12\" " +
                "OriginalSizeInBytes=\"343509281\" Priority=\"NORMAL\" " +
                "RequestType=\"PUT\" StartDate=\"2016-08-30T21:55:09.000Z\" " +
                "Status=\"CANCELED\" UserId=\"5079e312-bcff-43c7-bd54-d8148af0a515\" " +
                "UserName=\"jk\"><Nodes/></Job>" +
                "</Jobs>", "utf-8");

        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetJobsSpectraS3Response response = new GetJobsSpectraS3Response(webResponse);

        when(client.getJobsSpectraS3(any(GetJobsSpectraS3Request.class))).thenReturn(response);
        final CommandResponse result = command.render();
        assertThat(result.getMessage(), is(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    @Test
    public void getJobsJson() throws Exception {

        final String expectedString = "\"Data\" : {\n" +
                "    \"result\" : {\n" +
                "      \"Job\" : [ {\n" +
                "        \"aggregating\" : false,\n" +
                "        \"bucketName\" : \"coffeehouse\",\n" +
                "        \"cachedSizeInBytes\" : 343479386,\n" +
                "        \"chunkClientProcessingOrderGuarantee\" : \"IN_ORDER\",\n" +
                "        \"completedSizeInBytes\" : 0,\n" +
                "        \"entirelyInCache\" : false,\n" +
                "        \"jobId\" : \"52dc72a9-7876-4024-9034-d2f6e886f7e7\",\n" +
                "        \"naked\" : false,\n" +
                "        \"name\" : \"PUT by 192.168.1.12\",\n" +
                "        \"originalSizeInBytes\" : 343479386,\n" +
                "        \"priority\" : \"NORMAL\",\n" +
                "        \"requestType\" : \"PUT\",\n" +
                "        \"startDate\" : \"2016-08-30T22:14:49.000Z\",\n" +
                "        \"status\" : \"IN_PROGRESS\",\n" +
                "        \"userId\" : \"5079e312-bcff-43c7-bd54-d8148af0a515\",\n" +
                "        \"userName\" : \"jk\",\n" +
                "        \"Nodes\" : null\n" +
                "      } ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"Status\" : \"OK\"\n" +
                "}";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_jobs", "--output-format", "json"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        final WebResponse webResponse = mock(WebResponse.class);

        final InputStream stream = IOUtils.toInputStream("<Jobs>" +
                "<Job Aggregating=\"false\" BucketName=\"coffeehouse\" " +
                "CachedSizeInBytes=\"343479386\" " +
                "ChunkClientProcessingOrderGuarantee=\"IN_ORDER\" " +
                "CompletedSizeInBytes=\"0\" " +
                "JobId=\"52dc72a9-7876-4024-9034-d2f6e886f7e7\" " +
                "Naked=\"false\" Name=\"PUT by 192.168.1.12\" " +
                "OriginalSizeInBytes=\"343479386\" Priority=\"NORMAL\" " +
                "RequestType=\"PUT\" StartDate=\"2016-08-30T22:14:49.000Z\" " +
                "Status=\"IN_PROGRESS\" UserId=\"5079e312-bcff-43c7-bd54-d8148af0a515\" " +
                "UserName=\"jk\"><Nodes/></Job></Jobs>", "utf-8");

        final Headers headers = mock(Headers.class);
        when(webResponse.getStatusCode()).thenReturn(200);
        when(webResponse.getHeaders()).thenReturn(headers);
        when(webResponse.getResponseStream()).thenReturn(stream);

        final GetJobsSpectraS3Response response = new GetJobsSpectraS3Response(webResponse);

        when(client.getJobsSpectraS3(any(GetJobsSpectraS3Request.class))).thenReturn(response);
        final CommandResponse result = command.render();
        assertTrue(result.getMessage().endsWith(expectedString));
        assertThat(result.getReturnCode(), is(0));
    }

    /**
     * Build list of objects to test result anb view of GetDetailedObjects and GetDetailedObjectsPhysical
     */
    private DetailedS3Object buildDetailedObject(final String name, final long size, final String id, final String owner,
                                                 final String createDate, final String[] barcodes)
            throws Exception {
        final DetailedS3Object obj1 = new DetailedS3Object();
        obj1.setName(name);
        obj1.setSize(size);
        obj1.setBucketId(UUID.fromString(id));
        obj1.setOwner(owner);
        obj1.setType(S3ObjectType.DATA);
        obj1.setCreationDate(DATE_FORMAT.parse(createDate));
        final BulkObjectList bulkObjectList = new BulkObjectList();
        final BulkObject bulk1 = new BulkObject();
        final PhysicalPlacement placement1 = new PhysicalPlacement();
        final ImmutableList.Builder<Tape> tapes = ImmutableList.builder();
        for (final String barcode : barcodes) {
            final Tape tape1 = new Tape();
            tape1.setBarCode(barcode);
            tape1.setState(TapeState.NORMAL);
            tapes.add(tape1);
        }
        placement1.setTapes(tapes.build());
        bulk1.setPhysicalPlacement(placement1);
        bulkObjectList.setObjects(ImmutableList.of(bulk1));
        obj1.setBlobs(bulkObjectList);
        return obj1;
    }

    @Test
    public void getDetailedObjects() throws Exception {
        final String expectedString =
                "+--------------------------------+--------------------------------------+-------+----------+------+--------------------------+---------------------+-------+\n" +
                        "|              Name              |                Bucket                | Owner |   Size   | Type |       Creation Date      |        Tapes        | Pools |\n" +
                        "+--------------------------------+--------------------------------------+-------+----------+------+--------------------------+---------------------+-------+\n" +
                        "| coffeehouse/im_in_the_mood.mp3 | c5ed6a28-1499-432d-85e5-e0b2d866ec65 | jk    | 3309717  | DATA | 2016-09-22T23:10:09.000Z | 362447L5 | 362453L5 |       |\n" +
                        "| coffeehouse/jk/Misty_2015.m4a  | c5ed6a28-1499-432d-85e5-e0b2d866ec65 | jk    | 10396369 | DATA | 2016-09-22T23:10:20.000Z | 362447L5 | 362453L5 |       |\n" +
                        "| coffeehouse/witchcraft.mp3     | c5ed6a28-1499-432d-85e5-e0b2d866ec65 | jk    | 6409093  | DATA | 2016-09-22T23:10:15.000Z | 362447L5 | 362453L5 |       |\n" +
                        "+--------------------------------+--------------------------------------+-------+----------+------+--------------------------+---------------------+-------+\n";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_detailed_objects", "-b", "jktwocopies", "--filter-params", "largerthan:1000000"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        // MockingDetails dets = mockingDetails(command);
        // builds the proper command from args
        assertTrue(command.getClass() == GetDetailedObjects.class);

        final ImmutableList.Builder<DetailedS3Object> objects = new ImmutableList.Builder<>();
        final String[] barcodes = new String[] {"362447L5", "362453L5"};
        objects.add(buildDetailedObject("coffeehouse/im_in_the_mood.mp3", 3309717L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:09.000Z",  barcodes));
        objects.add(buildDetailedObject("coffeehouse/jk/Misty_2015.m4a", 10396369L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:20.000Z2016-09-22T23:10:20.000Z",  barcodes));
        objects.add(buildDetailedObject("coffeehouse/witchcraft.mp3", 6409093L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:15.000Z",  barcodes));

        final GetDetailedObjectsResult result = new GetDetailedObjectsResult(objects.build());

        final String actual = command.getView().render(result);
        assertThat(actual, is(expectedString));
    }

    @Test(expected = BadArgumentException.class )
    public void getDetailedObjectsBadParam() throws Exception {
        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_detailed_objects", "-b", "jktwocopies", "--filter-params", "largerthan:1000000,fred:wilma", "--output-format", "csv"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
    }

    @Test
    public void getDetailedObjectsCsv() throws Exception {
        final String expectedString =
                "Name,Bucket,Owner,Size,Type,Creation Date,Tapes,Pools\r\n" +
                        "coffeehouse/im_in_the_mood.mp3,c5ed6a28-1499-432d-85e5-e0b2d866ec65,jk,3309717,DATA,2016-09-22T23:10:09.000Z,362447L5 | 362453L5,\r\n" +
                        "coffeehouse/jk/Misty_2015.m4a,c5ed6a28-1499-432d-85e5-e0b2d866ec65,jk,10396369,DATA,2016-09-22T23:10:20.000Z,362447L5 | 362453L5,\r\n" +
                        "coffeehouse/witchcraft.mp3,c5ed6a28-1499-432d-85e5-e0b2d866ec65,jk,6409093,DATA,2016-09-22T23:10:15.000Z,362447L5 | 362453L5,\r\n";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_detailed_objects", "-b", "jktwocopies", "--filter-params", "largerthan:1000000", "--output-format", "csv"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);

        // MockingDetails dets = mockingDetails(command);
        // builds the proper command from args
        assertTrue(command.getClass() == GetDetailedObjects.class);

        final ImmutableList.Builder<DetailedS3Object> objects = new ImmutableList.Builder<>();
        final String[] barcodes = new String[] {"362447L5", "362453L5"};
        objects.add(buildDetailedObject("coffeehouse/im_in_the_mood.mp3", 3309717L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:09.000Z",  barcodes));
        objects.add(buildDetailedObject("coffeehouse/jk/Misty_2015.m4a", 10396369L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:20.000Z2016-09-22T23:10:20.000Z",  barcodes));
        objects.add(buildDetailedObject("coffeehouse/witchcraft.mp3", 6409093L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:15.000Z",  barcodes));

        final GetDetailedObjectsResult result = new GetDetailedObjectsResult(objects.build());

        final String actual = command.getView().render(result);
        assertThat(actual, is(expectedString));
    }

    @Test
    public void getDetailedObjectsPhysicalCsv() throws Exception {
        final String expectedString =
                "Name,Bucket,Owner,Size,Type,Creation Date,Barcode,State\r\n" +
                        "coffeehouse/im_in_the_mood.mp3,c5ed6a28-1499-432d-85e5-e0b2d866ec65,jk,3309717,DATA,2016-09-22T23:10:09.000Z,362447L5,NORMAL\r\n" +
                        "coffeehouse/im_in_the_mood.mp3,c5ed6a28-1499-432d-85e5-e0b2d866ec65,jk,3309717,DATA,2016-09-22T23:10:09.000Z,362453L5,NORMAL\r\n" +
                        "coffeehouse/jk/Misty_2015.m4a,c5ed6a28-1499-432d-85e5-e0b2d866ec65,jk,10396369,DATA,2016-09-22T23:10:20.000Z,362447L5,NORMAL\r\n" +
                        "coffeehouse/jk/Misty_2015.m4a,c5ed6a28-1499-432d-85e5-e0b2d866ec65,jk,10396369,DATA,2016-09-22T23:10:20.000Z,362453L5,NORMAL\r\n" +
                        "coffeehouse/witchcraft.mp3,c5ed6a28-1499-432d-85e5-e0b2d866ec65,jk,6409093,DATA,2016-09-22T23:10:15.000Z,362447L5,NORMAL\r\n" +
                        "coffeehouse/witchcraft.mp3,c5ed6a28-1499-432d-85e5-e0b2d866ec65,jk,6409093,DATA,2016-09-22T23:10:15.000Z,362453L5,NORMAL\r\n";

        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_detailed_objects_physical", "-b", "jktwocopies", "--filter-params", "largerthan:1000000", "--output-format", "csv"});
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand());
        command.init(args);
        assertTrue(command.getClass() == GetDetailedObjectsPhysical.class);

        final ImmutableList.Builder<DetailedS3Object> objects = new ImmutableList.Builder<>();
        final String[] barcodes = new String[] {"362447L5", "362453L5"};
        objects.add(buildDetailedObject("coffeehouse/im_in_the_mood.mp3", 3309717L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:09.000Z",  barcodes));
        objects.add(buildDetailedObject("coffeehouse/jk/Misty_2015.m4a", 10396369L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:20.000Z2016-09-22T23:10:20.000Z",  barcodes));
        objects.add(buildDetailedObject("coffeehouse/witchcraft.mp3", 6409093L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:15.000Z",  barcodes));

        final GetDetailedObjectsResult result = new GetDetailedObjectsResult(objects.build());

        final String actual = command.getView().render(result);
        assertThat(actual, is(expectedString));
    }

    @Test
    public void getDetailedObjectsPhysicalNoPhysical() throws Exception {
        final String expectedString = "No speciifed objects have physical placement";
        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_detailed_objects_physical", "-b", "jktwocopies", "--filter-params", "largerthan:1000000"});
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand());
        command.init(args);
        assertTrue(command.getClass() == GetDetailedObjectsPhysical.class);

        final ImmutableList.Builder<DetailedS3Object> objects = new ImmutableList.Builder<>();
        final String[] barcodes = new String[] {};
        objects.add(buildDetailedObject("coffeehouse/im_in_the_mood.mp3", 3309717L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:09.000Z",  barcodes));
        objects.add(buildDetailedObject("coffeehouse/jk/Misty_2015.m4a", 10396369L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:20.000Z2016-09-22T23:10:20.000Z",  barcodes));
        objects.add(buildDetailedObject("coffeehouse/witchcraft.mp3", 6409093L, "c5ed6a28-1499-432d-85e5-e0b2d866ec65", "jk", "2016-09-22T23:10:15.000Z",  barcodes));

        final GetDetailedObjectsResult result = new GetDetailedObjectsResult(objects.build());

        final String actual = command.getView().render(result);
        assertThat(actual, is(expectedString));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void getDetailedObjectsBadArgs() throws Exception {
        final Arguments args = new Arguments( new String[] {"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_detailed_objects_physical", "-b", "jktwocopies", "--filter-params", "largerthan:1000000", "-d", "directory"});
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand());
        command.init(args);
    }

    @Test
    public void parseRelativeDate() throws Exception {
        final String input = "d1.h2.m3.s4";
        final long diff = DateUtils.dateDiffToSeconds(input);
        assertEquals(diff, (24 * 60 * 60) + (2 * 60 * 60) + (3 * 60) + 4);
    }

    @Test
    public void parseAbsoluteDate() throws Exception {
        final String input = "Y1960.M5.D26";
        final long parse = DateUtils.parseParamDate(input).getTime();
        final long construct =  Constants.DATE_FORMAT.parse("1960-05-26T00:00:00.000Z").getTime();
        assertEquals(parse, construct);
    }

    @Test
    public void parseAbsoluteDateTimeZoneShift() throws Exception {
        final String input1 = "Y1960.M5.D26.ZMST";
        final long parse1 = DateUtils.parseParamDate(input1).getTime();
        final String input2 = "Y1960.M5.D26.ZCST";
        final long parse2 = DateUtils.parseParamDate(input2).getTime();
        // one hour off
        assertEquals(parse1 - 60 * 60 * 1000, parse2);
    }

    @Test(expected = ParseException.class)
    public void parseAbsoluteDateBad() throws Exception {
        final String input = "Y2016.M5.D26.Fred";
        final Date parse = DateUtils.parseParamDate(input);
    }

    @Test(expected = ParseException.class)
    public void parseAbsoluteDateBadArgs() throws Exception {
        final Arguments args = new Arguments(new String[]{"ds3_java_cli", "-e", "localhost:8080", "-k", "key!", "-a", "access", "-c", "get_detailed_objects", "-b", "jktwocopies", "--filter-params", "after:Y2016.fred,before:Y2016.wilma"});
        final Ds3Client client = mock(Ds3Client.class);
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(new Ds3ProviderImpl(client, null), null);
        command.init(args);
        final CommandResponse result = command.render();
    }

}
