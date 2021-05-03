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

import com.bloomberg.selekt.commons.forEachByPosition
import com.bloomberg.selekt.commons.joinTo
import org.intellij.lang.annotations.Language
import java.lang.StringBuilder
import javax.annotation.concurrent.NotThreadSafe

private val EMPTY_ARRAY = emptyArray<Any?>()
private val EMPTY_SQL_STATEMENT_INFORMATION = SQLStatementInformation(false, 0, emptyArray())

@NotThreadSafe
internal class SQLQuery internal constructor(
    private val session: ThreadLocalSession,
    private val sql: String,
    private val statementType: SQLStatementType,
    private val bindArgs: Array<Any?>
) : IQuery {
    companion object {
        fun create(
            session: ThreadLocalSession,
            @Language("RoomSql") sql: String,
            statementType: SQLStatementType,
            argsCount: Int
        ) = SQLQuery(session, sql, statementType, arrayOfNulls(argsCount))

        @Suppress("UNCHECKED_CAST")
        fun create(
            session: ThreadLocalSession,
            @Language("RoomSql") sql: String,
            statementType: SQLStatementType,
            args: Array<out Any?>
        ) = SQLQuery(session, sql, statementType, Array<Any?>::class.java.cast(args.copyOf()))
    }

    override fun bindBlob(index: Int, value: ByteArray) = bind(index, value)

    override fun bindDouble(index: Int, value: Double) = bind(index, value)

    override fun bindInt(index: Int, value: Int) = bind(index, value)

    override fun bindLong(index: Int, value: Long) = bind(index, value)

    override fun bindNull(index: Int) = bind(index, null)

    override fun bindString(index: Int, value: String) = bind(index, value)

    override fun clearBindings() {
        bindArgs.fill(null)
    }

    override fun close() {
        clearBindings()
    }

    // TODO Return unit instead? Move populating cursor column names to a cursor factory or similar, always after step?
    // TODO Ever need to prepare again after execute, prepare_v2 will auto-recompile on step picking up any schema change?
    override fun fill(
        window: ICursorWindow
    ): SQLStatementInformation {
        val information = session.get().execute(
            statementType.isPredictedWrite,
            sql,
            statementType,
            EMPTY_SQL_STATEMENT_INFORMATION
        ) {
            it.prepare(sql).apply {
                if (isReadOnly) {
                    it.executeForCursorWindow(sql, bindArgs, window)
                    return@fill this
                }
            }
        }
        return if (information !== EMPTY_SQL_STATEMENT_INFORMATION) {
            session.get().execute(true, sql, statementType, EMPTY_SQL_STATEMENT_INFORMATION) {
                it.executeForCursorWindow(sql, bindArgs, window)
            }
            information
        } else {
            // Query was resolved as transactional(!!)
            EMPTY_SQL_STATEMENT_INFORMATION
        }
    }

    private fun bind(index: Int, arg: Any?) {
        bindArgs[index - 1] = arg
    }
}

class SimpleSQLQuery(
    @Language("RoomSql") override val sql: String,
    private val bindArgs: Array<out Any?> = EMPTY_ARRAY
) : ISQLQuery {
    override val argCount = bindArgs.size

    override fun bindTo(statement: ISQLProgram) = bind(statement, bindArgs)

    private companion object {
        fun bind(statement: ISQLProgram, bindArgs: Array<out Any?>) {
            bindArgs.forEachByPosition { arg, i -> bind(statement, i, arg) }
        }

        fun bind(statement: ISQLProgram, index: Int, arg: Any?) = statement.run {
            when (arg) {
                null -> bindNull(index)
                is String -> bindString(index, arg)
                is Int -> bindInt(index, arg)
                is Long -> bindLong(index, arg)
                is Float -> bindDouble(index, arg.toDouble())
                is Double -> bindDouble(index, arg)
                is Short -> bindInt(index, arg.toInt())
                is Byte -> bindInt(index, arg.toInt())
                is ByteArray -> bindBlob(index, arg)
                else -> throw IllegalArgumentException("Cannot bind arg of class ${arg.javaClass} at index $index.")
            }
        }
    }
}

private interface IQuery : ISQLProgram {
    fun fill(window: ICursorWindow): SQLStatementInformation
}

internal fun StringBuilder.selectColumns(columns: Array<out String>, distinct: Boolean) = apply {
    append("SELECT ")
    if (distinct) {
        append("DISTINCT ")
    }
    if (columns.isEmpty()) {
        append('*')
    } else {
        columns.joinTo(this, ',')
    }
}

internal fun StringBuilder.fromTable(table: String) = append(" FROM ").append(table)

internal fun StringBuilder.where(clause: String) = apply {
    if (clause.isNotEmpty()) {
        append(" WHERE ").append(clause)
    }
}

internal fun StringBuilder.groupBy(clause: String?) = apply {
    clause?.let { append(" GROUP BY ").append(it) }
}

internal fun StringBuilder.having(clause: String?) = apply {
    clause?.let { append(" HAVING ").append(it) }
}

internal fun StringBuilder.orderBy(clause: String?) = apply {
    clause?.let { append(" ORDER BY ").append(it) }
}

internal fun StringBuilder.limit(limit: Int?) = apply {
    limit?.let { append(" LIMIT ").append(it) }
}
