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

import com.bloomberg.selekt.DatabaseConfiguration.Companion.COMMON_BUSY_TIMEOUT_MILLIS

/**
 * @since v0.1.0.
 */
enum class SQLiteJournalMode(
    @JvmField
    val databaseConfiguration: DatabaseConfiguration = commonSingleConnectionConfiguration
) {
    /**
     * The DELETE journaling mode is the normal behavior. In the DELETE mode, the rollback journal is deleted at the
     * conclusion of each transaction. Indeed, the delete operation is the action that causes the transaction to commit.
     */
    DELETE,

    /**
     * The MEMORY journaling mode stores the rollback journal in volatile RAM. This saves disk I/O but at the expense of
     * database safety and integrity. If the application using SQLite crashes in the middle of a transaction when the MEMORY
     * journaling mode is set, then the database file will very likely go corrupt.
     */
    MEMORY(commonInMemoryConfiguration),

    /**
     * The OFF journaling mode disables the rollback journal completely. No rollback journal is ever created and hence there
     * is never a rollback journal to delete. The OFF journaling mode disables the atomic commit and rollback capabilities
     * of SQLite. The ROLLBACK command no longer works; it behaves in an undefined way. Applications must avoid using the
     * ROLLBACK command when the journal mode is OFF. If the application crashes in the middle of a transaction when the OFF
     * journaling mode is set, then the database file will very likely go corrupt. Without a journal, there is no way for a
     * statement to unwind partially completed operations following a constraint error. This might also leave the database
     * in a corrupted state.
     */
    OFF,

    /**
     * The PERSIST journaling mode prevents the rollback journal from being deleted at the end of each transaction. Instead,
     * the header of the journal is overwritten with zeros. This will prevent other database connections from rolling the
     * journal back. The PERSIST journaling mode is useful as an optimization on platforms where deleting or truncating a
     * file is much more expensive than overwriting the first block of a file with zeros.
     */
    PERSIST,

    /**
     * The TRUNCATE journaling mode commits transactions by truncating the rollback journal to zero-length instead of
     * deleting it. On many systems, truncating a file is much faster than deleting the file since the containing directory
     * does not need to be changed.
     */
    TRUNCATE,

    /**
     * The WAL journaling mode uses a write-ahead log instead of a rollback journal to implement transactions. The WAL
     * journaling mode is persistent; after being set it stays in effect across multiple database connections and after
     * closing and reopening the database.
     *
     * @see <a href="https://www.sqlite.org/wal.html">SQLite's WAL journal mode</a>
     */
    WAL(commonMultipleConnectionConfiguration)
}

private val commonMultipleConnectionConfiguration = DatabaseConfiguration(
    busyTimeoutMillis = COMMON_BUSY_TIMEOUT_MILLIS,
    evictionDelayMillis = 1_000L,
    maxConnectionPoolSize = 4,
    maxSqlCacheSize = 8,
    timeBetweenEvictionRunsMillis = 20_000L
)

private val commonSingleConnectionConfiguration = commonMultipleConnectionConfiguration.copy(
    busyTimeoutMillis = 0,
    maxConnectionPoolSize = 1
)

private val commonPersistentConnectionConfiguration = commonSingleConnectionConfiguration.copy(
    timeBetweenEvictionRunsMillis = -1L
)

private val commonInMemoryConfiguration = commonPersistentConnectionConfiguration
