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
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetBucketResult;
import com.spectralogic.ds3cli.views.cli.GetBucketView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.ArgumentFactory.BUCKET;
import static com.spectralogic.ds3cli.ArgumentFactory.PREFIX;

public class GetBucket extends CliCommand<GetBucketResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(PREFIX);

    private String bucketName;
    private String prefix;

    public GetBucket() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, optionalArgs, args);

        this.bucketName = args.getBucket();
        this.prefix = args.getPrefix();
        return this;
    }

    @Override
    public GetBucketResult call() throws Exception {

        try {
            final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(getClient());

            final Iterable<Contents> objects;

            if (this.prefix == null) {
                objects = helper.listObjects(bucketName);
            }
            else {
                objects = helper.listObjects(bucketName, this.prefix);
            }

            return new GetBucketResult(bucketName, objects);
        }
        catch(final FailedRequestException e) {
            if(e.getStatusCode() == 404) {
                throw new CommandException("Error: Unknown bucket.", e);
            }
            throw e;
        }
    }

    @Override
    public View<GetBucketResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetBucketView();
    }
}
