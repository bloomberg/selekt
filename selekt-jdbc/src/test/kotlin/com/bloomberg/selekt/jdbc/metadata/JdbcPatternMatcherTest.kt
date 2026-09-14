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

import java.time.Duration
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively

internal class JdbcPatternMatcherTest {
    @Test
    fun percentMatchesAnything(): Unit = JdbcPatternMatcher("%").let {
        assertTrue(it.matches("anything"))
        assertTrue(it.matches(""))
    }

    @Test
    fun literalMatchesExact(): Unit = JdbcPatternMatcher("my_table").let {
        assertTrue(it.matches("myXtable"))
        assertFalse(it.matches("my_table_extra"))
    }

    @Test
    fun underscoreMatchesSingleCharacter(): Unit = JdbcPatternMatcher("a_c").let {
        assertTrue(it.matches("abc"))
        assertTrue(it.matches("axc"))
        assertFalse(it.matches("ac"))
        assertFalse(it.matches("abbc"))
    }

    @Test
    fun prefixWildcard(): Unit = JdbcPatternMatcher("user_%").let {
        assertTrue(it.matches("userXname"))
        assertTrue(it.matches("userXfoo_bar"))
        assertFalse(it.matches("user"))
    }

    @Test
    fun multipleWildcardsCanMatchEmptyOrNonEmptyText(): Unit = JdbcPatternMatcher("%ab%%ac%").let {
        assertTrue(it.matches("abac"))
        assertTrue(it.matches("prefixabmiddleacsuffix"))
        assertFalse(it.matches("prefixacmiddleab"))
    }

    @Test
    fun regexMetacharactersAreLiteral(): Unit = JdbcPatternMatcher("my.table").let {
        assertFalse(it.matches("myXtable"))
        assertTrue(it.matches("my.table"))
    }

    @Test
    fun parenthesesAreLiteral(): Unit = JdbcPatternMatcher("col(1)").let {
        assertTrue(it.matches("col(1)"))
        assertFalse(it.matches("col1"))
    }

    @Test
    fun bracketsAreLiteral(): Unit = JdbcPatternMatcher("col[0]").let {
        assertTrue(it.matches("col[0]"))
        assertFalse(it.matches("col0"))
    }

    @Test
    fun pipeIsLiteral(): Unit = JdbcPatternMatcher("a|b").let {
        assertTrue(it.matches("a|b"))
        assertFalse(it.matches("a"))
        assertFalse(it.matches("b"))
    }

    @Test
    fun regexSyntaxIsLiteral(): Unit = JdbcPatternMatcher("(.*){20}a%").let {
        assertTrue(it.matches("(.*){20}ahello"))
        assertFalse(it.matches("hello"))
    }

    @Test
    fun emptyPatternMatchesEmpty(): Unit = JdbcPatternMatcher("").let {
        assertTrue(it.matches(""))
        assertFalse(it.matches("x"))
    }

    @Test
    fun searchEscapeMakesWildcardsLiteral(): Unit = JdbcPatternMatcher("value\\_\\%\\\\").let {
        assertTrue(it.matches("value_%\\"))
        assertFalse(it.matches("valueXA\\"))
    }

    @Test
    fun trailingSearchEscapeIsLiteral(): Unit = JdbcPatternMatcher("value\\").let {
        assertTrue(it.matches("value\\"))
        assertFalse(it.matches("value"))
    }

    @Test
    fun underscoreMatchesOneUnicodeCodePoint(): Unit = JdbcPatternMatcher("a_b").let {
        assertTrue(it.matches("a\uD83D\uDE00b"))
        assertFalse(it.matches("a\uD83D\uDE00\uD83D\uDE00b"))
    }

    @Test
    fun wildcardGeneratedBacktrackingIsBounded() {
        val matcher = JdbcPatternMatcher("%a".repeat(30) + "b")
        assertTimeoutPreemptively(Duration.ofMillis(250)) {
            assertFalse(matcher.matches("a".repeat(30) + "c"))
        }
    }

    @Test
    fun veryLongPatternsHaveBoundedTokenisation() {
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            val literal = "a".repeat(100_000)
            assertTrue(JdbcPatternMatcher(literal).matches(literal))
            assertTrue(JdbcPatternMatcher("%".repeat(100_000)).matches("anything"))
        }
    }
}
