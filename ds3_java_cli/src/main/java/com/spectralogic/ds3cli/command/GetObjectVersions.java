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
import com.spectralogic.ds3cli.ArgumentFactory;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.GetObjectVersionsResult;
import com.spectralogic.ds3cli.util.pagination.GetObjectVersionsLoaderFactory;
import com.spectralogic.ds3cli.views.cli.GetObjectVersionsView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.models.S3Object;
import com.spectralogic.ds3client.utils.collections.LazyIterable;
import org.apache.commons.cli.Option;

public class GetObjectVersions extends CliCommand<GetObjectVersionsResult> {

    private static final ImmutableList<Option> requiredArgs = ImmutableList.of(ArgumentFactory.BUCKET, ArgumentFactory.OBJECT_NAME);

    private String bucketName;
    private String objectName;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(requiredArgs, EMPTY_LIST, args);

        this.bucketName = args.getBucket();
        this.objectName = args.getObjectName();

        return this;
    }

    @Override
    public GetObjectVersionsResult call() throws Exception {
        final LazyIterable<S3Object> versionsIterable = new LazyIterable<>(new GetObjectVersionsLoaderFactory(getClient(), bucketName, objectName));

        return new GetObjectVersionsResult(versionsIterable);
    }

    @Override
    public View<GetObjectVersionsResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }

        return new GetObjectVersionsView();
    }
}
