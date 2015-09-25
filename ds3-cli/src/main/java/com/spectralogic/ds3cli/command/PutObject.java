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

import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.BadArgumentException;
import com.spectralogic.ds3cli.models.PutObjectResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PutObject extends CliCommand<PutObjectResult> {

    private final static Logger LOG = LoggerFactory.getLogger(PutObject.class);

    private String bucketName;
    private Path objectPath;
    private String objectName;
    private String prefix;

    public PutObject(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The put object command requires '-b' to be set.");
        }
        objectName = args.getObjectName();
        if (objectName == null) {
            throw new MissingOptionException("The put object command requires '-o' to be set.");
        }

        if (args.getDirectory() != null) {
            throw new BadArgumentException("'-d' should not be used with the command 'put_object'.  If you want to move an entire directory, use 'put_bulk' instead.");
        }

        objectPath = FileSystems.getDefault().getPath(args.getObjectName());
        if(!getFileUtils().exists(objectPath)) {
            throw new BadArgumentException("File '"+ objectName +"' does not exist.");
        }
        if (!getFileUtils().isRegularFile(objectPath)) {
            throw new BadArgumentException("The '-o' command must be a file and not a directory.");
        }

        this.prefix = args.getPrefix();

        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public PutObjectResult call() throws Exception {
        final Ds3ClientHelpers helpers = getClientHelpers();
        final Ds3Object ds3Obj = new Ds3Object(normalizeObjectName(objectName), getFileUtils().size(objectPath));

        if(prefix != null) {
            LOG.info("Pre-appending " + prefix + " to object name");
            ds3Obj.setName(prefix + ds3Obj.getName());
        }

        final Ds3ClientHelpers.Job putJob = helpers.startWriteJob(bucketName, Lists.newArrayList(ds3Obj));
        putJob.transfer(new Ds3ClientHelpers.ObjectChannelBuilder() {
            @Override
            public SeekableByteChannel buildChannel(final String s) throws IOException {
                return FileChannel.open(objectPath, StandardOpenOption.READ);
            }
        });

        return new PutObjectResult("Success: Finished writing file to ds3 appliance.");
    }

    private static String normalizeObjectName(final String objectName) {
        final String path;
        final int colonIndex = objectName.indexOf(':');
        if (colonIndex != -1) {
            path = objectName.substring(colonIndex + 2);
        }
        else if (objectName.startsWith("/")) {
            return objectName.substring(1);
        }
        else {
            path = objectName;
        }
        if (!path.contains("\\")) {
            return path;
        }

        final String normalizedPath = path.replace("\\", "/");
        LOG.info("Normalized object name: " + normalizedPath);
        return normalizedPath;
    }
}
