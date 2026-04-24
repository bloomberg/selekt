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
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.concurrent.NotThreadSafe

@Suppress("Detekt.StringLiteralDuplication")
@NotThreadSafe
internal class JdbcClob : Clob {
    private val content = StringBuilder()
    private val freed = AtomicBoolean(false)

    constructor()

    constructor(initialContent: String) {
        content.append(initialContent)
    }

    override fun length(): Long {
        checkNotFreed()
        return content.length.toLong()
    }

    override fun getSubString(pos: Long, length: Int): String {
        checkNotFreed()
        if (pos < 1) {
            throw SQLException("Position must be >= 1 (got $pos)")
        } else if (length < 0) {
            throw SQLException("Length must be non-negative (got $length)")
        }
        val startIndex = (pos - 1).toInt()
        if (startIndex < 0 || startIndex > content.length) {
            throw SQLException("Position $pos is out of bounds (length=${content.length})")
        }
        val endIndex = minOf(startIndex + length, content.length)
        return content.substring(startIndex, endIndex)
    }

    override fun getCharacterStream(): Reader {
        checkNotFreed()
        return StringReader(content.toString())
    }

    override fun getCharacterStream(pos: Long, length: Long): Reader {
        checkNotFreed()
        val substring = getSubString(pos, length.toInt())
        return StringReader(substring)
    }

    override fun getAsciiStream(): InputStream {
        checkNotFreed()
        return ByteArrayInputStream(content.toString().toByteArray(Charsets.US_ASCII))
    }

    override fun position(searchstr: String, start: Long): Long {
        checkNotFreed()
        if (start < 1) {
            throw SQLException("Start position must be >= 1 (got $start)")
        }
        val startIndex = (start - 1).toInt()
        if (startIndex >= content.length) {
            return -1L
        }
        val index = content.indexOf(searchstr, startIndex)
        return if (index >= 0) {
            (index + 1).toLong()
        } else {
            -1L
        }
    }

    override fun position(searchstr: Clob, start: Long): Long {
        checkNotFreed()
        val searchString = searchstr.getSubString(1, searchstr.length().toInt())
        return position(searchString, start)
    }

    override fun setString(pos: Long, str: String): Int {
        checkNotFreed()
        return setString(pos, str, 0, str.length)
    }

    override fun setString(pos: Long, str: String, offset: Int, len: Int): Int {
        checkNotFreed()
        if (pos < 1) {
            throw SQLException("Position must be >= 1 (got $pos)")
        } else if (offset < 0 || offset > str.length) {
            throw SQLException("Offset $offset is out of bounds for string of length ${str.length}")
        } else if (len < 0 || offset + len > str.length) {
            throw SQLException("Length $len with offset $offset exceeds string length ${str.length}")
        }
        val startIndex = (pos - 1).toInt()
        val substring = str.substring(offset, offset + len)
        while (content.length < startIndex) {
            content.append(' ')
        }
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
        if (pos < 1) {
            throw SQLException("Position must be >= 1 (got $pos)")
        }
        val startIndex = (pos - 1).toInt()
        return object : Writer() {
            override fun write(cbuf: CharArray, off: Int, len: Int) {
                checkNotFreed()
                while (content.length < startIndex) {
                    content.append(' ')
                }
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
        if (pos < 1) {
            throw SQLException("Position must be >= 1 (got $pos)")
        }
        val startIndex = (pos - 1).toInt()
        return object : OutputStream() {
            private var currentPos = startIndex

            override fun write(b: Int) {
                checkNotFreed()
                while (content.length < currentPos) {
                    content.append(' ')
                }
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
                while (content.length < currentPos) {
                    content.append(' ')
                }
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
            throw SQLException("Length must be non-negative (got $len)")
        } else if (len >= content.length) {
            return
        }
        content.setLength(len.toInt())
    }

    override fun free() {
        if (freed.compareAndSet(false, true)) {
            content.clear()
            content.trimToSize()
        }
    }

    private fun checkNotFreed() {
        if (freed.get()) {
            throw SQLException("Clob has been freed")
        }
    }

    internal fun asString(): String {
        checkNotFreed()
        return content.toString()
    }
}
