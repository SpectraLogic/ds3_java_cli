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
import static com.spectralogic.ds3cli.ArgumentFactory.*;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.DeleteBucketRequest;
import com.spectralogic.ds3client.commands.DeleteObjectsRequest;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DeleteBucket extends CliCommand<DefaultResult> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteBucket.class);

    private String bucketName;
    private boolean force;

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET);
    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(FORCE);

    public DeleteBucket() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        addOptionalArguments(optionalArgs, args);
        args.parseCommandLine();

        this.bucketName = args.getBucket();
        this.force = args.isForce();
        this.viewType = args.getOutputFormat();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {

        if (force) {
            return new DefaultResult(clearObjects());
        } else {
            return new DefaultResult(deleteBucket());
        }
    }

    private String deleteBucket() throws CommandException, IOException {
        try {
            getClient().deleteBucket(new DeleteBucketRequest(bucketName));
        } catch (final FailedRequestException e) {
            if (e.getStatusCode() == 409) { //BUCKET_NOT_EMPTY
                throw new CommandException("Error: Tried to delete a non-empty bucket without the force delete objects flag.\nUse --force to delete all objects in the bucket");
            }
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }

        return "Success: Deleted bucket '" + bucketName + "'.";
    }

    private String clearObjects() throws CommandException {
        // TODO when the multi object delete command has been added to DS3
        // Get the list of objects from the bucket
        LOG.debug("Deleting objects in bucket first");
        final Ds3Client client = getClient();
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(client);

        try {
            final Iterable<Contents> fileList = helper.listObjects(bucketName);
            client.deleteObjects(new DeleteObjectsRequest(bucketName, fileList));

            LOG.debug("Deleting bucket");
            getClient().deleteBucket(new DeleteBucketRequest(bucketName));

        } catch (final IOException e) {
            throw new CommandException("Error: Request failed with the following error: " + e.getMessage(), e);
        }

        return "Success: Deleted " + bucketName + " and all the objects contained in it.";
    }
}
