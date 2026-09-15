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

package com.bloomberg.selekt.jdbc.driver

import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SharedResource
import java.sql.SQLException
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.concurrent.GuardedBy
import kotlin.concurrent.withLock

/**
 * A reference-counted wrapper around [SQLDatabase] for use by the JDBC driver cache.
 *
 * The cache owns the initial retain count. Each
 * [JdbcConnection][com.bloomberg.selekt.jdbc.connection.JdbcConnection] obtains an additional
 * reference, and [releaseConnection] releases it and notifies the cache when the database becomes
 * idle. Releasing the cache reference closes the underlying [SQLDatabase] after all connections
 * have also released their references.
 *
 * @since 0.29.11
 */
internal class SharedDatabase(
    val database: SQLDatabase,
    private val onClose: () -> Unit = {},
    private val onConnectionReleased: (SharedDatabase) -> Unit = {}
) : SharedResource() {
    private data class TransactionOwner(
        val token: Any,
        val thread: Thread
    )

    private val transactionOwnerLock = ReentrantLock()

    // A transaction is claimed at its actual SQLSession begin boundary, before listener callbacks can re-enter JDBC,
    // and released at its end boundary or on connection close.
    @GuardedBy("transactionOwnerLock")
    private var transactionOwner: TransactionOwner? = null

    fun releaseConnection() {
        try {
            release()
        } finally {
            onConnectionReleased(this)
        }
    }

    /**
     * Rejects attempts to hand an active transaction to another thread.
     */
    fun checkTransactionThread(owner: Any) = transactionOwnerLock.withLock {
        val currentOwner = transactionOwner
        if (currentOwner?.token === owner && currentOwner.thread !== Thread.currentThread()) {
            throw SQLException("JDBC connection transaction is owned by another thread")
        }
    }

    /**
     * Synchronizes ownership at the SQLSession transaction begin and end boundaries.
     */
    fun synchronizeTransaction(owner: Any, inTransaction: Boolean) = transactionOwnerLock.withLock {
        if (inTransaction) {
            val currentOwner = transactionOwner?.token
            if (currentOwner != null && currentOwner !== owner) {
                throw SQLException("Database transaction is already owned by another JDBC connection")
            }
            transactionOwner = TransactionOwner(owner, Thread.currentThread())
        } else if (transactionOwner?.token === owner) {
            transactionOwner = null
        }
    }

    /**
     * Releases ownership when its JDBC connection closes, including after rollback failure.
     */
    fun releaseTransaction(owner: Any) = transactionOwnerLock.withLock {
        if (transactionOwner?.token === owner) {
            transactionOwner = null
        }
    }

    /**
     * Rejects only re-entrant primary-connection access by a different owner on the transaction's active thread.
     * Access from another thread is allowed to reach the connection pool and wait for the physical writer normally.
     */
    fun checkPrimaryConnectionAccess(owner: Any) = transactionOwnerLock.withLock {
        val currentOwner = transactionOwner
        if (currentOwner != null &&
            currentOwner.token !== owner &&
            currentOwner.thread === Thread.currentThread()
        ) {
            throw SQLException("Database transaction on this thread is owned by another JDBC connection")
        }
    }

    override fun onReleased() {
        database.use { _ ->
            onClose()
        }
    }
}
