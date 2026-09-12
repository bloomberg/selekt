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
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.StringReader
import java.io.Writer
import java.sql.Clob
import java.sql.SQLException
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import javax.annotation.concurrent.NotThreadSafe

/**
 * @since 0.28.0
 */
@Suppress("Detekt.StringLiteralDuplication")
@NotThreadSafe
internal class JdbcClob(initialContent: String = "") : Clob {
    private val content = StringBuilder()
    @Volatile
    private var freed = 0

    private companion object {
        @JvmField
        val FREED_UPDATER: AtomicIntegerFieldUpdater<JdbcClob> = AtomicIntegerFieldUpdater.newUpdater(
            JdbcClob::class.java,
            "freed"
        )
    }

    init {
        content.append(initialContent)
    }

    override fun length(): Long {
        checkNotFreed()
        return content.length.toLong()
    }

    override fun getSubString(pos: Long, length: Int): String {
        checkNotFreed()
        if (length < 0) {
            throw SQLException("Length must be non-negative (received $length)")
        }
        val startIndex = validatedReadIndex(pos)
        val endIndex = startIndex + minOf(length, content.length - startIndex)
        return content.substring(startIndex, endIndex)
    }

    override fun getCharacterStream(): Reader {
        checkNotFreed()
        return StringReader(content.toString())
    }

    override fun getCharacterStream(pos: Long, length: Long): Reader {
        checkNotFreed()
        val substring = getSubString(pos, validatedLobLength(length, "CLOB stream"))
        return StringReader(substring)
    }

    override fun getAsciiStream(): InputStream {
        checkNotFreed()
        return ByteArrayInputStream(content.toString().toByteArray(Charsets.US_ASCII))
    }

    override fun position(searchstr: String, start: Long): Long {
        checkNotFreed()
        if (start < 1) {
            throw SQLException("Start position must be >= 1 (received $start)")
        }
        val index = if (start > content.length.toLong()) {
            -1
        } else {
            content.indexOf(searchstr, (start - 1L).toInt())
        }
        return if (index >= 0) {
            (index + 1).toLong()
        } else {
            -1L
        }
    }

    override fun position(searchstr: Clob, start: Long): Long {
        checkNotFreed()
        val searchLength = validatedLobLength(searchstr.length(), "Search CLOB")
        val searchString = searchstr.getSubString(1, searchLength)
        return position(searchString, start)
    }

    override fun setString(pos: Long, str: String): Int {
        checkNotFreed()
        return setString(pos, str, 0, str.length)
    }

    override fun setString(pos: Long, str: String, offset: Int, len: Int): Int {
        checkNotFreed()
        when {
            offset < 0 || offset > str.length -> throw SQLException(
                "Offset $offset is out of bounds for string of length ${str.length}")
            len < 0 || len > str.length - offset -> throw SQLException(
                "Length $len with offset $offset exceeds string length ${str.length}")
        }
        val startIndex = validatedWriteIndex(pos)
        val substring = str.substring(offset, offset + len)
        if (startIndex < content.length) {
            val endIndex = minOf(startIndex + len, content.length)
            content.replace(startIndex, endIndex, substring)
        } else {
            content.append(substring)
        }
        return len
    }

    override fun setCharacterStream(pos: Long): Writer {
        checkNotFreed()
        val startIndex = validatedWriteIndex(pos)
        return object : Writer() {
            override fun write(cbuf: CharArray, off: Int, len: Int) {
                checkNotFreed()
                val str = String(cbuf, off, len)
                if (content.length == startIndex) {
                    content.append(str)
                } else {
                    val endIndex = minOf(startIndex + len, content.length)
                    content.replace(startIndex, endIndex, str)
                }
            }

            override fun flush() = Unit

            override fun close() = Unit
        }
    }

    override fun setAsciiStream(pos: Long): OutputStream {
        checkNotFreed()
        val startIndex = validatedWriteIndex(pos)
        return object : OutputStream() {
            private var currentPos = startIndex

            override fun write(b: Int) {
                checkNotFreed()
                val char = b.toChar()
                if (currentPos < content.length) {
                    content.setCharAt(currentPos, char)
                } else {
                    content.append(char)
                }
                ++currentPos
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                checkNotFreed()
                val str = String(b, off, len, Charsets.US_ASCII)
                if (currentPos < content.length) {
                    val endIndex = minOf(currentPos + len, content.length)
                    content.replace(currentPos, endIndex, str)
                } else {
                    content.append(str)
                }
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
        } else if (len >= content.length) {
            return
        }
        content.setLength(len.toInt())
    }

    override fun free() {
        if (FREED_UPDATER.compareAndSet(this, 0, 1)) {
            content.clear()
            content.trimToSize()
        }
    }

    private fun checkNotFreed() {
        if (freed != 0) {
            throw SQLException("Clob has been freed")
        }
    }

    private fun validatedReadIndex(pos: Long): Int {
        if (pos < 1) {
            throw SQLException("Position must be >= 1 (received $pos)")
        }
        val index = pos - 1L
        if (index > content.length.toLong()) {
            throw SQLException("Position $pos is out of bounds (length=${content.length})")
        }
        return index.toInt()
    }

    private fun validatedWriteIndex(pos: Long): Int {
        if (pos < 1) {
            throw SQLException("Position must be >= 1 (received $pos)")
        } else if (pos > content.length + 1L) {
            throw SQLException("Position $pos is out of bounds (length=${content.length})")
        }
        return (pos - 1L).toInt()
    }

    private fun validatedLobLength(length: Long, description: String): Int {
        if (length < 0L) {
            throw SQLException("$description length must be non-negative (received $length)")
        } else if (length > Int.MAX_VALUE.toLong()) {
            throw SQLException("$description length $length exceeds maximum supported length ${Int.MAX_VALUE}")
        }
        return length.toInt()
    }

    internal fun asString(): String {
        checkNotFreed()
        return content.toString()
    }
}
