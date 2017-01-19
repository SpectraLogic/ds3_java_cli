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
import com.spectralogic.ds3cli.api.Arguments;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.EjectStorageDomainSpectraS3Request;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.Option;

import java.util.UUID;

import static com.spectralogic.ds3cli.api.ArgumentFactory.*;

public class EjectStorageDomain extends BaseCliCommand<DefaultResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, ID);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(EJECT_LABEL, EJECT_LOCATION);

    private UUID id;
    private String ejectLabel;
    private String ejectLocation;
    private String bucket;

    public EjectStorageDomain() {
    }

    @Override
    public BaseCliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.id = UUID.fromString(args.getId());
        this.bucket = args.getBucket();
        this.ejectLabel = args.GetEjectLabel();
        this.ejectLocation = args.GetEjectLocation();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {

        final EjectStorageDomainSpectraS3Request request
                = new EjectStorageDomainSpectraS3Request(id)
                .withBucketId(bucket).withEjectLabel(ejectLabel).withEjectLocation(ejectLocation);

        this.getClient().ejectStorageDomainSpectraS3(request);

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
