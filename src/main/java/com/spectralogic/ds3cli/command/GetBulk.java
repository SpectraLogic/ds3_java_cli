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
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.SignatureException;

public class GetBulk extends CliCommand {
    private String bucketName;
    private Path outputPath;
    private String prefix;
    public GetBulk(final Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The bulk get command requires '-b' to be set.");
        }

        final String directory = args.getDirectory();
        if (directory == null || directory.equals(".")) {
            outputPath = FileSystems.getDefault().getPath(".");
        } else {
            final Path dirPath = FileSystems.getDefault().getPath(directory);
            outputPath = FileSystems.getDefault().getPath(".").resolve(dirPath);
        }

        this.prefix = args.getPrefix();
        return this;
    }


    @Override
    public String call() throws Exception {
        if (this.prefix == null) {
            Logging.log("Getting all objects from " + this.bucketName);
            return restoreAll();
        }
        else {
            Logging.log("Getting only those objects that start with " + this.prefix);
            return restoreSome();
        }
    }

    private String restoreSome() throws IOException, SignatureException, XmlProcessingException {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(getClient());
        final Iterable<Contents> contents = helper.listObjects(this.bucketName, this.prefix);
        final Iterable<Ds3Object> objects = Iterables.transform(contents, new Function<Contents, Ds3Object>() {
            @Override
            public Ds3Object apply(final Contents input) {
                return new Ds3Object(input.getKey(), input.getSize());
            }
        });

        final Ds3ClientHelpers.ReadJob job = helper.startReadJob(this.bucketName, objects);
        job.read(new FileObjectGetter(this.outputPath));
        return "SUCCESS: Wrote all the objects that start with '"+ this.prefix +"' from " + this.bucketName + " to " + this.outputPath.toString();
    }

    private String restoreAll() throws XmlProcessingException, SignatureException, IOException {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(getClient());
        final Ds3ClientHelpers.ReadJob job = helper.startReadAllJob(this.bucketName);
        job.read(new FileObjectGetter(this.outputPath));

        return "SUCCESS: Wrote all the objects from " + this.bucketName + " to " + this.outputPath.toString();
    }


}
