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

package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetBucketResult;
import com.spectralogic.ds3client.models.Contents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;

public class GetBucketView extends TableView<GetBucketResult> {

    private Iterator<Contents> objectIterator;

    @Override
    public String render(final GetBucketResult br) {
        if( (null == br.getObjIterator()) || !br.getObjIterator().hasNext()) {
            return "No objects were reported in bucket '" + br.getBucketName() + "'";
        }
        this.objectIterator = br.getObjIterator();
        initTable(ImmutableList.of("File Name", "Size", "Owner", "Last Modified", "ETag"));
        setTableDataAlignment(ImmutableList.of(ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT ,ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_RIGHT ));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final List<String[]> contents = new ArrayList<>();

        while(this.objectIterator.hasNext()) {
            final Contents content = this.objectIterator.next();
            final String[] arrayEntry = new String[5];
            arrayEntry[0] = nullGuard(content.getKey());
            arrayEntry[1] = nullGuard(Long.toString(content.getSize()));
            arrayEntry[2] = nullGuard(content.getOwner().getDisplayName());
            arrayEntry[3] = nullGuardToDate(content.getLastModified(), DATE_FORMAT);
            arrayEntry[4] = nullGuard(content.getETag());
            contents.add(arrayEntry);
        }

        return contents.toArray(new String[contents.size()][]);
    }

}
