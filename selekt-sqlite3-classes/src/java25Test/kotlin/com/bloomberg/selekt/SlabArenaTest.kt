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
