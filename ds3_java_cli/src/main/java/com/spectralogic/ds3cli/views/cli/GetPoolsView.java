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
import com.spectralogic.ds3cli.models.GetPoolsResult;
import com.spectralogic.ds3client.models.Pool;

import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.util.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.util.Guard.*;

public class GetPoolsView extends TableView<GetPoolsResult> {

        private Iterable<Pool> poolIterator;

        @Override
        public String render(final GetPoolsResult poolsResult) {
            if (null == poolsResult.getResult().getPools() || Iterables.isEmpty(poolsResult.getResult().getPools())) {
                return "No Pools returned." ;
            }

            this.poolIterator = poolsResult.getResult().getPools();

            initTable(ImmutableList.of("Pool","ID", "Type", "Capacity", "Bucket Id", "Partition Id", "Assigned to Storage Domain", "Last Modified", "Last Verification"));
            return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
        }

        protected String[][] formatTableContents() {
            final List<String[]> contents = new ArrayList<>();

            for (final Pool content : this.poolIterator) {
                final String[] arrayEntry = new String[this.columnCount];
                arrayEntry[0] = nullGuardToString(content.getName());
                arrayEntry[1] = nullGuardToString(content.getId());
                arrayEntry[2] = content.getType() == null ? "N/A" : nullGuard(content.getType().name());
                arrayEntry[3] = nullGuardToString(content.getAvailableCapacity());
                arrayEntry[4] = nullGuardToString(content.getBucketId());
                arrayEntry[5] = nullGuardToString(content.getPartitionId());
                arrayEntry[6] = nullGuardToString(content.getAssignedToStorageDomain());
                arrayEntry[7] = nullGuardFromDate(content.getLastModified(), DATE_FORMAT);
                arrayEntry[8] = nullGuardFromDate(content.getLastVerified(), DATE_FORMAT);
                contents.add(arrayEntry);
            }
            return contents.toArray(new String[contents.size()][]);
        }

}
