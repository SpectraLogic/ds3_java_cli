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

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.Ds3ExceptionHandlerFactory;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.DeleteFolderRecursivelySpectraS3Request;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;

public class DeleteFolder extends CliCommand<DefaultResult> {

    private String bucketName;
    private String folderName;

    public DeleteFolder() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        folderName = args.getDirectory();
        if (bucketName == null) {
            throw new MissingOptionException("The delete folder command requires '-b' to be set.");
        }
        if (folderName == null) {
            throw new MissingOptionException("The delete folder command requires '-d' to be set.");
        }
        return this;
    }

    @Override
    public DefaultResult call() throws IOException {
        getClient().deleteFolderRecursivelySpectraS3(new DeleteFolderRecursivelySpectraS3Request(bucketName, folderName));

        return new DefaultResult("Success: Deleted folder '" + folderName + "'.");
    }
}
