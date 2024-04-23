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

import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import javax.annotation.concurrent.NotThreadSafe

@JvmSynthetic
internal fun SQLBlob.inputStream(offset: Int, limit: Int): InputStream = BlobInputStream(this, offset, limit)

@JvmSynthetic
internal fun SQLBlob.outputStream(offset: Int): OutputStream = BlobOutputStream(this, offset)

@NotThreadSafe
internal class SQLBlob(
    private val pointer: Long,
    private val sqlite: SQLite,
    val readOnly: Boolean
) : Closeable {
    init {
        check(pointer != NULL)
    }

    val size: Int by lazy(LazyThreadSafetyMode.NONE) { sqlite.blobBytes(pointer) }

    override fun close() {
        sqlite.blobClose(pointer)
    }

    fun read(
        offset: Int,
        destination: ByteArray,
        destinationOffset: Int,
        length: Int
    ) {
        sqlite.blobRead(pointer, offset, destination, destinationOffset, length)
    }

    fun reopen(row: Long) {
        sqlite.blobReopen(pointer, row)
    }

    fun write(
        offset: Int,
        source: ByteArray,
        sourceOffset: Int,
        length: Int
    ) {
        sqlite.blobWrite(pointer, offset, source, sourceOffset, length)
    }
}

@NotThreadSafe
internal class BlobInputStream(
    private val blob: SQLBlob,
    val start: Int = 0,
    val limit: Int = blob.size - start
) : InputStream() {
    private var index = start

    init {
        require(start > -1 && limit > -1 && start + limit <= blob.size)
    }

    override fun available() = start + limit - index

    override fun close() = Unit

    override fun read() = byteArrayOf(0).let {
        if (read(it, index, 1) > -1) it.first().toInt() else -1
    }

    override fun read(
        destination: ByteArray,
        offset: Int,
        length: Int
    ) = if (offset < 0 || length < 0) {
        throw ArrayIndexOutOfBoundsException("Offset: $offset; length: $length")
    } else {
        available().let {
            if (it > 0) {
                minOf(it, length).also { len ->
                    if (offset + len > destination.size) {
                        throw ArrayIndexOutOfBoundsException(
                            "Size: ${destination.size}; offset: $offset; length: $length; truncated length: $len"
                        )
                    }
                    blob.read(index, destination, offset, len)
                    index += len
                }
            } else {
                -1
            }
        }
    }
}

@NotThreadSafe
internal class BlobOutputStream(
    private val blob: SQLBlob,
    val start: Int = 0
) : OutputStream() {
    init {
        require(!blob.readOnly) { "Cannot write to a read-only blob." }
        require(start > -1 && start <= blob.size) { "Cannot write outside the range of a blob." }
    }

    private var index = start
    private val remainingCapacity: Int
        get() = blob.size - index

    override fun close() = Unit

    override fun write(byte: Int) = write(byteArrayOf(byte.toByte()), 0, 1)

    override fun write(
        source: ByteArray,
        offset: Int,
        length: Int
    ) = when {
        offset < 0 || length < 0 || offset + length > source.size ->
            throw ArrayIndexOutOfBoundsException("Size: ${source.size}; offset: $offset; length: $length")
        length == 0 -> Unit
        remainingCapacity >= length -> {
            blob.write(index, source, offset, length)
            index += length
        }
        else -> throw IndexOutOfBoundsException("Blob's size is about to be exceeded.")
    }
}
