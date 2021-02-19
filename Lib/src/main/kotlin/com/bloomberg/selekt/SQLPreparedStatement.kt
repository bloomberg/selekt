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

import java.io.Closeable
import javax.annotation.concurrent.NotThreadSafe
import kotlin.math.min

private const val NANOS_PER_MILLI = 1_000_000L
private const val MAX_PAUSE_MILLIS = 100L

@NotThreadSafe
@Suppress("Detekt.TooManyFunctions")
internal class SQLPreparedStatement(
    private var pointer: Pointer,
    private var rawSql: String,
    private val sqlite: SQLite,
    private val random: IRandom
) : Closeable {
    companion object {
        fun recycle(
            preparedStatement: SQLPreparedStatement,
            pointer: Long,
            sql: String
        ) = preparedStatement.apply {
            this.pointer = pointer
            this.rawSql = sql
            this.parameterCount = sqlite.bindParameterCount(pointer)
            this.isReadOnly = sqlite.statementReadOnly(pointer) != 0
        }
    }

    val columnCount: Int
        get() = sqlite.columnCount(pointer)

    val columnNames: Array<out String>
        get() = sqlite.run {
            Array(columnCount) { columnName(pointer, it) }
        }

    /**
     * True if and only if the prepared statement makes no direct changes to the content of the database.
     *
     * Note that application-defined SQL functions or virtual tables might still change the database indirectly as a
     * side-effect.
     *
     * @Link [SQLite's stmt_readonly](https://www.sqlite.org/c3ref/stmt_readonly.html)
     */
    var isReadOnly = sqlite.statementReadOnly(pointer) != 0
        private set

    var parameterCount = sqlite.bindParameterCount(pointer)
        private set

    val sql: String
        get() = rawSql

    fun bind(index: Int, value: ByteArray) {
        sqlite.bindBlob(pointer, index, value)
    }

    fun bind(index: Int, value: Double) {
        sqlite.bindDouble(pointer, index, value)
    }

    fun bind(index: Int, value: Int) {
        sqlite.bindInt(pointer, index, value)
    }

    fun bind(index: Int, value: Long) {
        sqlite.bindInt64(pointer, index, value)
    }

    fun bind(index: Int, value: String) {
        sqlite.bindText(pointer, index, value)
    }

    fun bindNull(index: Int) {
        sqlite.bindNull(pointer, index)
    }

    fun bindZeroBlob(index: Int, length: Int) {
        sqlite.bindZeroBlob(pointer, index, length)
    }

    fun clearBindings() {
        sqlite.clearBindings(pointer)
    }

    override fun close() {
        sqlite.finalize(pointer)
        pointer = NULL
        rawSql = ""
    }

    fun columnBlob(index: Int) = sqlite.columnBlob(pointer, index)

    fun columnDouble(index: Int) = sqlite.columnDouble(pointer, index)

    fun columnInt(index: Int) = sqlite.columnInt(pointer, index)

    fun columnLong(index: Int) = sqlite.columnInt64(pointer, index)

    fun columnName(index: Int) = sqlite.columnName(pointer, index)

    fun columnString(index: Int) = sqlite.columnText(pointer, index)

    fun columnType(index: Int) = sqlite.columnType(pointer, index)

    fun isBusy() = sqlite.statementBusy(pointer) != 0

    fun reset() {
        sqlite.reset(pointer)
    }

    fun step() = sqlite.step(pointer)

    fun step(intervalMillis: Long): SQLCode {
        require(intervalMillis > -1L) { "Interval must be non-negative." }
        return stepUntil(intervalMillis + System.nanoTime() / NANOS_PER_MILLI)
    }

    private tailrec fun stepUntil(expireAtMillis: Long): SQLCode = when (sqlite.stepWithoutThrowing(pointer)) {
        SQL_ROW -> SQL_ROW
        SQL_DONE -> SQL_DONE
        SQL_BUSY -> {
            pauseElseExpire(expireAtMillis)
            sqlite.reset(pointer)
            stepUntil(expireAtMillis)
        }
        else -> sqlite.throwSQLException(sqlite.databaseHandle(pointer))
    }

    private fun pauseElseExpire(expireAtMillis: Long) {
        val nowNanos = System.nanoTime()
        val remainderMillis = (expireAtMillis - nowNanos / NANOS_PER_MILLI).also {
            if (it < 1L) {
                sqlite.throwSQLException(sqlite.databaseHandle(pointer), "${sql.resolvedSqlStatementType()}")
            }
        }
        Thread.sleep(1L + min(MAX_PAUSE_MILLIS, remainderMillis).nextRandom())
    }

    private fun Long.nextRandom() = random.nextLong(this)
}
