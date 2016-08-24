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
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.CancelJobSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.CancelJobSpectraS3Response;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DeleteJob extends CliCommand<DefaultResult> {

    private final static Logger LOG = LoggerFactory.getLogger(DeleteJob.class);

    private UUID id;

    public DeleteJob() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        if (args.getId() == null) {
            throw new MissingOptionException("The delete job command requires '-i' to be set.");
        }
        this.id = UUID.fromString(args.getId());
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        // Force is always on in CancelJobSpectraS3Request
        final CancelJobSpectraS3Request request = new CancelJobSpectraS3Request(id);
        final CancelJobSpectraS3Response response = getClient().cancelJobSpectraS3(request);
        return new DefaultResult("SUCCESS: Deleted job '" + this.id.toString() + "'");
    }
}
