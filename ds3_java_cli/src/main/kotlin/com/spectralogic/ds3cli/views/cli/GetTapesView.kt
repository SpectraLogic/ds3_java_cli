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

package com.spectralogic.ds3cli.views.cli

import com.google.common.collect.ImmutableList
import com.spectralogic.ds3cli.api.View
import com.spectralogic.ds3cli.models.GetTapesResult
import com.spectralogic.ds3client.models.Tape
import com.spectralogic.ds3client.utils.Guard

import com.spectralogic.ds3cli.tableview.*

class GetTapesView : View<GetTapesResult> {

    override fun render(obj: GetTapesResult): String {
        val result = obj.result
        if (result == null || Guard.isNullOrEmpty(result.tapes)) {
            return "You do not have any tapes"
        }

        val tableDescription = AsciiTableView<Tape>(
                ImmutableList.of(
                    TableStringEntry("Bar Code", Tape::getBarCode),
                    TableIdEntry("ID", Tape::getId),
                    TableStringEntry("State", {it.state.toString()}),
                    TableDateEntry("Last Modified", Tape::getLastModified),
                    TableLongEntry("Available Raw Capacity", Tape::getAvailableRawCapacity),
                    TableIdEntry("BucketID", Tape::getBucketId),
                    TableBooleanEntry("Assigned to Storage Domain", Tape::getAssignedToStorageDomain),
                    TableDateEntry("Ejection Date", Tape::getEjectDate),
                    TableStringEntry("Ejection Location", Tape::getEjectLocation),
                    TableStringEntry("Ejection Label", Tape::getEjectLabel),
                    TableDateEntry("Ejection Pending", Tape::getEjectPending)
                ),
                result.tapes)

        return tableDescription.print()
    }
}

