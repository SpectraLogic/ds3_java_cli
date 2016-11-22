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
import com.spectralogic.ds3client.commands.DeleteObjectRequest;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.api.ArgumentFactory.BUCKET;
import static com.spectralogic.ds3cli.api.ArgumentFactory.OBJECT_NAME;

public class DeleteObject extends BaseCliCommand<DefaultResult> {
    
    private String bucketName;
    private String objectName;

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(BUCKET, OBJECT_NAME);

    public DeleteObject() {
    }

    @Override
    public BaseCliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);

        this.bucketName = args.getBucket();
        this.objectName = args.getObjectName();
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        getClient().deleteObject(new DeleteObjectRequest(bucketName, objectName));
        return new DefaultResult("Success: Deleted object '" + this.objectName + "' from bucket '" + this.bucketName + "'.");
    }
}
