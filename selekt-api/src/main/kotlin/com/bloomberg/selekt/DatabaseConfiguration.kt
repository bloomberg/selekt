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
     * Maximum rows retained by a scrollable cursor at once, or
     * [PLATFORM_DEFAULT_CURSOR_WINDOW_SIZE] to use the platform default.
     *
     * Moving outside the current window re-runs the query to refill it. Callers requiring a stable
     * snapshot must keep the cursor inside a transaction. Sequential processing of unbounded results
     * should use a forward-only API.
     *
     * Android defaults to [UNBOUNDED_CURSOR_WINDOW_SIZE] for compatibility. Other JVM runtimes
     * default to [JVM_DEFAULT_CURSOR_WINDOW_SIZE].
     */
    val cursorWindowSize: Int = PLATFORM_DEFAULT_CURSOR_WINDOW_SIZE,
    /**
     * Maximum estimated bytes retained by a scrollable cursor window, or
     * [PLATFORM_DEFAULT_CURSOR_WINDOW_BYTE_SIZE] to use the platform default.
     *
     * A row that cannot fit by itself is rejected before its text or BLOB values are copied into JVM
     * memory. The estimate includes stored value payloads and cursor bookkeeping.
     *
     * Android defaults to [UNBOUNDED_CURSOR_WINDOW_BYTE_SIZE] for compatibility. Other JVM runtimes
     * default to [JVM_DEFAULT_CURSOR_WINDOW_BYTE_SIZE].
     */
    val cursorWindowByteSize: Int = PLATFORM_DEFAULT_CURSOR_WINDOW_BYTE_SIZE
) {
    init {
        require(maxConnectionPoolSize > 0)
        require(cursorWindowSize == PLATFORM_DEFAULT_CURSOR_WINDOW_SIZE || cursorWindowSize > 0) {
            "Cursor window size must be positive or the platform default, but was $cursorWindowSize."
        }
        require(
            cursorWindowByteSize == PLATFORM_DEFAULT_CURSOR_WINDOW_BYTE_SIZE ||
                cursorWindowByteSize >= MINIMUM_CURSOR_WINDOW_BYTE_SIZE
        ) {
            "Cursor window byte size must be at least $MINIMUM_CURSOR_WINDOW_BYTE_SIZE or the platform default, " +
                "but was $cursorWindowByteSize."
        }
    }

    /** Returns a copy with explicit scrollable-cursor row and byte limits. */
    fun withCursorWindowLimits(cursorWindowSize: Int, cursorWindowByteSize: Int) = copy(
        cursorWindowSize = cursorWindowSize,
        cursorWindowByteSize = cursorWindowByteSize
    )

    companion object {
        const val PLATFORM_DEFAULT_CURSOR_WINDOW_SIZE = -1
        const val PLATFORM_DEFAULT_CURSOR_WINDOW_BYTE_SIZE = -1
        const val JVM_DEFAULT_CURSOR_WINDOW_SIZE = 1024
        const val JVM_DEFAULT_CURSOR_WINDOW_BYTE_SIZE = 2 * 1024 * 1024
        const val UNBOUNDED_CURSOR_WINDOW_SIZE = Int.MAX_VALUE
        const val UNBOUNDED_CURSOR_WINDOW_BYTE_SIZE = Int.MAX_VALUE
        const val MINIMUM_CURSOR_WINDOW_BYTE_SIZE = 8

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
