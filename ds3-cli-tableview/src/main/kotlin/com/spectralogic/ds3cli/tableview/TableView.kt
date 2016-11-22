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

package com.spectralogic.ds3cli.tableview

import com.bethecoder.ascii_table.ASCIITable
import com.spectralogic.ds3cli.utils.Constants.DATE_FORMAT
import com.spectralogic.ds3cli.utils.Guard.*
import com.spectralogic.ds3cli.tableview.table.AsciiTable
import java.util.*

interface TableView {
    fun print(): String
}

class AsciiTableView<in T>(columns: List<TableEntry<T>>,
                           data: Iterable<T>) : TableView {

    private val backingTable: AsciiTable<T>

    init {
        backingTable = AsciiTable(columns, data)
    }

    override fun print(): String {

        return ASCIITable.getInstance().getTable(backingTable) ?: ""
    }
}

class CsvTableView<in T>(columns: List<TableEntry<T>>, data: Iterable<T>) : TableView {
    override fun print(): String {
        throw NotImplementedError("This has not been implemented yet")
    }
}

enum class TableEntryAlignment {
    LEFT, RIGHT
}

interface TableEntry<in T> {
    val alignment: TableEntryAlignment
    val columnName: String
    fun formatCell(entry: T): String
}

abstract class TableLeftAlignedEntry<in T> : TableEntry<T> {
    override val alignment: TableEntryAlignment = TableEntryAlignment.LEFT
}

class TableIdEntry<in T>(override val columnName: String,
                         val transform: (T) -> UUID?) : TableLeftAlignedEntry<T>() {

    override fun formatCell(entry: T): String {
        return nullGuardToString(transform.invoke(entry))
    }
}

class TableStringEntry<in T>(override val columnName: String,
                             val transform: (T) -> String?) : TableLeftAlignedEntry<T>() {

    override fun formatCell(entry: T): String {
        return nullGuard(transform.invoke(entry))
    }
}

class TableBooleanEntry<in T>(override val columnName: String,
                           val transform: (T) -> Boolean?) : TableLeftAlignedEntry<T>() {

    override fun formatCell(entry: T): String {
        return nullGuardToString(transform.invoke(entry))
    }
}

class TableLongEntry<in T>(override val columnName: String,
                           val transform: (T) -> Long?) : TableLeftAlignedEntry<T>() {

    override fun formatCell(entry: T): String {
        return nullGuardToString(transform.invoke(entry))
    }
}

class TableDateEntry<in T>(override val columnName: String,
                           val transform: (T) -> Date?) : TableLeftAlignedEntry<T>() {

    override fun formatCell(entry: T): String {
        return nullGuardFromDate(transform.invoke(entry), DATE_FORMAT)
    }
}
