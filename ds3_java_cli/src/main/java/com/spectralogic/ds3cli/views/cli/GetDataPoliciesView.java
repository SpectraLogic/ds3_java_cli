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
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.models.GetDataPoliciesResult;
import com.spectralogic.ds3client.models.DataPolicy;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;

public class GetDataPoliciesView extends TableView<GetDataPoliciesResult> {

    protected Iterable<DataPolicy> dataPolicies;

    @Override
    public String render(final GetDataPoliciesResult br) {
        if (null == br.getPolicyList() || br.getPolicyList().getDataPolicies() == null || Iterables.isEmpty(br.getPolicyList().getDataPolicies())) {
            return "No Data Policies returned." ;
        }
        this.dataPolicies = br.getPolicyList().getDataPolicies();

        initTable(ImmutableList.of("Name", "Created", "Versioning", "Checksum Type", "End-to-End CRC Required",
                "Blobbing Enabled", "Default Blob Size", "Default Get Job Priority","Default Put Job Priority",
                "Default Verify Job Priority", "Id", "LTFS Object Naming"));
        setTableDataAlignment(ImmutableList.of(ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_RIGHT , ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT,
                ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT));
        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {

        final ImmutableList.Builder<String[]> builder = ImmutableList.builder();

        for (final DataPolicy dataPolicy: dataPolicies) {
            final String[] arrayEntry = new String[this.columnCount];
            arrayEntry[0] = nullGuardToString(dataPolicy.getName());
            arrayEntry[1] = nullGuardToDate(dataPolicy.getCreationDate(), DATE_FORMAT);
            arrayEntry[2] = nullGuardToString(dataPolicy.getVersioning());
            arrayEntry[3] = nullGuardToString(dataPolicy.getChecksumType());
            arrayEntry[4] = nullGuardToString(dataPolicy.getEndToEndCrcRequired());
            arrayEntry[5] = nullGuardToString(dataPolicy.getBlobbingEnabled());
            arrayEntry[6] = nullGuardToString(dataPolicy.getDefaultBlobSize());
            arrayEntry[7] = nullGuardToString(dataPolicy.getDefaultGetJobPriority());
            arrayEntry[8] = nullGuardToString(dataPolicy.getDefaultPutJobPriority());
            arrayEntry[9] = nullGuardToString(dataPolicy.getDefaultVerifyJobPriority());
            arrayEntry[10] = nullGuardToString(dataPolicy.getId());
            arrayEntry[11] = nullGuardToString(dataPolicy.getLtfsObjectNamingAllowed());
            builder.add(arrayEntry);
        }

        final ImmutableList<String[]> dataPolicyStrings = builder.build();
        return dataPolicyStrings.toArray(new String[dataPolicyStrings.size()][]);
    }
}
