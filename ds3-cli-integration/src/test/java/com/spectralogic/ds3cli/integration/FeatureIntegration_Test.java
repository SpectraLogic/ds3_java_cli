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

import com.spectralogic.ds3cli.*;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetJobRequest;
import com.spectralogic.ds3client.commands.GetJobResponse;
import com.spectralogic.ds3client.networking.Headers;
import com.spectralogic.ds3client.networking.WebResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureIntegration_Test {

    final private String[] CONNECTION = {"ds3_java_cli", "--http", "-e", "192.168.56.101:8080", "-k", "8qHN6TUE",
            "-a", "c3BlY3RyYQ=="};

    @Test
    public void getCompletedJob() throws Exception {
        final String jobId = "aa5df0cc-b03a-4cb9-b69d-56e7367e917f";

        final Arguments args = new Arguments(new String[]{"--http", "-c", "get_job", "-i", jobId});

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

    /*
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
    */


}
