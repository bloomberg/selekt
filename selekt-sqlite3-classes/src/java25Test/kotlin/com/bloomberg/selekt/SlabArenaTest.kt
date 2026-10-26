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

import java.lang.foreign.ValueLayout.JAVA_BYTE
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

internal class SlabArenaTest {
    @Test
    fun `C string allocations reject embedded NUL`() {
        SlabArena().use { slab ->
            listOf("ascii\u0000suffix", "café\u0000suffix").forEach { value ->
                val failure = assertFailsWith<IllegalArgumentException> { slab.allocateFrom(value) }

                assertEquals("Native C strings must not contain NUL.", failure.message)
            }
        }
    }

    @Test
    fun `C string allocations preserve Unicode and malformed surrogates`() {
        SlabArena().use { slab ->
            listOf("café \uD83C\uDF0D", "unpaired \uD800 surrogate").forEach { value ->
                val expected = value.toByteArray(Charsets.UTF_8).toString(Charsets.UTF_8)

                assertEquals(expected, slab.allocateFrom(value).getString(0))
            }
        }
    }

    @Test
    fun growthRetainsEarlierAllocationsUntilReset() {
        SlabArena(capacity = 1).use { slab ->
            val earlierAllocation = slab.allocate(1).also { it.set(JAVA_BYTE, 0, 42) }

            slab.allocate(2)

            assertEquals(42, earlierAllocation.get(JAVA_BYTE, 0))
            slab.reset()
            assertFailsWith<IllegalStateException> { earlierAllocation.get(JAVA_BYTE, 0) }
        }
    }
}
