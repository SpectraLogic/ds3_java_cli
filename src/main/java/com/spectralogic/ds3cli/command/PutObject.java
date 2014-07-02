/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.BadArgumentException;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.PutObjectRequest;
import com.spectralogic.ds3client.helpers.ResettableFileInputStream;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class PutObject extends CliCommand {

    private String bucketName;
    private Path objectPath;
    private String objectName;

    public PutObject(final Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The get object command requires '-b' to be set.");
        }
        objectName = args.getObjectName();
        if (objectName == null) {
            throw new MissingOptionException("The get object command requires '-o' to be set.");
        }

        if (args.getDirectory() != null) {
            throw new BadArgumentException("'-d' should not be used with the command 'put_object'.  If you want to move an entire directory, use 'put_bulk' instead.");
        }

        objectPath = FileSystems.getDefault().getPath(args.getObjectName());
        if(!Files.exists(objectPath)) {
            throw new BadArgumentException("File '"+ objectName +"' does not exist.");
        }
        if (!Files.isRegularFile(objectPath)) {
            throw new BadArgumentException("The '-o' command must be a file and not a directory.");
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public String call() throws Exception {

        try(final ResettableFileInputStream stream = new ResettableFileInputStream(new FileInputStream(objectPath.toFile()))) {
            getClient().putObject(new PutObjectRequest(bucketName, objectName, Files.size(objectPath), stream)).close();
        }

        return "Success: Finished writing file to ds3 appliance.";
    }
}
