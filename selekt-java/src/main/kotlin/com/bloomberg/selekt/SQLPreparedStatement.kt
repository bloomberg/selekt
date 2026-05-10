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

import java.io.Closeable
import javax.annotation.concurrent.NotThreadSafe
import kotlin.math.min

private const val NANOS_PER_MILLI = 1_000_000L
private const val MAX_PAUSE_MILLIS = 100L

/**
 * @since 0.12.1
 */
@NotThreadSafe
@Suppress("Detekt.MethodOverloading", "Detekt.TooManyFunctions")
internal class SQLPreparedStatement(
    private val statement: StatementHandle,
    val sql: String,
    private val sqlite: SQLite,
    private val random: IRandom
) : Closeable {
    val columnCount = sqlite.columnCount(statement)

    val columnNames: Array<out String> = sqlite.run {
        Array(columnCount) { columnName(statement, it) }
    }

    /**
     * True if and only if the prepared statement makes no direct changes to the content of the database.
     *
     * Note that application-defined SQL functions or virtual tables might still change the database indirectly as a
     * side-effect.
     *
     * @see <a href="https://www.sqlite.org/c3ref/stmt_readonly.html">SQLite's stmt_readonly</a>
     */
    val isReadOnly = sqlite.statementReadOnly(statement) != 0

    val parameterCount = sqlite.bindParameterCount(statement)

    fun bind(index: Int, value: ByteArray) {
        sqlite.bindBlob(statement, index, value)
    }

    fun bind(name: String, value: ByteArray) {
        sqlite.bindBlob(statement, resolveParameterIndex(name), value)
    }

    fun bind(index: Int, value: Double) {
        sqlite.bindDouble(statement, index, value)
    }

    fun bind(name: String, value: Double) {
        sqlite.bindDouble(statement, resolveParameterIndex(name), value)
    }

    fun bind(index: Int, value: Int) {
        sqlite.bindInt(statement, index, value)
    }

    fun bind(name: String, value: Int) {
        sqlite.bindInt(statement, resolveParameterIndex(name), value)
    }

    fun bind(index: Int, value: Long) {
        sqlite.bindInt64(statement, index, value)
    }

    fun bind(name: String, value: Long) {
        sqlite.bindInt64(statement, resolveParameterIndex(name), value)
    }

    fun bind(index: Int, value: String) {
        sqlite.bindText(statement, index, value)
    }

    fun bind(name: String, value: String) {
        sqlite.bindText(statement, resolveParameterIndex(name), value)
    }

    fun bindNull(index: Int) {
        sqlite.bindNull(statement, index)
    }

    fun bindNull(name: String) {
        sqlite.bindNull(statement, resolveParameterIndex(name))
    }

    fun bindZeroBlob(index: Int, length: Int) {
        sqlite.bindZeroBlob(statement, index, length)
    }

    fun bindRow(args: Array<out Any?>) {
        sqlite.bindRow(statement, args)
    }

    fun bindRow(row: ParameterRow) {
        sqlite.bindRow(statement, row)
    }

    fun clearBindings() {
        sqlite.clearBindings(statement)
    }

    override fun close() {
        sqlite.finalize(statement)
    }

    fun columnBlob(index: Int) = sqlite.columnBlob(statement, index)

    fun columnDouble(index: Int) = sqlite.columnDouble(statement, index)

    fun columnInt(index: Int) = sqlite.columnInt(statement, index)

    fun columnLong(index: Int) = sqlite.columnInt64(statement, index)

    fun columnName(index: Int) = sqlite.columnName(statement, index)

    fun columnString(index: Int) = sqlite.columnText(statement, index)

    fun columnType(index: Int) = sqlite.columnType(statement, index)

    fun isBusy() = sqlite.statementBusy(statement) != 0

    fun reset() {
        sqlite.reset(statement)
    }

    fun resetAndClearBindings() {
        sqlite.resetAndClearBindings(statement)
    }

    fun step() = sqlite.step(statement)

    fun step(intervalMillis: Long): SQLCode {
        require(intervalMillis > -1L) { "Interval must be non-negative." }
        return stepUntil(deadlineNanos = System.nanoTime() + intervalMillis * NANOS_PER_MILLI)
    }

    private tailrec fun stepUntil(deadlineNanos: Long): SQLCode = when (sqlite.stepWithoutThrowing(statement)) {
        SQL_ROW -> SQL_ROW
        SQL_DONE -> SQL_DONE
        SQL_BUSY -> {
            pauseElseExpire(deadlineNanos)
            stepUntil(deadlineNanos)
        }
        else -> sqlite.throwSQLException(sqlite.databaseHandle(statement))
    }

    private fun pauseElseExpire(deadlineNanos: Long) {
        val remainingNanos = (deadlineNanos - System.nanoTime()).also {
            if (it < NANOS_PER_MILLI) {
                sqlite.throwSQLException(sqlite.databaseHandle(statement), "${sql.resolvedSqlStatementType()}")
            }
        }
        Thread.sleep(1L + min(MAX_PAUSE_MILLIS, remainingNanos / NANOS_PER_MILLI).nextRandom())
    }

    private fun Long.nextRandom() = random.nextLong(this)

    private fun resolveParameterIndex(name: String): Int = sqlite.bindParameterIndex(statement, name).also {
        require(it > 0) { "Named parameter '$name' not found in SQL statement." }
    }
}
