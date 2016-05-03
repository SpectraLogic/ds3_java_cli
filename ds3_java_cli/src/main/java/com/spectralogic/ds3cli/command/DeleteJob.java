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
import com.spectralogic.ds3cli.models.DeleteResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.CancelJobSpectraS3Request;
import org.apache.commons.cli.MissingOptionException;

import java.util.UUID;

public class DeleteJob extends CliCommand<DeleteResult> {

    private UUID id;
    private boolean force;

    public DeleteJob(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        if (args.getId() == null) {
            throw new MissingOptionException("The delete job command requires '-i' to be set.");
        }
        this.id = UUID.fromString(args.getId());
        this.force = args.isForce();
        return this;
    }

    @Override
    public DeleteResult call() throws Exception {
        final CancelJobSpectraS3Request request = new CancelJobSpectraS3Request(id).withForce(this.force);
        getClient().cancelJobSpectraS3(request);
        return new DeleteResult("SUCCESS: Deleted job '"+ this.id.toString() +"'");
    }
}
