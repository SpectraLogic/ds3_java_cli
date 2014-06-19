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
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectGetter;
import org.apache.commons.cli.MissingOptionException;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class GetBulk extends CliCommand {
    private String bucketName;
    private Path outputPath;
    public GetBulk(final Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The bulk get command requires '-b' to be set.");
        }

        final String prefix = args.getDirectory();
        if (prefix == null || prefix.equals(".")) {
            outputPath = FileSystems.getDefault().getPath(".");
        } else {
            final Path dirPath = FileSystems.getDefault().getPath(prefix);
            outputPath = FileSystems.getDefault().getPath(".").resolve(dirPath);
        }
        return this;
    }


    @Override
    public String call() throws Exception {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(getClient());
        final Ds3ClientHelpers.ReadJob job = helper.startReadAllJob(bucketName);
        job.read(new FileObjectGetter(outputPath));

        return "SUCCESS: Wrote all the objects from " + bucketName + " to " + outputPath.toString();
    }
}
