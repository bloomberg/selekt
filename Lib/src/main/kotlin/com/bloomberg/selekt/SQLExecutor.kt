/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt

private val EMPTY_ARRAY = emptyArray<Any?>()

@Suppress("Detekt.ComplexInterface", "Detekt.TooManyFunctions") // Reflects SQLite3.
internal interface SQLExecutor : BatchSQLExecutor {
    val isAutoCommit: Boolean

    /**
     * True if and only if the executor can execute only read-only SQL statements.
     *
     * @Link [SQLite's db_readonly](https://www.sqlite.org/c3ref/db_readonly.html)
     */
    val isReadOnly: Boolean

    fun checkpoint(name: String? = null, mode: SQLCheckpointMode = SQLCheckpointMode.PASSIVE)

    fun execute(sql: String, bindArgs: Array<*> = EMPTY_ARRAY): Int

    fun executeForBlob(
        name: String,
        table: String,
        column: String,
        row: Long
    ): SQLBlob

    fun executeForChangedRowCount(
        sql: String,
        bindArgs: Array<*> = EMPTY_ARRAY
    ): Int

    fun executeForCursorWindow(
        sql: String,
        bindArgs: Array<*>,
        window: ICursorWindow
    )

    fun executeForLastInsertedRowId(sql: String, bindArgs: Array<*> = EMPTY_ARRAY): Long

    fun executeForInt(sql: String, bindArgs: Array<*> = EMPTY_ARRAY): Int

    fun executeForLong(sql: String, bindArgs: Array<*> = EMPTY_ARRAY): Long

    fun executeForString(sql: String, bindArgs: Array<*> = EMPTY_ARRAY): String?

    fun executeWithRetry(sql: String): Int

    fun prepare(sql: String): SQLStatementInformation
}
