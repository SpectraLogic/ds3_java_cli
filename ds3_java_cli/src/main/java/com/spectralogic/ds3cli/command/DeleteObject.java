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
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DeleteResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.DeleteObjectRequest;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;

public class DeleteObject extends CliCommand<DeleteResult> {
    
    private String bucketName;
    private String objectName;

    public DeleteObject(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The delete object command requires '-b' to be set.");
        }
        objectName = args.getObjectName();
        if (objectName == null) {
            throw new MissingOptionException("The delete object command requires '-o' to be set.");
        }
        return this;
    }

    @Override
    public String getLongHelp() {
        final StringBuffer helpStringBuffer = new StringBuffer();
        helpStringBuffer.append("Permanently deletes an object.\n");
        helpStringBuffer.append("Requires the '-b' parameter to specify bucketname.\n");
        helpStringBuffer.append("Requires the '-i' parameter to specify object name (UUID or name).\n");
        helpStringBuffer.append("\nUse the get_service command to retrieve a list of buckets.");
        helpStringBuffer.append("\nUse the get_bucket command to retrieve a list of objects.");

        return helpStringBuffer.toString();
    }

    @Override
    public DeleteResult call() throws Exception {
        try {
            getClient().deleteObject(new DeleteObjectRequest(bucketName, objectName));
        }
        catch (final IOException e) {
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }

        return new DeleteResult("Success: Deleted object '" + this.objectName + "' from bucket '" + this.bucketName + "'.");
    }
}
