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
import com.spectralogic.ds3cli.models.GetUsersResult;
import com.spectralogic.ds3client.models.SpectraUser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.spectralogic.ds3cli.util.Guard.nullGuardToString;

public class GetUsersView extends TableView<GetUsersResult> {

    private Iterator<SpectraUser> objectIterator;

    @Override
    public String render(final GetUsersResult usersResult) {
        if (null == usersResult.getObjIterator() || !usersResult.getObjIterator().hasNext()) {
            return "No Users returned." ;
        }

        this.objectIterator = usersResult.getObjIterator();

        initTable(ImmutableList.of("Name", "Secret Key", "Id", "Default Data Policy Id", "Authorization Id"));
        setTableDataAlignment(ImmutableList.of(ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT));
        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final List<String[]> contents = new ArrayList<>();

        while(this.objectIterator.hasNext()) {
            final SpectraUser content = this.objectIterator.next();
            final String[] arrayEntry = new String[this.columnCount];
            arrayEntry[0] = nullGuardToString(content.getName());
            arrayEntry[1] = nullGuardToString(content.getSecretKey());
            arrayEntry[2] = nullGuardToString(content.getId());
            arrayEntry[3] = nullGuardToString(content.getDefaultDataPolicyId());
            arrayEntry[4] = nullGuardToString(content.getAuthId());

            contents.add(arrayEntry);
        }
        return contents.toArray(new String[contents.size()][]);
    }
}
