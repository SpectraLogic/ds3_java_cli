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

package com.spectralogic.ds3cli.tableview.table

import com.bethecoder.ascii_table.ASCIITable
import com.bethecoder.ascii_table.ASCIITableHeader
import com.bethecoder.ascii_table.spec.IASCIITableAware
import com.spectralogic.ds3cli.tableview.TableEntry
import com.spectralogic.ds3cli.tableview.TableEntryAlignment

class AsciiTable<in T>(columns: List<TableEntry<T>>,
                       data: Iterable<T>) : IASCIITableAware {

    private val mappedData: List<List<Any>>
    private val headers: List<ASCIITableHeader>

    init {
        mappedData = mapData(columns, data)
        headers = mapHeaders(columns)
    }

    companion object {
        private fun<T> mapHeaders(columns: List<TableEntry<T>>): List<ASCIITableHeader> {
            return columns.asSequence().map { ASCIITableHeader(it.columnName, alignment(it.alignment))}.toList()
        }

        private fun  alignment(alignment: TableEntryAlignment): Int {
            return if (alignment == TableEntryAlignment.LEFT) {
                ASCIITable.ALIGN_LEFT
            } else {
                ASCIITable.ALIGN_RIGHT
            }
        }

        private fun<T> mapData(columns: List<TableEntry<T>>, data: Iterable<T>): List<List<Any>> {

            return data.asSequence().map { dataEntry ->
                columns.map {
                    it.formatCell(dataEntry)
                }
            }.toList()
        }
    }

    override fun getHeaders(): List<ASCIITableHeader> {
        return headers
    }

    override fun formatData(p0: ASCIITableHeader?, p1: Int, p2: Int, p3: Any?): String? {
        return null
    }

    override fun getData(): List<List<Any>> {
        return mappedData
    }
}
