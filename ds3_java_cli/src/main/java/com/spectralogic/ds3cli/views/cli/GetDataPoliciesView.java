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
import com.spectralogic.ds3cli.models.GetDataPoliciesResult;
import com.spectralogic.ds3client.models.DataPolicy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;

public class GetDataPoliciesView extends TableView<GetDataPoliciesResult> {

    protected Iterator<DataPolicy> objectIterator;

    @Override
    public String render(final GetDataPoliciesResult br) {
        if( (null == br.getObjIterator()) || !br.getObjIterator().hasNext()) {
            return "No Data Policies returned." ;
        }
        this.objectIterator = br.getObjIterator();

        initTable(ImmutableList.of("Name", "Created", "Versioning", "Checksum Type", "End-to-End CRC Required",
                "Blobbing Enabled", "Default Blob Size", "Default Get Job Priority","Default Put Job Priority",
                "Default Verify Job Priority", "Id", "LTFS Object Naming"));
        setTableDataAlignment(ImmutableList.of(ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_RIGHT , ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT,
                ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT));
        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final List<String[]> contents = new ArrayList<>();

        while(this.objectIterator.hasNext()) {
            final DataPolicy content = this.objectIterator.next();
            final String[] arrayEntry = new String[this.columnCount];
            arrayEntry[0] = nullGuardToString(content.getName());
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
}
