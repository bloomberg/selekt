/*
 * Copyright 2020 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt

internal interface BatchSQLExecutor {
    /**
     * @param sql SQL statement with ? placeholders for bind parameters.
     * @param bindArgs arrays of arguments for binding to the statement; all sub-arrays must have
     *   the same length and the same types at corresponding indices (e.g., String, Int, ByteArray, null).
     * @return the number of rows affected.
     */
    fun executeBatchForChangedRowCount(sql: String, bindArgs: Array<out Array<*>>): Int

    fun executeBatchForChangedRowCount(sql: String, bindArgs: Sequence<Array<*>>): Int

    fun executeBatchForChangedRowCount(sql: String, bindArgs: Iterable<Array<*>>): Int =
        executeBatchForChangedRowCount(sql, bindArgs.asSequence())
}
