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

import java.io.InputStream

private const val DEFAULT_BLOB_TRANSFER_BUFFER_SIZE = 64 * 1024

/**
 * Describes a schema-aware batch of incremental BLOB inserts.
 *
 * [insertSql] must insert exactly one row per execution. Its parameter at [blobParameterIndex]
 * is replaced with `zeroblob(length)` for each [StreamingBlobRow]. The target must be a rowid
 * table and [table] and [column] must identify the inserted BLOB. Views, `WITHOUT ROWID` tables,
 * upserts, and statements that can update or ignore an existing row are not supported.
 *
 * Execution retains one prepared statement, one incremental BLOB handle, and one
 * [transferBufferSize]-byte transfer buffer for the complete batch.
 *
 * @since 1.4.0
 */
data class StreamingBlobBatch @JvmOverloads constructor(
    val table: String,
    val column: String,
    val insertSql: String,
    val blobParameterIndex: Int,
    val databaseName: String = "main",
    val transferBufferSize: Int = DEFAULT_BLOB_TRANSFER_BUFFER_SIZE
) {
    init {
        require(table.isNotEmpty()) { "Table must not be empty." }
        require(column.isNotEmpty()) { "Column must not be empty." }
        require(insertSql.isNotBlank()) { "Insert SQL must not be blank." }
        require(blobParameterIndex > 0) { "BLOB parameter index must be positive." }
        require(databaseName.isNotEmpty()) { "Database name must not be empty." }
        require(transferBufferSize > 0) { "Transfer buffer size must be positive." }
    }
}

/**
 * One row in a [StreamingBlobBatch].
 *
 * [bindArguments] must contain one entry for every parameter in the batch SQL. The entry selected
 * by `StreamingBlobBatch.blobParameterIndex` is ignored. If [rowId] is null, the rowid reported by
 * SQLite after the insert is used. The caller retains ownership of [inputStream]; execution neither
 * closes it nor retains it after this row has been written. [length] must be exact: both shorter and
 * longer streams fail the batch.
 *
 * @since 1.4.0
 */
@Suppress("Detekt.UseDataClass")
class StreamingBlobRow @JvmOverloads constructor(
    val bindArguments: Array<out Any?>,
    val length: Int,
    val inputStream: InputStream,
    val rowId: Long? = null
) {
    init {
        require(length >= 0) { "BLOB length must be non-negative." }
    }
}
