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

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue

private interface Disposer {
    fun dispose()
}

internal object Disposal {
    private object Work : Runnable {
        override fun run() {
            while (true) {
                disposeNext()
            }
        }

        private fun disposeNext() {
            requireNotNull(queue.remove() as? Disposer).run {
                dispose()
                check(disposers.remove(this)) { "Leaked a disposer reference." }
            }
        }
    }

    private val disposers = concurrentSetOf<Disposer>()
    private val queue = ReferenceQueue<Any>()

    init {
        Thread(Work).apply {
            isDaemon = true
            name = "Selekt.Disposer"
            priority = Thread.NORM_PRIORITY
            start()
        }
    }

    internal fun registerConnection(
        connection: SQLConnection,
        pointer: Long,
        sqlite: SQLite
    ) {
        disposers.add(ExternalConnectionReference(
            connection,
            queue,
            pointer,
            sqlite
        )).also {
            check(it) { "Failed to register a connection reference for disposal." }
        }
    }

    internal fun registerStatement(
        statement: SQLPreparedStatement,
        pointer: Long,
        sqlite: SQLite
    ) {
        disposers.add(ExternalStatementReference(
            statement,
            queue,
            pointer,
            sqlite
        )).also {
            check(it) { "Failed to register a statement reference for disposal." }
        }
    }
}

private class ExternalConnectionReference constructor(
    connection: SQLConnection,
    queue: ReferenceQueue<Any>,
    private val pointer: Long,
    private val sqlite: SQLite
) : PhantomReference<Any>(connection, queue), Disposer {
    override fun dispose() {
        check(sqlite.closeV2(pointer) == 0) { "Failed to close an external connection resource." }
    }
}

private class ExternalStatementReference constructor(
    statement: SQLPreparedStatement,
    queue: ReferenceQueue<Any>,
    private val pointer: Long,
    private val sqlite: SQLite
) : PhantomReference<Any>(statement, queue), Disposer {
    override fun dispose() {
        check(sqlite.finalize(pointer) == 0) { "Failed to close an external statement resource." }
    }
}
