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
import com.spectralogic.ds3cli.models.GetCacheStateResult;
import com.spectralogic.ds3client.models.CacheFilesystem;
import com.spectralogic.ds3client.models.CacheFilesystemInformation;
import com.spectralogic.ds3client.utils.Guard;

import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.*;

public class GetCacheStateView extends TableView<GetCacheStateResult> {

    protected List<CacheFilesystemInformation> filesystemList;

    @Override
    public String render(final GetCacheStateResult obj) {
        this.filesystemList = obj.getResult();
        if (Guard.isNullOrEmpty(this.filesystemList)){
            return "No Cache Filesystems reported.";
        }

        initTable(ImmutableList.of("Path", "Available Capacity", "Used Capacity", "Unavailable Capacity", "Total Capacity", "Max Capacity",
                "Auto Reclaim Threshold", "Burst Threshold", "Max Utilization", "ID", "Node ID"));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final String [][] formatArray = new String[this.filesystemList.size()][];
        int i = 0;
        for(final CacheFilesystemInformation cacheFilesystemInfo : this.filesystemList) {
            final CacheFilesystem cacheFilesystem = cacheFilesystemInfo.getCacheFilesystem();
            final String [] bucketArray = new String[this.columnCount];
            bucketArray[0] = nullGuard(cacheFilesystem.getPath());
            bucketArray[1] = nullGuardToString(cacheFilesystemInfo.getAvailableCapacityInBytes());
            bucketArray[2] = nullGuardToString(cacheFilesystemInfo.getUsedCapacityInBytes());
            bucketArray[3] = nullGuardToString(cacheFilesystemInfo.getUnavailableCapacityInBytes());
            bucketArray[4] = nullGuardToString(cacheFilesystemInfo.getTotalCapacityInBytes());
            bucketArray[5] = nullGuardToString(cacheFilesystem.getMaxCapacityInBytes());
            bucketArray[6] = nullGuardToString(cacheFilesystem.getAutoReclaimInitiateThreshold());
            bucketArray[7] = nullGuardToString(cacheFilesystem.getBurstThreshold());
            bucketArray[8] = nullGuardToString(cacheFilesystem.getMaxPercentUtilizationOfFilesystem());
            bucketArray[9] = nullGuardToString(cacheFilesystem.getId());
            bucketArray[10] = nullGuardToString(cacheFilesystem.getNodeId());
            formatArray[i++] = bucketArray;
        }
        return formatArray;
    }
}

