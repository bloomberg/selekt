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

/**
 * @since 0.12.1
 */
data class DatabaseConfiguration(
    val borrowWaitTimeoutMillis: Long = -1L,
    val busyTimeoutMillis: Int = 0,
    val evictionDelayMillis: Long,
    val maxConnectionPoolSize: Int,
    /**
     * Maximum size of the prepared statement cache.
     *
     * Each prepared statement is between 1KB and 6KB, depending on the complexity of the SQL statement and schema. A large
     * cache may use a significant amount of memory.
     */
    val maxSqlCacheSize: Int,
    val name: String = "main",
    val secureDelete: SQLiteSecureDelete = SQLiteSecureDelete.FAST,
    /**
     * Time between idle connection eviction runs in milliseconds, -1L for never.
     */
    val timeBetweenEvictionRunsMillis: Long,
    val trace: SQLiteTraceEventMode? = null,
    /**
     * Whether to use native SQLite commit/rollback hooks for transaction listeners.
     *
     * When true, transaction listeners are called via SQLite's native commit_hook and rollback_hook,
     * which requires JNI thread attachment overhead but catches all transactions including those
     * initiated via raw SQL.
     *
     * When false (default), transaction listeners are called explicitly from SQLSession methods,
     * which only works for transactions managed via the SQLSession.
     */
    val useNativeTransactionListeners: Boolean = false,
    /**
     * Maximum number of rows a cursor holds in memory at once, [Int.MAX_VALUE] to hold every row.
     *
     * When bounded, a query materialises only this many rows and re-queries whenever the caller
     * moves outside them. Memory is then bounded by the window rather than by the size of the
     * result set, at the cost of re-stepping the statement on each refill. Refills re-run the query,
     * so volatile expressions, unspecified row ordering, and writes made after the first window can
     * change the rows subsequently observed.
     *
     * Bounding this matters most for cursor windows populated natively, whose rows occupy a single
     * off-heap allocation sized to the whole result set. Rows held on the Java heap can be paged
     * too, though an unclosed cursor there is collected as ordinary garbage.
     *
     * Paged cursors are not thread safe and must not be accessed concurrently from multiple
     * threads. Cross-thread hand-off requires the caller to provide the usual happens-before
     * relationship.
     */
    val cursorWindowSize: Int = Int.MAX_VALUE
) {
    init {
        require(maxConnectionPoolSize > 0)
        require(cursorWindowSize > 0) { "Cursor window size must be positive, but was $cursorWindowSize." }
    }

    companion object {
        /**
         * When using WAL, a timeout could occur if one connection is busy performing an auto-checkpoint operation. The
         * busy timeout needs to be long enough to tolerate slow I/O write operations but not so long as to cause the
         * application to hang indefinitely if there is a problem acquiring a database lock.
         *
         * @see <a href="https://www.sqlite.org/c3ref/busy_timeout.html">SQLite's busy_timeout</a>
         */
        const val COMMON_BUSY_TIMEOUT_MILLIS = 2_500
    }
}

private object TraceCodes {
    const val STATEMENT = 0x01
    const val PROFILE = 0x02
    const val ROW = 0x04
    const val CLOSE = 0x08
}

enum class SQLTraceEventCode(private val value: Int) {
    STATEMENT(TraceCodes.STATEMENT),
    PROFILE(TraceCodes.PROFILE),
    ROW(TraceCodes.ROW),
    CLOSE(TraceCodes.CLOSE);

    operator fun invoke() = value
}
