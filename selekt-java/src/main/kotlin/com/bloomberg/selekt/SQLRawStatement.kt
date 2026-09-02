/*
 * Copyright 2026 Bloomberg Finance L.P.
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

/**
 * @since 1.1.0
 */
@Suppress("Detekt.ComplexInterface", "Detekt.TooManyFunctions")
interface ISQLRawStatement : Closeable {
    val columnCount: Int

    val parameterCount: Int

    val isReadOnly: Boolean

    fun bindBlob(index: Int, value: ByteArray)

    fun bindDouble(index: Int, value: Double)

    fun bindInt(index: Int, value: Int)

    fun bindLong(index: Int, value: Long)

    fun bindString(index: Int, value: String)

    fun bindNull(index: Int)

    fun columnBlob(index: Int): ByteArray?

    fun columnDouble(index: Int): Double

    fun columnInt(index: Int): Int

    fun columnLong(index: Int): Long

    fun columnString(index: Int): String?

    fun columnName(index: Int): String

    fun columnNames(): Array<out String>

    fun columnType(index: Int): Int

    fun isNull(index: Int): Boolean

    /**
     * @return true if there is a row of data ready to be read, false if the statement is done executing.
     */
    fun step(): Boolean

    fun reset()

    fun clearBindings()
}

/**
 * @since 1.1.0
 */
@NotThreadSafe
internal class SQLRawStatement private constructor(
    private val session: ThreadLocalSession,
    private val sql: String,
    private val statementType: SQLStatementType,
    private val executor: CloseableSQLExecutor?,
    private val preparedStatement: SQLPreparedStatement?
) : ISQLRawStatement {
    private var closed = false

    companion object {
        fun prepare(session: ThreadLocalSession, sql: String): SQLRawStatement {
            val statementType = sql.resolvedSqlStatementType()
            return if (statementType.isTransactional) {
                SQLRawStatement(session, sql, statementType, null, null)
            } else {
                val (executor, preparedStatement) = session().prepareRawStatement(sql, statementType.isPredictedWrite)
                SQLRawStatement(session, sql, statementType, executor, preparedStatement)
            }
        }
    }

    override val columnCount: Int
        get() = preparedStatement?.columnCount ?: 0

    override val parameterCount: Int
        get() = preparedStatement?.parameterCount ?: 0

    override val isReadOnly: Boolean
        get() = preparedStatement?.isReadOnly ?: false

    override fun bindBlob(index: Int, value: ByteArray) = statement().bind(index, value)

    override fun bindDouble(index: Int, value: Double) = statement().bind(index, value)

    override fun bindInt(index: Int, value: Int) = statement().bind(index, value)

    override fun bindLong(index: Int, value: Long) = statement().bind(index, value)

    override fun bindString(index: Int, value: String) = statement().bind(index, value)

    override fun bindNull(index: Int) = statement().bindNull(index)

    override fun columnBlob(index: Int) = statement().columnBlob(index)

    override fun columnDouble(index: Int) = statement().columnDouble(index)

    override fun columnInt(index: Int) = statement().columnInt(index)

    override fun columnLong(index: Int) = statement().columnLong(index)

    override fun columnString(index: Int) = statement().columnString(index)

    override fun columnName(index: Int) = statement().columnName(index)

    override fun columnNames(): Array<out String> = preparedStatement?.columnNames ?: emptyArray()

    override fun columnType(index: Int) = statement().columnType(index)

    override fun isNull(index: Int) = SQL_NULL == statement().columnType(index)

    override fun step(): Boolean = if (statementType.isTransactional) {
        session().execute(statementType.isPredictedWrite, sql, statementType, false) { false }
    } else {
        SQL_ROW == statement().step()
    }

    override fun reset() {
        preparedStatement?.reset()
    }

    override fun clearBindings() {
        preparedStatement?.clearBindings()
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        if (executor != null && preparedStatement != null) {
            session().releaseRawStatement(executor, preparedStatement)
        }
    }

    private fun statement() = preparedStatement ?: error("Statement '$sql' does not support this operation.")
}
