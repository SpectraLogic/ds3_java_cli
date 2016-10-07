/*
 * *****************************************************************************
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
 * ***************************************************************************
 */

package com.spectralogic.ds3cli.command;

import com.google.common.collect.ImmutableList;
import static com.spectralogic.ds3cli.ArgumentFactory.*;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.DeleteFolderRecursivelySpectraS3Request;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;

import java.io.IOException;

public class DeleteFolder extends CliCommand<DefaultResult> {

    private String bucketName;
    private String folderName;

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, DIRECTORY);

    public DeleteFolder() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        args.parseCommandLine();

        this.bucketName = args.getBucket();
        folderName = args.getDirectory();
        this.viewType = args.getOutputFormat();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        try {
            getClient().deleteFolderRecursivelySpectraS3(new DeleteFolderRecursivelySpectraS3Request(bucketName, folderName));
        }
        catch (final IOException e) {
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }
        return new DefaultResult("Success: Deleted folder '" + folderName + "'.");
    }
}
