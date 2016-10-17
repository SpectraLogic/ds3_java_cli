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
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.DeletePermanentlyLostTapeSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.EjectStorageDomainSpectraS3Request;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.util.UUID;

public class EjectStorageDomain extends CliCommand<DefaultResult> {

    private UUID id;
    private String ejectLabel;
    private String ejectLocation;
    private String bucket;

    public EjectStorageDomain() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.id = UUID.fromString(args.getId());
        if (this.id == null) {
            throw new MissingOptionException("The eject storage domain command requires '-i' to be set.");
        }
        this.bucket = args.getBucket();
        if (Guard.isStringNullOrEmpty(this.bucket)) {
            throw new MissingOptionException("The eject storage domain command requires '-b' to be set.");
        }
        this.ejectLabel = args.GetEjectLabel();
        this.ejectLocation = args.GetEjectLocation();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {

        try {
            final EjectStorageDomainSpectraS3Request request
                    = new EjectStorageDomainSpectraS3Request(id)
                    .withBucketId(bucket).withEjectLabel(ejectLabel).withEjectLocation(ejectLocation);
            this.getClient().ejectStorageDomainSpectraS3(request);
        }
        catch (final IOException e) {
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }

        final StringBuilder message = new StringBuilder("Scheduled Eject of Storage Domain ");
        message.append(id.toString());
        if(!Guard.isStringNullOrEmpty(bucket)) {
            message.append("\nBucket: ");
            message.append(bucket);
        }
        if(!Guard.isStringNullOrEmpty(ejectLabel)) {
            message.append("\nEject label: ");
            message.append(ejectLabel);
        }
        if(!Guard.isStringNullOrEmpty(ejectLocation)) {
            message.append("\nEject location: ");
            message.append(ejectLocation);
        }
        return new DefaultResult(message.toString());
    }
}
