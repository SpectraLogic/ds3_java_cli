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
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.Result;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static java.lang.System.out;

/**
 * Replace TableView base class to provide CSV output
 * @param <T> Result Type
 */
public abstract class CsvView<T extends Result> implements View<T> {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(CsvView.class);

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

    protected String renderTable()  {
        final Appendable outs = new StringWriter();
        try {
            final CSVPrinter csv = new CSVPrinter(outs, CSVFormat.EXCEL);
            csv.printRecord(getHeaders());
            final String[][] body = formatTableContents();
            for (final String[] line : body) {
                csv.printRecord(line);
            }
            csv.flush();
            csv.close();
        } catch (IOException e) {
            LOG.warn("Failed to create CSV output", e);
            return "ERROR: Failed to create CSV output";
        }
        return outs.toString();
    }
}
