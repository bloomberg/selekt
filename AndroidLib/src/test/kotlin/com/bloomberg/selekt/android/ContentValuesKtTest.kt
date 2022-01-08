/*
 * Copyright 2022 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android

import android.content.ContentValues
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.AbstractMap.SimpleEntry
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class ContentValuesKtTest {
    @Test
    fun asSelektContentValuesGetString() {
        ContentValues().apply { put("a", "42") }.asSelektContentValues().run {
            assertFalse(entrySet.isEmpty())
        }
    }

    @Test
    fun asSelektContentValuesPutByte() {
        ContentValues().asSelektContentValues().apply {
            put("a", 42.toByte())
            entrySet.contains(SimpleEntry("a", 42.toByte()))
        }
    }

    @Test
    fun asSelektContentValuesPutByteArray() {
        val array = byteArrayOf()
        ContentValues().asSelektContentValues().apply {
            put("a", array)
            entrySet.contains(SimpleEntry("a", array))
        }
    }

    @Test
    fun asSelektContentValuesPutDouble() {
        ContentValues().asSelektContentValues().apply {
            put("a", 42.0)
            entrySet.contains(SimpleEntry("a", 42.0))
        }
    }

    @Test
    fun asSelektContentValuesPutFloat() {
        ContentValues().asSelektContentValues().apply {
            put("a", 42.0f)
            entrySet.contains(SimpleEntry("a", 42.0f))
        }
    }

    @Test
    fun asSelektContentValuesPutInt() {
        ContentValues().asSelektContentValues().apply {
            put("a", 42)
            entrySet.contains(SimpleEntry("a", 42))
        }
    }

    @Test
    fun asSelektContentValuesPutLong() {
        ContentValues().asSelektContentValues().apply {
            put("a", 42L)
            entrySet.contains(SimpleEntry("a", 42L))
        }
    }

    @Test
    fun asSelektContentValuesPutNull() {
        ContentValues().asSelektContentValues().apply {
            putNull("a")
            entrySet.contains(SimpleEntry("a", null))
        }
    }

    @Test
    fun asSelektContentValuesPutShort() {
        ContentValues().asSelektContentValues().apply {
            put("a", 42.toShort())
            entrySet.contains(SimpleEntry("a", 42.toShort()))
        }
    }

    @Test
    fun asSelektContentValuesPutString() {
        ContentValues().asSelektContentValues().apply {
            put("a", "42")
            entrySet.contains(SimpleEntry("a", "42"))
        }
    }

    @Test
    fun iterateOverNull() {
        ContentValues().apply { putNull("a") }.asSelektContentValues().run {
            for (entry in entrySet) {
                assertEquals("a", entry.key)
                assertNull(entry.value)
            }
        }
    }

    @Test
    fun isEmpty() {
        assertTrue(ContentValues().asSelektContentValues().isEmpty)
    }

    @Test
    fun isNotEmpty() {
        assertFalse(ContentValues().apply { putNull("a") }.asSelektContentValues().isEmpty)
    }

    @Test
    fun entrySetContains() {
        ContentValues().apply { put("a", 42) }.asSelektContentValues().run {
            assertTrue(entrySet.contains(SimpleEntry("a", 42)))
        }
    }

    @Test
    fun entrySetDoesNotContainKey() {
        ContentValues().apply { put("a", 42) }.asSelektContentValues().run {
            assertFalse(entrySet.contains(SimpleEntry("b", 42)))
        }
    }

    @Test
    fun entrySetDoesNotContainValue() {
        ContentValues().apply { put("a", 42) }.asSelektContentValues().run {
            assertFalse(entrySet.contains(SimpleEntry("a", 43)))
        }
    }

    @Test
    fun entrySetContainsAll() {
        ContentValues().apply {
            put("a", 42)
            put("b", 43)
        }.asSelektContentValues().run {
            assertTrue(entrySet.containsAll(setOf(SimpleEntry("a", 42), SimpleEntry("b", 43))))
        }
    }

    @Test
    fun entrySetDoesNotContainAll() {
        ContentValues().apply {
            put("a", 42)
            put("b", 43)
        }.asSelektContentValues().run {
            assertFalse(entrySet.containsAll(setOf(SimpleEntry("a", 42), SimpleEntry("c", 44))))
        }
    }

    @Test
    fun iteratorNextThrows() {
        val iterator = ContentValues().asSelektContentValues().entrySet.iterator()
        assertThatExceptionOfType(NoSuchElementException::class.java).isThrownBy {
            iterator.next()
        }
    }

    @Test
    fun emptyEntrySetSize() {
        ContentValues().asSelektContentValues().run {
            assertEquals(0, entrySet.size)
        }
    }

    @Test
    fun entrySetSize() {
        ContentValues().apply { put("a", 42) }.asSelektContentValues().run {
            assertEquals(1, entrySet.size)
        }
    }

    @Test
    fun putByte() {
        ContentValues().apply { put("a", 42.toByte()) }.asSelektContentValues().run {
            assertTrue(entrySet.contains(SimpleEntry("a", 42.toByte())))
        }
    }

    @Test
    fun putByteArray() {
        val blob = byteArrayOf(42.toByte())
        ContentValues().apply { put("a", blob) }.asSelektContentValues().run {
            assertTrue(entrySet.contains(SimpleEntry("a", blob)))
        }
    }

    @Test
    fun putInt() {
        ContentValues().apply { put("a", 42) }.asSelektContentValues().run {
            assertTrue(entrySet.contains(SimpleEntry("a", 42)))
        }
    }

    @Test
    fun putLong() {
        ContentValues().apply { put("a", 42L) }.asSelektContentValues().run {
            assertTrue(entrySet.contains(SimpleEntry("a", 42L)))
        }
    }

    @Test
    fun putShort() {
        ContentValues().apply { put("a", 42.toShort()) }.asSelektContentValues().run {
            assertTrue(entrySet.contains(SimpleEntry("a", 42.toShort())))
        }
    }

    @Test
    fun putString() {
        ContentValues().apply { put("a", "42") }.asSelektContentValues().run {
            assertTrue(entrySet.contains(SimpleEntry("a", "42")))
        }
    }

    @Test
    fun putNull() {
        ContentValues().apply { putNull("a") }.asSelektContentValues().run {
            assertTrue(entrySet.contains(SimpleEntry("a", null)))
        }
    }
}
