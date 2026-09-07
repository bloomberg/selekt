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

import com.bloomberg.selekt.commons.forEachByPosition
import com.bloomberg.selekt.commons.joinTo
import org.intellij.lang.annotations.Language
import java.lang.StringBuilder
import javax.annotation.concurrent.NotThreadSafe

private val EMPTY_ARRAY = emptyArray<Any?>()
private val EMPTY_SQL_STATEMENT_INFORMATION = SQLStatementInformation(false, 0, emptyArray())

private fun emptyCursorWindowPage() = CursorWindowPage(SimpleCursorWindow(), 0, 0)

/**
 * @since 0.12.1
 */
@NotThreadSafe
internal class SQLQuery internal constructor(
    private val session: SQLSessionProvider,
    private val sql: String,
    private val statementType: SQLStatementType,
    private val bindArgs: Array<Any?>,
    private var highestBoundIndex: Int = 0
) : IQuery {
    private val namedParameters: Map<String, Int> by lazy { parseNamedParameters(sql) }
    private var preparedParameterCount: Int? = null

    companion object {
        fun create(
            session: SQLSessionProvider,
            @Language("RoomSql") sql: String,
            statementType: SQLStatementType,
            argsCount: Int
        ) = SQLQuery(session, sql, statementType, arrayOfNulls(argsCount))

        @Suppress("UNCHECKED_CAST")
        fun create(
            session: SQLSessionProvider,
            @Language("RoomSql") sql: String,
            statementType: SQLStatementType,
            args: Array<out Any?>
        ): SQLQuery {
            val argsCopy = Array<Any?>::class.java.cast(args.copyOf())
            return SQLQuery(session, sql, statementType, argsCopy, argsCopy.size)
        }
    }

    override fun bindBlob(index: Int, value: ByteArray) = bind(index, value)

    override fun bindBlob(name: String, value: ByteArray) {
        bind(resolveParameterIndex(name), value)
    }

    override fun bindDouble(index: Int, value: Double) = bind(index, value)

    override fun bindDouble(name: String, value: Double) = bind(resolveParameterIndex(name), value)

    override fun bindInt(index: Int, value: Int) = bind(index, value)

    override fun bindInt(name: String, value: Int) = bind(resolveParameterIndex(name), value)

    override fun bindLong(index: Int, value: Long) = bind(index, value)

    override fun bindLong(name: String, value: Long) = bind(resolveParameterIndex(name), value)

    override fun bindNull(index: Int) = bind(index, null)

    override fun bindNull(name: String) = bind(resolveParameterIndex(name), null)

    override fun bindString(index: Int, value: String) = bind(index, value)

    override fun bindString(name: String, value: String) = bind(resolveParameterIndex(name), value)

    override fun clearBindings() {
        bindArgs.fill(null)
        highestBoundIndex = 0
    }

    override fun close() {
        clearBindings()
    }

    // TODO Ever need to prepare again after execute, prepare_v2 will auto-recompile on step picking up any schema change?
    override fun fill(windowSize: Int): Pair<SQLStatementInformation, CursorWindowPage> = fill(
        windowSize,
        countAllRows = true
    )

    fun fillUpTo(maximumRows: Int): Pair<SQLStatementInformation, CursorWindowPage> {
        require(maximumRows > 0) { "Maximum rows must be positive." }
        val (information, page) = fill(maximumRows, countAllRows = false)
        return information to page.copy(count = page.window.numberOfRows())
    }

    private fun fill(
        windowSize: Int,
        countAllRows: Boolean
    ): Pair<SQLStatementInformation, CursorWindowPage> {
        var page: CursorWindowPage? = null
        val information = session().execute(
            statementType.isPredictedWrite,
            sql,
            statementType,
            EMPTY_SQL_STATEMENT_INFORMATION
        ) {
            it.prepare(sql).apply {
                if (isReadOnly) {
                    page = it.executeForCursorWindow(
                        sql,
                        validatedBindArgs(parameterCount),
                        0,
                        windowSize,
                        countAllRows
                    )
                }
            }
        }
        page?.let { return information to it }
        return if (information !== EMPTY_SQL_STATEMENT_INFORMATION) {
            information to session().execute(true, sql, statementType, emptyCursorWindowPage()) {
                it.executeForCursorWindow(
                    sql,
                    validatedBindArgs(information.parameterCount),
                    0,
                    windowSize,
                    countAllRows
                )
            }
        } else {
            // Query was resolved as transactional(!!)
            EMPTY_SQL_STATEMENT_INFORMATION to emptyCursorWindowPage()
        }
    }

    fun refiller(windowSize: Int): (Int) -> CursorWindowPage {
        val args = validatedBindArgs(checkNotNull(preparedParameterCount)).copyOf()
        return { startPosition ->
            session().execute(false, sql, statementType, emptyCursorWindowPage()) {
                it.executeForCursorWindow(sql, args, startPosition, windowSize, false)
            }
        }
    }

    private fun bind(index: Int, arg: Any?) {
        bindArgs[index - 1] = arg
        highestBoundIndex = maxOf(highestBoundIndex, index)
    }

    private fun validatedBindArgs(parameterCount: Int): Array<out Any?> {
        require(bindArgs.size >= parameterCount) {
            "Expected $parameterCount bind arguments but ${bindArgs.size} were provided."
        }
        // Some SupportSQLiteQuery adapters report their 1-based binding storage capacity as the argument count.
        // Ignore only unused capacity; never discard an argument that was actually bound.
        require(highestBoundIndex <= parameterCount) {
            "Cannot bind argument at index $highestBoundIndex; statement has $parameterCount parameters."
        }
        preparedParameterCount = parameterCount
        return if (bindArgs.size == parameterCount) { bindArgs } else { bindArgs.copyOf(parameterCount) }
    }

    private fun resolveParameterIndex(name: String): Int = namedParameters[name] ?: throw IllegalArgumentException(
        "Named parameter '$name' not found in SQL. Available parameters: ${namedParameters.keys}"
    )
}

class SimpleSQLQuery(
    @field:Language("RoomSql") override val sql: String,
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
    fun fill(windowSize: Int): Pair<SQLStatementInformation, CursorWindowPage>
}

@JvmSynthetic
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

@JvmSynthetic
internal fun StringBuilder.fromTable(table: String) = append(" FROM ").append(table)

@JvmSynthetic
internal fun StringBuilder.where(clause: String) = apply {
    if (clause.isNotEmpty()) {
        append(" WHERE ").append(clause)
    }
}

@JvmSynthetic
internal fun StringBuilder.groupBy(clause: String?) = apply {
    clause?.let { append(" GROUP BY ").append(it) }
}

@JvmSynthetic
internal fun StringBuilder.having(clause: String?) = apply {
    clause?.let { append(" HAVING ").append(it) }
}

@JvmSynthetic
internal fun StringBuilder.orderBy(clause: String?) = apply {
    clause?.let { append(" ORDER BY ").append(it) }
}

@JvmSynthetic
internal fun StringBuilder.limit(limit: Int?) = apply {
    limit?.let { append(" LIMIT ").append(it) }
}
