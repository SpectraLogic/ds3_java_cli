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

package com.spectralogic.ds3cli.views.csv;

import com.google.common.collect.ImmutableList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;

public class CsvOutput<E> {

    private final ImmutableList<String> headers;
    private final Iterable<E> csvContents;
    private final ContentFormatter<E> contentFormatter;

    public CsvOutput(final ImmutableList<String> headers, final Iterable<E> csvContents, final ContentFormatter<E> contentFormatter) {
        this.headers = headers;
        this.csvContents = csvContents;
        this.contentFormatter = contentFormatter;
    }

    public String toString() {
        try (final StringWriter writer = new StringWriter();
             final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {

            csvPrinter.printRecord(headers);
            for (final E content : csvContents) {
                csvPrinter.printRecord(contentFormatter.format(content));
            }
            return writer.toString();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to write csv", e);
        }
    }

    public interface ContentFormatter<E> {
        Iterable<String> format(final E content);
    }
}
