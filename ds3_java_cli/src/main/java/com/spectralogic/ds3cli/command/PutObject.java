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
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.models.PutObjectResult;
import com.spectralogic.ds3cli.util.BlackPearlUtils;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.util.SyncUtils;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SignatureException;

public class PutObject extends CliCommand<PutObjectResult> {

    private final static Logger LOG = LoggerFactory.getLogger(PutObject.class);

    private String bucketName;
    private Path objectPath;
    private String objectName;
    private String prefix;
    private boolean sync;
    private boolean force;

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
        if (!getFileUtils().exists(objectPath)) {
            throw new BadArgumentException("File '" + objectName + "' does not exist.");
        }
        if (!getFileUtils().isRegularFile(objectPath)) {
            throw new BadArgumentException("The '-o' command must be a file and not a directory.");
        }

        this.prefix = args.getPrefix();
        this.force = args.isForce();

        if (args.isSync()) {
            LOG.info("Using sync command");
            this.sync = true;
        }

        return this;
    }

    @Override
    public PutObjectResult call() throws Exception {
        if (!force) {
            BlackPearlUtils.checkBlackPearlForTapeFailure(getClient());
        }

        final Ds3ClientHelpers helpers = getClientHelpers();
        final Ds3Object ds3Obj = new Ds3Object(normalizeObjectName(objectName), getFileUtils().size(objectPath));

        if (prefix != null) {
            LOG.info("Pre-appending " + prefix + " to object name");
            ds3Obj.setName(prefix + ds3Obj.getName());
        }

        /* Ensure the bucket exists and if not create it */
        helpers.ensureBucketExists(this.bucketName);

        if (sync) {
            if (!SyncUtils.isSyncSupported(getClient())) {
                return new PutObjectResult("Failed: The sync command is not supported with your version of BlackPearl.");
            }

            if (SyncUtils.needToSync(helpers, bucketName, objectPath, ds3Obj.getName(), true)) {
                Transfer(helpers, ds3Obj);
                return new PutObjectResult("Success: Finished syncing file to ds3 appliance.");
            } else {
                return new PutObjectResult("Success: No need to sync " + objectName);
            }
        }

        Transfer(helpers, ds3Obj);
        return new PutObjectResult("Success: Finished writing file to ds3 appliance.");
    }

    private void Transfer(final Ds3ClientHelpers helpers, final Ds3Object ds3Obj) throws SignatureException, IOException, XmlProcessingException {
        final Ds3ClientHelpers.Job putJob = helpers.startWriteJob(bucketName, Lists.newArrayList(ds3Obj));
        putJob.transfer(new Ds3ClientHelpers.ObjectChannelBuilder() {
            @Override
            public SeekableByteChannel buildChannel(final String s) throws IOException {
                return FileChannel.open(objectPath, StandardOpenOption.READ);
            }
        });
    }

    private static String normalizeObjectName(final String objectName) {
        final String path;
        final int colonIndex = objectName.indexOf(':');
        if (colonIndex != -1) {
            path = objectName.substring(colonIndex + 2);
        } else if (objectName.startsWith("/")) {
            return objectName.substring(1);
        } else {
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
