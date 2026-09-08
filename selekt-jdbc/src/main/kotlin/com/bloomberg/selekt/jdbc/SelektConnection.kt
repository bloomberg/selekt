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

package com.bloomberg.selekt.jdbc

import com.bloomberg.selekt.CancellationSignal
import com.bloomberg.selekt.StreamingBlobBatch
import com.bloomberg.selekt.StreamingBlobRow
import java.sql.Connection
import java.sql.SQLException

/**
 * Selekt-specific operations available through [Connection.unwrap].
 *
 * @since 1.4.0
 */
interface SelektConnection {
    /**
     * Executes a schema-aware incremental BLOB batch.
     *
     * The batch participates in the JDBC connection's current transaction. In auto-commit mode,
     * the complete batch is committed atomically. Input streams remain owned by the caller.
     */
    @Throws(SQLException::class)
    fun insertBlobs(batch: StreamingBlobBatch, rows: Iterable<StreamingBlobRow>): Int

    /**
     * Cancellable counterpart of [insertBlobs].
     */
    @Throws(SQLException::class)
    fun insertBlobs(
        batch: StreamingBlobBatch,
        rows: Iterable<StreamingBlobRow>,
        cancellationSignal: CancellationSignal
    ): Int
}
