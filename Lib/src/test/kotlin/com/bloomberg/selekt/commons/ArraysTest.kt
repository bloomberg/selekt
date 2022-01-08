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

package com.bloomberg.selekt.commons

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ArraysTest {
    @Test
    fun joinToWithComma() {
        StringBuilder().let {
            arrayOf("a", "b").joinTo(it, ',')
            assertEquals("a,b", it.toString())
        }
    }

    @Test
    fun charsJoinToWithComma() {
        StringBuilder().let {
            arrayOf('a', 'b').joinTo(it, ',')
            assertEquals("a,b", it.toString())
        }
    }

    @Test
    fun mixedJoinToWithComma() {
        StringBuilder().let {
            arrayOf('a', "b").joinTo(it, ',')
            assertEquals("a,b", it.toString())
        }
    }

    @Test
    fun nullJoinToWithComma() {
        StringBuilder().let {
            arrayOf(null, "a").joinTo(it, ',')
            assertEquals("null,a", it.toString())
        }
    }

    @Test
    fun intJoinToWithComma() {
        StringBuilder().let {
            arrayOf(1, 2).joinTo(it, ',')
            assertEquals("1,2", it.toString())
        }
    }

    @Test
    fun emptyJoinTo() {
        StringBuilder().let {
            emptyArray<String>().joinTo(it, ',')
            assertEquals("", it.toString())
        }
    }

    @Test
    fun forEachByIndex() {
        arrayOf(42).forEachByIndex { i, it ->
            assertEquals(0, i)
            assertEquals(42, it)
        }
    }

    @Test
    fun forEachByPosition() {
        arrayOf(42).forEachByPosition { it, i ->
            assertEquals(1, i)
            assertEquals(42, it)
        }
    }
}
