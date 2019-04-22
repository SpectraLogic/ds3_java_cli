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
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.Result;

public abstract class TableView<T extends Result> implements View<T> {

    protected ASCIITableHeader[] header;
    protected int columnCount;

    public abstract String render(final T result);

    public void initTable(final ImmutableList<String> columnHeads) {
        this.columnCount = columnHeads.size();

        // create the header
        this.header = new ASCIITableHeader[this.columnCount];
        for (int i = 0; i < this.columnCount; i++) {
            header[i] = new ASCIITableHeader(columnHeads.get(i), ASCIITable.ALIGN_LEFT);
        }
    }

    public void setTableDataAlignment(final ImmutableList<Integer> columnAlign) {
        // set alignment
        if (this.header != null && this.columnCount > 0) {
            for (int i = 0; i < this.columnCount; i++) {
                this.header[i].setDataAlign(columnAlign.get(i).shortValue());
            }
        }
    }

    protected String renderTable() {
        return ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected abstract String[][] formatTableContents();

    protected ASCIITableHeader[] getHeaders() {
        return this.header;
    }
}
