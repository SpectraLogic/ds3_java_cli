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

package com.spectralogic.ds3cli.views.csv;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.Result;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Replace TableView base class to provide CSV output
 * @param <T> Result Type
 */
public abstract class CsvView<T extends Result> implements View<T> {

    protected final static String LINE_SEPARATOR = "\n";
    protected final static String CELL_SEPARATOR = "\",\"";
    protected final static char CELL_QUOTE = '"';

    protected ImmutableList<String> header;
    protected int columnCount;

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    public abstract String render(final T result);

    public void initTable(final ImmutableList<String> columnHeads) {
        this.columnCount = columnHeads.size();
        this.header = columnHeads;
    }

    public void setTableDataAlignment(final ImmutableList<Integer> columnAlign) {
        // pass -- significant in cli
    }

    protected abstract String[][] formatTableContents();

    protected ImmutableList<String> getHeaders() {
        return this.header;
    }

    protected String renderTable() {
        final StringBuilder csvOut = new StringBuilder();
        csvOut.append(CELL_QUOTE);
        csvOut.append(Joiner.on(CELL_SEPARATOR).join(getHeaders()));
        csvOut.append(CELL_QUOTE);
        csvOut.append(LINE_SEPARATOR);
        final String[][] body = formatTableContents();
        for(final String[] line : body) {
            csvOut.append(CELL_QUOTE);
            csvOut.append(Joiner.on(CELL_SEPARATOR).join(line));
            csvOut.append(CELL_QUOTE);
            csvOut.append(LINE_SEPARATOR);
        }
        return csvOut.toString();
    }

}
