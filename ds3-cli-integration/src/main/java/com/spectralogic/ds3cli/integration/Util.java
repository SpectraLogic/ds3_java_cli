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
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static final String RESOURCE_BASE_NAME = "./src/test/resources/books/";
    public static final String DOWNLOAD_BASE_NAME = "./output/";

    private Util() {}

    public static CommandResponse command(final Ds3Client client, final Arguments args) throws Exception {
        final Ds3Provider provider = new Ds3ProviderImpl(client, Ds3ClientHelpers.wrap(client));
        final FileUtils fileUtils = new FileUtilsImpl();
        final Ds3Cli runner = new Ds3Cli(provider, args, fileUtils);
        final CommandResponse response = runner.call();

        return response;
    }

    public static CommandResponse createBucket(final Ds3Client client, final String bucketName) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "put_bucket", "-b", bucketName});
        return command(client, args);
    }

    public static CommandResponse deleteBucket(final Ds3Client client, final String bucketName) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "delete_bucket", "-b", bucketName, "--force"});
        return command(client, args);
    }

    public static void loadBookTestData(final Ds3Client client, final String bucketName) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "put_bulk", "-b", bucketName, "-d", RESOURCE_BASE_NAME});
        command(client, args);
    }

    public static void deleteLoadedFile(final String fileName) throws IOException {
        Files.deleteIfExists(Paths.get(DOWNLOAD_BASE_NAME + fileName));
    }
}
