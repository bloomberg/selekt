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

package com.bloomberg.selekt.jdbc.lob

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.sql.Blob
import java.sql.SQLException
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import javax.annotation.concurrent.NotThreadSafe

/**
 * @since 0.33.1
 */
@Suppress("Detekt.StringLiteralDuplication")
@NotThreadSafe
internal class JdbcBlob(initialData: ByteArray = byteArrayOf()) : Blob {
    private val data = ByteArrayOutputStream()
    @Volatile
    private var freed = 0

    private companion object {
        private const val ZERO_CHUNK_SIZE = 8192
        private val ZERO_CHUNK = ByteArray(ZERO_CHUNK_SIZE)

        val FREED_UPDATER: AtomicIntegerFieldUpdater<JdbcBlob> = AtomicIntegerFieldUpdater.newUpdater(
            JdbcBlob::class.java,
            "freed"
        )
    }

    init {
        data.write(initialData)
    }

    override fun length(): Long {
        checkNotFreed()
        return data.size().toLong()
    }

    override fun getBytes(pos: Long, length: Int): ByteArray {
        checkNotFreed()
        if (pos < 1) {
            throw SQLException("Position must be >= 1 (got $pos)")
        } else if (length < 0) {
            throw SQLException("Length must be non-negative (got $length)")
        }
        val startIndex = (pos - 1).toInt().also {
            if (it < 0) {
                throw SQLException("Position $pos is out of bounds (index underflow)")
            }
        }
        val bytes = data.toByteArray().also {
            if (startIndex > it.size) {
                throw SQLException("Position $pos is out of bounds (length=${it.size})")
            }
        }
        val endIndex = minOf(startIndex + length, bytes.size)
        return bytes.copyOfRange(startIndex, endIndex)
    }

    override fun getBinaryStream(): InputStream {
        checkNotFreed()
        return ByteArrayInputStream(data.toByteArray())
    }

    override fun getBinaryStream(pos: Long, length: Long): InputStream {
        checkNotFreed()
        return ByteArrayInputStream(getBytes(pos, length.toInt()))
    }

    override fun position(pattern: ByteArray, start: Long): Long {
        checkNotFreed()
        if (start < 1) {
            throw SQLException("Start position must be >= 1 (received $start)")
        }
        val startIndex = (start - 1).toInt()
        val bytes = data.toByteArray()
        for (i in startIndex until bytes.size - pattern.size + 1) {
            var match = true
            for (j in pattern.indices) {
                if (bytes[i + j] != pattern[j]) {
                    match = false
                    break
                }
            }
            if (match) {
                return (i + 1).toLong()
            }
        }
        return -1L
    }

    override fun position(searchBlob: Blob, start: Long): Long {
        checkNotFreed()
        return position(
            searchBlob.getBytes(1, searchBlob.length().toInt()),
            start
        )
    }

    override fun setBytes(pos: Long, bytes: ByteArray): Int {
        checkNotFreed()
        return setBytes(pos, bytes, 0, bytes.size)
    }

    override fun setBytes(pos: Long, bytes: ByteArray, offset: Int, len: Int): Int {
        checkNotFreed()
        val startIndex = validatedStartIndex(pos)
        when {
            offset < 0 || offset > bytes.size -> throw SQLException(
                "Offset $offset is out of bounds for byte array of size ${bytes.size}")
            len < 0 || offset + len > bytes.size -> throw SQLException(
                "Length $len with offset $offset exceeds byte array size ${bytes.size}")
        }
        if (startIndex.toLong() + len.toLong() > Int.MAX_VALUE.toLong()) {
            throw SQLException(
                "Resulting blob size (position=$pos, length=$len) exceeds maximum supported size ${Int.MAX_VALUE}")
        }
        val currentData = data.toByteArray()
        if (startIndex > currentData.size) {
            data.reset()
            data.write(currentData)
            writeZeroPadding(startIndex - currentData.size)
            data.write(bytes, offset, len)
        } else if (startIndex + len <= currentData.size) {
            val newData = currentData.copyOf()
            bytes.copyInto(newData, startIndex, offset, offset + len)
            data.reset()
            data.write(newData)
        } else {
            val newData = currentData.copyOf(startIndex + len)
            bytes.copyInto(newData, startIndex, offset, offset + len)
            data.reset()
            data.write(newData)
        }
        return len
    }

    override fun setBinaryStream(pos: Long): OutputStream {
        checkNotFreed()
        val startIndex = validatedStartIndex(pos)
        return object : OutputStream() {
            private var currentPos = startIndex
            private var initialized = false

            private fun initializeIfNeeded() {
                if (!initialized) {
                    initialized = true
                    val currentData = data.run {
                        toByteArray().also { data.reset() }
                    }
                    data.write(currentData, 0, minOf(currentPos, currentData.size))
                    writeZeroPadding(currentPos - currentData.size)
                }
            }

            override fun write(b: Int) {
                checkNotFreed()
                initializeIfNeeded()
                data.write(b)
                ++currentPos
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                checkNotFreed()
                initializeIfNeeded()
                data.write(b, off, len)
                currentPos += len
            }

            override fun flush() = Unit

            override fun close() = Unit
        }
    }

    override fun truncate(len: Long) {
        checkNotFreed()
        if (len < 0) {
            throw SQLException("Length must be non-negative (received $len)")
        }
        val currentData = data.toByteArray().also {
            if (len >= it.size) {
                return
            }
        }
        data.run {
            reset()
            write(currentData, 0, len.toInt())
        }
    }

    override fun free() {
        if (FREED_UPDATER.compareAndSet(this, 0, 1)) {
            data.reset()
        }
    }

    private fun checkNotFreed() {
        if (freed != 0) {
            throw SQLException("Blob has been freed")
        }
    }

    private fun validatedStartIndex(pos: Long): Int {
        if (pos < 1L) {
            throw SQLException("Position must be >= 1 (received $pos)")
        }
        return (pos - 1L).also {
            if (it > Int.MAX_VALUE.toLong()) {
                throw SQLException(
                    "Position $pos exceeds maximum supported blob index ${Int.MAX_VALUE.toLong() + 1L}")
            }
        }.toInt()
    }

    private fun writeZeroPadding(count: Int) {
        var remaining = count
        while (remaining > 0) {
            val chunkSize = minOf(remaining, ZERO_CHUNK_SIZE)
            data.write(ZERO_CHUNK, 0, chunkSize)
            remaining -= chunkSize
        }
    }

    internal fun asBytes(): ByteArray {
        checkNotFreed()
        return data.toByteArray()
    }
}
