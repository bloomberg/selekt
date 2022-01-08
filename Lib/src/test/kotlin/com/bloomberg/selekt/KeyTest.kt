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

package com.bloomberg.selekt

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

internal class KeyTest {
    @Test
    fun makesCopy() {
        val raw = ByteArray(32) { 0x42 }
        Key(raw).use {
            assertEquals(raw.first(), it.first())
            assertEquals(raw.size, it.size)
            assertNotSame(raw, it)
        }
    }

    @Test
    fun zerosAfterUse() {
        assertEquals(0, Key(ByteArray(32) { 0x42 }).use { it }.first())
    }

    @Test
    fun zeroThenUseThrows() {
        Key(ByteArray(32) { 0x42 }).apply {
            zero()
            assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
                use {}
            }
        }
    }
}
