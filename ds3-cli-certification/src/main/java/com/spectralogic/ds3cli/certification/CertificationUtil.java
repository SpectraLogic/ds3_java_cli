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

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.Ds3ProviderImpl;
import com.spectralogic.ds3cli.FileSystemProviderImpl;
import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.command.CliCommandFactory;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileSystemProvider;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.SpectraUser;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;


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
            return "";
        }
    }

    public static SpectraUser createUser(final Ds3Client client, final String username) throws IOException {
        final DelegateCreateUserSpectraS3Response response
                =  client.delegateCreateUserSpectraS3(new DelegateCreateUserSpectraS3Request(username));
        return response.getSpectraUserResult();
    }

    public static boolean deleteUser(final Ds3Client client, final String username) throws IOException {
        final DelegateDeleteUserSpectraS3Response response
                =  client.delegateDeleteUserSpectraS3(new DelegateDeleteUserSpectraS3Request(username));
        return (response.getStatusCode() == 200);
    }


    public static CommandResponse putPerformanceFiles(final Ds3Client client, final String bucketName, final int fileCount, final long fileSize) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "performance", "-b", bucketName,
                "-n", Integer.toString(fileCount), "-s", Long.toString(fileSize), "--do-not-delete" });
        final Ds3Provider provider = new Ds3ProviderImpl(client, Ds3ClientHelpers.wrap(client));
        final FileSystemProvider fileSystemProvider = new FileSystemProviderImpl();
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(provider, fileSystemProvider);
        command.init(args);
        return command.render();
    }

    public static String getBucketName(final String testName) {
        return "test_" + testName.replaceAll("[ !,.:;<>&]+", "_");
    }

}
