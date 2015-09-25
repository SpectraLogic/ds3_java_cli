/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.models.DeleteResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.DeleteTapeDriveRequest;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;

public class DeleteTapeDrive extends CliCommand<DeleteResult> {

    private String id;

    public DeleteTapeDrive(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.id = args.getId();
        if (this.id == null) {
            throw new MissingOptionException("The delete tape drive command requires '-i' to be set.");
        }
        return this;
    }

    @Override
    public DeleteResult call() throws Exception {
        try {
            getClient().deleteTapeDrive(new DeleteTapeDriveRequest(this.id));
        }
        catch (final IOException e) {
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }

        return new DeleteResult("Success: Deleted tape drive '" + this.id+ "'.");
    }
}
