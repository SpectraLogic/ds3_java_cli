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
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetStorageDomainsResult;
import com.spectralogic.ds3client.models.StorageDomain;
import com.spectralogic.ds3client.models.StorageDomainList;
import com.spectralogic.ds3client.utils.Guard;

import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.util.Guard.nullGuard;
import static com.spectralogic.ds3cli.util.Guard.nullGuardToString;

public class GetStorageDomainsView extends TableView<GetStorageDomainsResult> {
    private Iterable<StorageDomain> objectIterator;

    @Override
    public String render(final GetStorageDomainsResult obj) {
        final StorageDomainList storageDomainList = obj.getResult();
        if (storageDomainList == null || Guard.isNullOrEmpty(storageDomainList.getStorageDomains())) {
            return "No Storage Domains returned";
        }
        objectIterator = storageDomainList.getStorageDomains();

        initTable(ImmutableList.of("Name", "ID", "LTFS Naming", "Flags", "Write Optimization"));

        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final List<String[]> contents = new ArrayList<>();

        for (final StorageDomain domain : objectIterator) {
            final String[] domainArray = new String[this.columnCount];
            domainArray[0] = nullGuard(domain.getName());
            domainArray[1] = nullGuardToString(domain.getId());
            domainArray[2] = nullGuardToString(domain.getLtfsFileNaming());
            domainArray[3] = formatFlagList(domain);
            domainArray[4] = nullGuardToString(domain.getWriteOptimization());
            contents.add(domainArray);
        }
        return contents.toArray(new String[contents.size()][]);
    }

    private static String formatFlagList(final StorageDomain domain) {
        final List<String> flagsSet = new ArrayList<>();
        if(domain.getAutoEjectUponJobCancellation()) {
            flagsSet.add("Auto Eject upon Job Cancellation");
        }
        if(domain.getAutoEjectUponJobCompletion()) {
            flagsSet.add("Auto Eject upon Job Completion");
        }
        if(domain.getAutoEjectUponMediaFull()) {
            flagsSet.add("Auto Eject upon Media Full ("
                    + nullGuardToString(domain.getAutoEjectMediaFullThreshold())
                    + " bytes)");
        }
        if(domain.getSecureMediaAllocation()) {
            flagsSet.add("Secure Media Allocation");
        }
        return Joiner.on(",").join(flagsSet);
    }

}
