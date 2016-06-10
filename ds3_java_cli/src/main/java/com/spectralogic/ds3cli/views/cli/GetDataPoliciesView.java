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
import com.spectralogic.ds3cli.models.GetDataPoliciesResult;
import com.spectralogic.ds3client.models.DataPolicy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;

public class GetDataPoliciesView implements View<GetDataPoliciesResult> {

    @Override
    public String render(final GetDataPoliciesResult br) {
        if( (null == br.getObjIterator()) || !br.getObjIterator().hasNext()) {
            return "No Data Policies returned." ;
        }
        return ASCIITable.getInstance().getTable(getHeaders(), formatDataPoliciesList(br.getObjIterator()));
    }

    private String[][] formatDataPoliciesList(final Iterator<DataPolicy> iterator) {
        final List<String[]> contents = new ArrayList<>();

        while(iterator.hasNext()) {
            final DataPolicy content = iterator.next();
            final String[] arrayEntry = new String[12];
            arrayEntry[0] = nullGuardToString(content.getName());
            final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
            arrayEntry[1] = nullGuardToDate(content.getCreationDate(), DATE_FORMAT);
            arrayEntry[2] = nullGuardToString(content.getVersioning());
            arrayEntry[3] = nullGuardToString(content.getChecksumType());
            arrayEntry[4] = nullGuardToString(content.getEndToEndCrcRequired());
            arrayEntry[5] = nullGuardToString(content.getBlobbingEnabled());
            arrayEntry[6] = nullGuardToString(content.getDefaultBlobSize());
            arrayEntry[7] = nullGuardToString(content.getDefaultGetJobPriority());
            arrayEntry[8] = nullGuardToString(content.getDefaultPutJobPriority());
            arrayEntry[9] = nullGuardToString(content.getDefaultVerifyJobPriority());
            arrayEntry[10] = nullGuardToString(content.getId());
            arrayEntry[11] = nullGuardToString(content.getLtfsObjectNamingAllowed());
            contents.add(arrayEntry);
        }
        return contents.toArray(new String[contents.size()][]);
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Created", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Versioning", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Checksum Type", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("End-to-End CRC Required", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Blobbing Enabled", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Default Blob Size", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Default Get Job Priority", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Default Put Job Priority", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Default Verify Job Priority", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Id", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("LTFS Object Naming", ASCIITable.ALIGN_RIGHT)
        };
    }
}
