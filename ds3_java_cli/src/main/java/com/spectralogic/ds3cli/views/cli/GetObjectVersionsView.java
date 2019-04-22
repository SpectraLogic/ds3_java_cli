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
 
package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.models.GetObjectVersionsResult;
import com.spectralogic.ds3client.models.S3Object;

import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Guard.nullGuard;
import static com.spectralogic.ds3cli.util.Guard.nullGuardFromDate;

public class GetObjectVersionsView extends TableView<GetObjectVersionsResult> {

    private Iterable<S3Object> versions;

    @Override
    public String render(final GetObjectVersionsResult result) {
        versions = result.getResult();

        if (versions == null || Iterables.isEmpty(versions)) {
            return "Could not find any versions.";
        }

        initTable(ImmutableList.of("Bucket Id", "Name", "Creation Date", "Latest", "Version Id"));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    @Override
    protected String[][] formatTableContents() {
       final List<String[]> contents = new ArrayList<>();

        for (final S3Object version : versions) {
            final String[] arrayEntry = new String[this.columnCount];
            arrayEntry[0] = nullGuard(version.getId());
            arrayEntry[1] = nullGuard(version.getName());
            arrayEntry[2] = nullGuardFromDate(version.getCreationDate(), DATE_FORMAT);
            arrayEntry[3] = nullGuard(version.getLatest());
            arrayEntry[4] = nullGuard(version.getBucketId());
            contents.add(arrayEntry);
        }

        return contents.toArray(new String[contents.size()][]);
    }
}
