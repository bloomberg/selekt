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

package com.bloomberg.selekt.jdbc.metadata

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

internal class JdbcPatternRegexTest {
    @Test
    fun percentMatchesAnything(): Unit = "%".toJdbcPatternRegex().let {
        assertTrue(it.matches("anything"))
        assertTrue(it.matches(""))
    }

    @Test
    fun literalMatchesExact(): Unit = "my_table".toJdbcPatternRegex().let {
        assertTrue(it.matches("myXtable"))
        assertFalse(it.matches("my_table_extra"))
    }

    @Test
    fun underscoreMatchesSingleCharacter(): Unit = "a_c".toJdbcPatternRegex().let {
        assertTrue(it.matches("abc"))
        assertTrue(it.matches("axc"))
        assertFalse(it.matches("ac"))
        assertFalse(it.matches("abbc"))
    }

    @Test
    fun prefixWildcard(): Unit = "user_%".toJdbcPatternRegex().let {
        assertTrue(it.matches("userXname"))
        assertTrue(it.matches("userXfoo_bar"))
        assertFalse(it.matches("user"))
    }

    @Test
    fun regexMetacharactersAreEscaped(): Unit = "my.table".toJdbcPatternRegex().let {
        assertFalse(it.matches("myXtable"))
        assertTrue(it.matches("my.table"))
    }

    @Test
    fun parenthesesAreEscaped(): Unit = "col(1)".toJdbcPatternRegex().let {
        assertTrue(it.matches("col(1)"))
        assertFalse(it.matches("col1"))
    }

    @Test
    fun bracketsAreEscaped(): Unit = "col[0]".toJdbcPatternRegex().let {
        assertTrue(it.matches("col[0]"))
        assertFalse(it.matches("col0"))
    }

    @Test
    fun pipeIsEscaped(): Unit = "a|b".toJdbcPatternRegex().let {
        assertTrue(it.matches("a|b"))
        assertFalse(it.matches("a"))
        assertFalse(it.matches("b"))
    }

    @Test
    fun maliciousReDoSPatternIsSafe(): Unit = "(.*){20}a%".toJdbcPatternRegex().let {
        assertTrue(it.matches("(.*){20}ahello"))
        assertFalse(it.matches("hello"))
    }

    @Test
    fun emptyPatternMatchesEmpty(): Unit = "".toJdbcPatternRegex().let {
        assertTrue(it.matches(""))
        assertFalse(it.matches("x"))
    }
}
