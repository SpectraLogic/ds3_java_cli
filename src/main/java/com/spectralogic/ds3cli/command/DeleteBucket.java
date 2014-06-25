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
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.DeleteBucketRequest;
import com.spectralogic.ds3client.commands.DeleteObjectRequest;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.models.Ds3Object;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;
import java.security.SignatureException;

public class DeleteBucket extends CliCommand {
    
    private String bucketName;
    private boolean clearBucket;
    public DeleteBucket(final Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The delete bucket command requires '-b' to be set.");
        }
        clearBucket = args.isClearBucket();
        return this;
    }

    @Override
    public String call() throws Exception {

        if (clearBucket) {
            return clearObjects();
        }
        else {
            return deleteBucket();
        }
    }

    private String deleteBucket() throws SignatureException {
        try {
            getClient().deleteBucket(new DeleteBucketRequest(bucketName));
        }
        catch (final IOException e) {
            return "Error: Request failed with the following error: " + e.getMessage();
        }
        return "Success: Deleted bucket '" + bucketName + "'.";
    }

    private String clearObjects() throws SignatureException {
        // TODO when the multi object delete command has been added to DS3
        // Get the list of objects from the bucket
        final Ds3Client client = getClient();
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(client);

        try {
            final Iterable<Contents> fileList = helper.listObjects(bucketName);
            for (final Contents content : fileList) {
                client.deleteObject(new DeleteObjectRequest(bucketName, content.getKey())).close();
            }

            getClient().deleteBucket(new DeleteBucketRequest(bucketName));

        } catch (final IOException e) {
            return "Error: Request failed with the following error: " + e.getMessage();
        }
        return "Success: Deleted " + bucketName + " and all the objects contained in it.";
    }
}
