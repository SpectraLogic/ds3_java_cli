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

package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetUsersResult;
import com.spectralogic.ds3client.commands.spectrads3.GetUsersSpectraS3Response;
import com.spectralogic.ds3client.models.DataPolicy;
import com.spectralogic.ds3client.models.SpectraUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class GetUsersView implements View<GetUsersResult> {

    @Override
    public String render(final GetUsersResult usersResult) {
        if( (null == usersResult.getObjIterator()) || !usersResult.getObjIterator().hasNext()) {
            return "No Users returned." ;
        }
        return ASCIITable.getInstance().getTable(getHeaders(), formatUsersList(usersResult.getObjIterator()));
    }

    private String[][] formatUsersList(final Iterator<SpectraUser> iterator) {
        final List<String[]> contents = new ArrayList<>();

        while(iterator.hasNext()) {
            final SpectraUser content = iterator.next();
            final String[] arrayEntry = new String[5];
            arrayEntry[0] = nullGuardToString(content.getName());
            arrayEntry[1] = nullGuardToString(content.getSecretKey());
            arrayEntry[2] = nullGuardToString(content.getId());
            arrayEntry[3] = nullGuardToString(content.getDefaultDataPolicyId());
            arrayEntry[4] = nullGuardToString(content.getAuthId());

            contents.add(arrayEntry);
        }
        return contents.toArray(new String[contents.size()][]);
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Secret Key", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Id", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Default Data Policy Id", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Authorization Id", ASCIITable.ALIGN_RIGHT),
        };
    }
}
