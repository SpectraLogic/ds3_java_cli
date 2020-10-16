/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetObjectsOnTapeResult;
import com.spectralogic.ds3cli.views.cli.GetObjectsOnTapeView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.GetBlobsOnTapeSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetBlobsOnTapeSpectraS3Response;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.ArgumentFactory.ID;
import static com.spectralogic.ds3cli.ArgumentFactory.PREFIX;


public class GetObjectsOnTape extends CliCommand<GetObjectsOnTapeResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ID);
    private final static int maxKeys = 1000;

    // Barcode or tape ID
    private String tapeId;

    public GetObjectsOnTape() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);

        this.tapeId = args.getId();
        return this;
    }

    @Override
    public GetObjectsOnTapeResult call() throws CommandException, IOException {
        final List<BulkObject> objects = new ArrayList<>();
        int offset = 0;
        try {
            while (true) {
                final GetBlobsOnTapeSpectraS3Request request = new GetBlobsOnTapeSpectraS3Request(this.tapeId)
                        .withPageOffset(offset)
                        .withPageLength(maxKeys);
                final GetBlobsOnTapeSpectraS3Response response = getClient().getBlobsOnTapeSpectraS3(request);
                final List<BulkObject> objectList = response.getBulkObjectListResult().getObjects();
                if (objectList.isEmpty()) {
                    break;
                } else {
                    objects.addAll(response.getBulkObjectListResult().getObjects());
                }
                offset = objects.size();
            }
            return new GetObjectsOnTapeResult(this.tapeId, objects);
        } catch (final FailedRequestException e) {
            if (e.getStatusCode() == 404) {
                throw new CommandException("Unknown tape '" + this.tapeId + "'", e);
            }
            throw e;
        }
    }

    @Override
    public View<GetObjectsOnTapeResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetObjectsOnTapeView();
    }
}
    
