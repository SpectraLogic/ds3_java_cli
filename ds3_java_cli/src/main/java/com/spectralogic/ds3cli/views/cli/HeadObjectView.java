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
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.HeadObjectResult;
import com.spectralogic.ds3client.networking.Metadata;

import java.util.List;
import java.util.Set;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class HeadObjectView extends TableView<HeadObjectResult> {

    protected Metadata metadata;

    @Override
    public String render(final HeadObjectResult obj) {

        switch (obj.getStatus()) {
            case DOESNTEXIST: return "The object does not exist";
            case UNKNOWN: return "Authentication failed";
            default: {
                this.metadata = obj.getMetadata();
                if (this.metadata.keys().size() == 0) {
                    return "The object exists and does not have any metadata";
                } else {
                    initTable(ImmutableList.of("MetaData Key", "MetaData Values"));

                    return "The object exists and contains the following metadata:\n"
                            + ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
                }
            }
        }
    }

    protected String[][] formatTableContents() {
        final Set<String> keys = metadata.keys();
        final String [][] formatArray = new String[keys.size()][];
        int arrayIndex = 0;

        for (final String key : keys) {
            final List<String> values = metadata.get(key);
            final String [] bucketArray = new String[this.columnCount];
            bucketArray[0] = nullGuard(key);
            bucketArray[1] = nullGuard(Joiner.on(", ").join(values));
            formatArray[arrayIndex++] = bucketArray;
        }
        return formatArray;
    }
}
