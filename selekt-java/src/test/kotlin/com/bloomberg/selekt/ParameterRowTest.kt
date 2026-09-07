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

import kotlin.test.assertContentEquals
import org.junit.jupiter.api.Test

internal class ParameterRowTest {
    @Test
    fun clearZerosEveryBackingStore() {
        val row = ParameterRow(4).apply {
            setInt(0, Int.MAX_VALUE)
            setLong(1, Long.MAX_VALUE)
            setDouble(2, Double.MAX_VALUE)
            setObject(3, "sensitive-value")
        }
        row.clear()
        assertContentEquals(ByteArray(4), row.tags)
        assertContentEquals(IntArray(4), row.ints)
        assertContentEquals(LongArray(4), row.longs)
        assertContentEquals(DoubleArray(4), row.doubles)
        assertContentEquals(arrayOfNulls<Any>(4), row.objects)
    }
}
