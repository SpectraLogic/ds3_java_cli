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

package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetDataPathBackendResult;
import com.spectralogic.ds3client.models.DataPathBackend;

import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class GetDataPathBackendView extends TableView<GetDataPathBackendResult> {

    private DataPathBackend dataPathBackend;

    @Override
    public String render(final GetDataPathBackendResult obj) {
        dataPathBackend = obj.getBackend();
        if( (dataPathBackend == null) || (null == dataPathBackend.getId()) ){
            return "No valid Data Path Backend on remote appliance";
        }

        initTable(ImmutableList.of("Activated", "Auto Timeout", "Auto Inspect", "Conflict Resolution", "ID",
                "Last Heartbeat", "Unavailable Media Policy", "Unavailable Pool Retry Mins", "Unavailable Partition Retry Mins"));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final List<String[]> contents = new ArrayList<>();
        final String[] attributesArray = new String[this.columnCount];
        attributesArray[0] = nullGuardToString(dataPathBackend.getActivated());
        attributesArray[1] = nullGuardToString(dataPathBackend.getAutoActivateTimeoutInMins());
        attributesArray[2] = nullGuardToString(dataPathBackend.getAutoInspect());
        attributesArray[3] = nullGuardToString(dataPathBackend.getDefaultImportConflictResolutionMode());
        attributesArray[4] = nullGuardToString(dataPathBackend.getId());
        attributesArray[5] = nullGuardToDate(dataPathBackend.getLastHeartbeat(),DATE_FORMAT);
        attributesArray[6] = nullGuardToString(dataPathBackend.getUnavailableMediaPolicy());
        attributesArray[7] = nullGuardToString(dataPathBackend.getUnavailablePoolMaxJobRetryInMins());
        attributesArray[8] = nullGuardToString(dataPathBackend.getUnavailableTapePartitionMaxJobRetryInMins());
        contents.add(attributesArray);
        return contents.toArray(new String[contents.size()][]);
    }

}
