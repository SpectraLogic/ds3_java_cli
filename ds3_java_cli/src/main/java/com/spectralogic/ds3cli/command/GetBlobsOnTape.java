/*
 * ******************************************************************************
 *   Copyright 2016 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.models.GetBlobsOnTapeResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.SSLSetupException;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;
import java.security.SignatureException;
import java.util.UUID;

public class GetBlobsOnTape extends CliCommand<GetBlobsOnTapeResult> {

    // Barcode or tape ID
    private String tapeId;
    private String barcode;

    public GetBlobsOnTape(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.tapeId = args.getId();
        if (Guard.isStringNullOrEmpty(this.tapeId)) {
            throw new MissingOptionException("The get_blobs_on_tape command requires '-i' to be set with the tape Id or barcode");
        }
        // pass in a string, could be UUID or Barcode
        try {
            UUID testId = (UUID.fromString(this.tapeId));
            this.barcode = "";
        } catch (IllegalArgumentException ex) {
            // not a UUID
            this.barcode = this.tapeId;
            this.tapeId = "";
        }
        return this;
    }

    @Override
    public GetBlobsOnTapeResult  call() throws IOException, SignatureException, SSLSetupException, CommandException {
        try {
            // if no id, try to find by barcode
            if (Guard.isStringNullOrEmpty(this.tapeId)) {
                final GetTapesSpectraS3Response tapeResponse
                        = getClient().getTapesSpectraS3(new GetTapesSpectraS3Request().withBarCode(this.barcode));

                if (Guard.isNullOrEmpty(tapeResponse.getTapeListResult().getTapes())) {
                    throw new CommandException("No tapes found for id or barcode: " + this.barcode) ;
                }
                // first tape matching barcode (hope there are not two sets Id
                this.tapeId = tapeResponse.getTapeListResult().getTapes().get(0).getId().toString();
            }

            if (Guard.isStringNullOrEmpty(this.tapeId)) {
                throw new CommandException("No valid id or barcode");
            }

            // get the blobs.
            final GetBlobsOnTapeSpectraS3Response response
                        = getClient().getBlobsOnTapeSpectraS3(new GetBlobsOnTapeSpectraS3Request(null, this.tapeId));

            return new GetBlobsOnTapeResult(this.tapeId, this.barcode, response.getBulkObjectListResult().getObjects().iterator());
        } catch (final FailedRequestException e) {
            if(e.getStatusCode() == 500) {
                throw new CommandException("Error: Cannot communicate with the remote DS3 appliance.", e);
            }
            else if(e.getStatusCode() == 404) {
                throw new CommandException("Unknown tape: " + this.tapeId + this.barcode, e);
            }
            else {
                throw new CommandException("Encountered an unknown error of ("+ e.getStatusCode() +") while accessing the remote DS3 appliance.", e);
            }
        }
    }
}
