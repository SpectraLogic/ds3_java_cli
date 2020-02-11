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

package com.spectralogic.ds3cli.views.csv

import com.spectralogic.ds3cli.RawView
import com.spectralogic.ds3cli.models.GetBucketResult
import com.spectralogic.ds3client.models.Contents
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.PrintStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GetBucketView : RawView<GetBucketResult> {
    private companion object {
        private val HEADERS = listOf("name", "size", "lastModified", "isLatest", "versionId", "owner", "etag")
        private val UTC = ZoneId.of("UTC")
    }
    override fun renderToStream(out: Appendable, result: GetBucketResult) {
        CSVPrinter(out, CSVFormat.EXCEL).use { printer ->
            printer.printRecord(HEADERS)
            result.result.asSequence()
                    .map(this::mapToList)
                    .chunked(100)
                    .forEach(printer::printRecords)
        }
    }

    private fun mapToList(content: Contents) =
            listOf(
                    content.key,
                    content.size.toString(),
                    content.lastModified.toInstant().atZone(UTC).format(DateTimeFormatter.ISO_DATE_TIME),
                    content.isLatest.toString(),
                    content.versionId.toString(),
                    content.owner.displayName,
                    content.eTag
            )
}
