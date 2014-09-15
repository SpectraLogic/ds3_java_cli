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
import com.spectralogic.ds3cli.logging.Logging;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectPutter;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.models.bulk.Priority;
import com.spectralogic.ds3client.models.bulk.WriteOptimization;

import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class PutBulk extends CliCommand {
    private String bucketName;
    private Path inputDirectory;
    private boolean checksum;
    private Priority priority;
    private WriteOptimization writeOptimization;

    public PutBulk(final Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        if (this.bucketName == null) {
            throw new MissingOptionException("The bulk put command requires '-b' to be set.");
        }

        final String srcDir = args.getDirectory();
        if (srcDir == null) {
            throw new MissingOptionException("The bulk put command required '-d' to be set.");
        }

        this.priority = args.getPriority();
        this.writeOptimization = args.getWriteOptimization();
        this.inputDirectory = FileSystems.getDefault().getPath(srcDir);
        this.checksum = args.isChecksum();

        return this;
    }

    @Override
    public String call() throws Exception {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(getClient());
        final Iterable<Ds3Object> objects = helper.listObjectsForDirectory(this.inputDirectory);

        helper.ensureBucketExists(this.bucketName);
        final Ds3ClientHelpers.Job job = helper.startWriteJob(this.bucketName, objects,
                WriteJobOptions.create()
                .withPriority(this.priority)
                .withWriteOptimization(this.writeOptimization));
        if (this.checksum) {
            throw new RuntimeException("Checksum calculation is not implemented in this release.");//TODO
//            Logging.log("Performing bulk put with checksum computation enabled");
//            job.withRequestModifier(new ComputedChecksumModifier());
        }
        job.transfer(new LoggingFileObjectPutter(this.inputDirectory));

        /* TODO add back in for next release
        final long startTime = System.currentTimeMillis();
        final long endTime = System.currentTimeMillis();

        TransferCalculationUtils.logTransferSpeed(endTime - startTime, TransferCalculationUtils.sum(objects));
        */

        return "SUCCESS: Wrote all the files in " + this.inputDirectory.toString() + " to bucket " + this.bucketName;
    }

    class LoggingFileObjectPutter implements Ds3ClientHelpers.ObjectTransferrer {
        final private FileObjectPutter objectPutter;

        public LoggingFileObjectPutter(final Path inputDirectory) {
            this.objectPutter = new FileObjectPutter(inputDirectory);
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            Logging.logf("Putting %s to ds3 endpoint", s);
            return this.objectPutter.buildChannel(s);
        }
    }
}
