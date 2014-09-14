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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.logging.Logging;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectGetter;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.models.bulk.Priority;
import com.spectralogic.ds3client.serializer.XmlProcessingException;

import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.SignatureException;

public class GetBulk extends CliCommand {
    private String bucketName;
    private Path outputPath;
    private String prefix;
    private boolean checksum;
    private Priority priority;

    public GetBulk(final Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        if (this.bucketName == null) {
            throw new MissingOptionException("The bulk get command requires '-b' to be set.");
        }

        final String directory = args.getDirectory();
        if (directory == null || directory.equals(".")) {
            this.outputPath = FileSystems.getDefault().getPath(".");
        } else {
            final Path dirPath = FileSystems.getDefault().getPath(directory);
            this.outputPath = FileSystems.getDefault().getPath(".").resolve(dirPath);
        }

        this.priority = args.getPriority();
        this.checksum = args.isChecksum();
        this.prefix = args.getPrefix();
        return this;
    }


    @Override
    public String call() throws Exception {
        final Ds3ClientHelpers.ObjectTransferrer getter;
        if (checksum) {
            throw new java.lang.RuntimeException("Checksumming is currently not implemented.");//TODO
//            Logging.log("Performing get_bulk with checksum verification");
//            getter = new VerifyingFileObjectGetter(this.outputPath);
        }
        else {
            getter = new FileObjectGetter(this.outputPath);
        }

        if (this.prefix == null) {
            Logging.log("Getting all objects from " + this.bucketName);
            return restoreAll(getter);
        }
        else {
            Logging.log("Getting only those objects that start with " + this.prefix);
            return restoreSome(getter);
        }
    }

    private String restoreSome(final Ds3ClientHelpers.ObjectTransferrer getter) throws IOException, SignatureException, XmlProcessingException {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(getClient());
        final Iterable<Contents> contents = helper.listObjects(this.bucketName, this.prefix);
        final Iterable<Ds3Object> objects = Iterables.transform(contents, new Function<Contents, Ds3Object>() {
            @Override
            public Ds3Object apply(final Contents input) {
                return new Ds3Object(input.getKey(), input.getSize());
            }
        });

        final Ds3ClientHelpers.Job job = helper.startReadJob(this.bucketName, objects,
                ReadJobOptions.create()
                .withPriority(this.priority));

        job.transfer(new LoggingFileObjectGetter(getter));

        return "SUCCESS: Wrote all the objects that start with '"+ this.prefix +"' from " + this.bucketName + " to " + this.outputPath.toString();
    }

    private String restoreAll(final Ds3ClientHelpers.ObjectTransferrer getter) throws XmlProcessingException, SignatureException, IOException {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(getClient());
        final Ds3ClientHelpers.Job job = helper.startReadAllJob(this.bucketName,
                ReadJobOptions.create()
                .withPriority(this.priority));

        job.transfer(new LoggingFileObjectGetter(getter));

        return "SUCCESS: Wrote all the objects from " + this.bucketName + " to " + this.outputPath.toString();
    }


    class LoggingFileObjectGetter implements Ds3ClientHelpers.ObjectTransferrer {

        final private Ds3ClientHelpers.ObjectTransferrer objectGetter;

        public LoggingFileObjectGetter(final Ds3ClientHelpers.ObjectTransferrer getter) {
            this.objectGetter = getter;
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            Logging.logf("Getting object %s", s);
            return this.objectGetter.buildChannel(s);
        }
    }
}
