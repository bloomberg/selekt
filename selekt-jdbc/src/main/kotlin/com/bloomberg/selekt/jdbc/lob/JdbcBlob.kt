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
import java.util.Objects
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import javax.annotation.concurrent.NotThreadSafe

/**
 * @since 0.33.1
 */
@Suppress("Detekt.StringLiteralDuplication")
@NotThreadSafe
internal class JdbcBlob(
    initialData: ByteArray = byteArrayOf(),
    private val maximumLength: Int = Int.MAX_VALUE
) : Blob {
    private val data = ByteArrayOutputStream()
    @Volatile
    private var freed = 0

    private companion object {
        @JvmField
        val FREED_UPDATER: AtomicIntegerFieldUpdater<JdbcBlob> = AtomicIntegerFieldUpdater.newUpdater(
            JdbcBlob::class.java,
            "freed"
        )
    }

    init {
        require(maximumLength >= initialData.size) {
            "Maximum length $maximumLength is smaller than initial BLOB length ${initialData.size}"
        }
        data.write(initialData)
    }

    override fun length(): Long {
        checkNotFreed()
        return data.size().toLong()
    }

    override fun getBytes(pos: Long, length: Int): ByteArray {
        checkNotFreed()
        if (length < 0) {
            throw SQLException("Length must be non-negative (got $length)")
        }
        val bytes = data.toByteArray()
        val startIndex = validatedReadIndex(pos, bytes.size)
        val endIndex = startIndex + minOf(length, bytes.size - startIndex)
        return bytes.copyOfRange(startIndex, endIndex)
    }

    override fun getBinaryStream(): InputStream {
        checkNotFreed()
        return ByteArrayInputStream(data.toByteArray())
    }

    override fun getBinaryStream(pos: Long, length: Long): InputStream {
        checkNotFreed()
        return ByteArrayInputStream(getBytes(pos, validatedLobLength(length, "BLOB stream")))
    }

    override fun position(pattern: ByteArray, start: Long): Long {
        checkNotFreed()
        if (start < 1) {
            throw SQLException("Start position must be >= 1 (received $start)")
        }
        val bytes = data.toByteArray()
        val candidateIndices = if (start <= bytes.size.toLong() + 1L) {
            (start - 1L).toInt()..bytes.size - pattern.size
        } else {
            IntRange.EMPTY
        }
        for (i in candidateIndices) {
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
        val searchLength = validatedLobLength(searchBlob.length(), "Search BLOB")
        return position(
            searchBlob.getBytes(1, searchLength),
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
            len < 0 || len > bytes.size - offset -> throw SQLException(
                "Length $len with offset $offset exceeds byte array size ${bytes.size}")
        }
        val endIndex = validatedWriteEndIndex(startIndex, len)
        if (len == 0) {
            return 0
        }
        val currentData = data.toByteArray()
        if (endIndex <= currentData.size) {
            val newData = currentData.copyOf()
            bytes.copyInto(newData, startIndex, offset, offset + len)
            data.reset()
            data.write(newData)
        } else {
            val newData = currentData.copyOf(endIndex)
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
                    if (currentPos > data.size()) {
                        throw SQLException(
                            "Stream position ${currentPos.toLong() + 1L} is out of bounds (length=${data.size()})")
                    }
                    val currentData = data.toByteArray()
                    data.reset()
                    data.write(currentData, 0, currentPos)
                    initialized = true
                }
            }

            override fun write(b: Int) {
                checkNotFreed()
                val endIndex = validatedWriteEndIndex(currentPos, 1)
                initializeIfNeeded()
                data.write(b)
                currentPos = endIndex
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                checkNotFreed()
                Objects.checkFromIndexSize(off, len, b.size)
                if (len == 0) {
                    return
                }
                val endIndex = validatedWriteEndIndex(currentPos, len)
                initializeIfNeeded()
                data.write(b, off, len)
                currentPos = endIndex
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
        val currentLength = data.size()
        if (pos > currentLength.toLong() + 1L) {
            throw SQLException("Position $pos is out of bounds (length=$currentLength)")
        }
        return (pos - 1L).toInt()
    }

    private fun validatedWriteEndIndex(startIndex: Int, length: Int): Int {
        val endIndex = startIndex.toLong() + length.toLong()
        if (endIndex > maximumLength.toLong()) {
            throw SQLException("Resulting BLOB length $endIndex exceeds maximum supported length $maximumLength")
        }
        return endIndex.toInt()
    }

    private fun validatedReadIndex(pos: Long, size: Int): Int {
        if (pos < 1L) {
            throw SQLException("Position must be >= 1 (got $pos)")
        }
        val index = pos - 1L
        if (index > size.toLong()) {
            throw SQLException("Position $pos is out of bounds (length=$size)")
        }
        return index.toInt()
    }

    private fun validatedLobLength(length: Long, description: String): Int {
        if (length < 0L) {
            throw SQLException("$description length must be non-negative (received $length)")
        } else if (length > Int.MAX_VALUE.toLong()) {
            throw SQLException("$description length $length exceeds maximum supported length ${Int.MAX_VALUE}")
        }
        return length.toInt()
    }

    internal fun asBytes(): ByteArray {
        checkNotFreed()
        return data.toByteArray()
    }
}
