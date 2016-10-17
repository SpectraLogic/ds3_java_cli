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

import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3client.commands.spectrads3.DeletePermanentlyLostTapeSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.EjectStorageDomainBlobsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.EjectStorageDomainSpectraS3Request;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingOptionException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EjectStorageDomainObjects extends CliCommand<DefaultResult> {

    private UUID id;
    private String ejectLabel;
    private String ejectLocation;
    private String bucket;
    private String directory;

    public EjectStorageDomainObjects() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.id = UUID.fromString(args.getId());
        if (this.id == null) {
            throw new MissingOptionException("The eject storage domain command requires '-i' to be set.");
        }
        this.directory = args.getDirectory();
        if (this.id == null) {
            throw new MissingOptionException("The eject storage domain objects command requires '-d' to be set.");
        }
        this.bucket = args.getBucket();
        this.ejectLabel = args.GetEjectLabel();
        this.ejectLocation = args.GetEjectLocation();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {

        final List<Path> paths = Utils.listObjectsForDirectory(Paths.get(this.directory));
        final List<Ds3Object> objects = new ArrayList<Ds3Object>(); 
        for (final Path path : paths) {
            objects.add(new Ds3Object(path.getFileName().toString()));
        }

        try {
            final Ds3ClientHelpers helpers = getClientHelpers();

            EjectStorageDomainBlobsSpectraS3Request request
                    = new EjectStorageDomainBlobsSpectraS3Request(bucket, (List<Ds3Object>)objects, id)
                    .withEjectLabel(ejectLabel)
                    .withEjectLocation(ejectLocation);
            getClient().ejectStorageDomainBlobsSpectraS3(request);
        }
        catch (final IOException e) {
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }

        final StringBuilder message = new StringBuilder("Scheduled Eject of Storage Domain ");
        message.append(id.toString());
        if(!Guard.isStringNullOrEmpty(directory)) {
            message.append("\nDirectory: ");
            message.append(directory);
        }
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
