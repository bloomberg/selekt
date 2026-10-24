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

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class SQLParameterParserTest {
    @Test
    fun parseSimpleColonParameter() {
        assertEquals(
            mapOf(":name" to 1),
            parseNamedParameters("SELECT * FROM users WHERE name = :name")
        )
    }

    @Test
    fun parseSimpleAtParameter() {
        assertEquals(
            mapOf("@name" to 1),
            parseNamedParameters("SELECT * FROM users WHERE name = @name")
        )
    }

    @Test
    fun parseSimpleDollarParameter() {
        assertEquals(mapOf("\$name" to 1), parseNamedParameters(
            "SELECT * FROM users WHERE name = \$name"
        ))
    }

    @Test
    fun parseMultipleParameters() {
        assertEquals(
            mapOf(":name" to 1, ":minAge" to 2, "@city" to 3),
            parseNamedParameters("SELECT * FROM users WHERE name = :name AND age > :minAge AND city = @city")
        )
    }

    @Test
    fun parseMixedPositionalAndNamedParameters() {
        assertEquals(mapOf(":name" to 2), parseNamedParameters(
            "SELECT * FROM users WHERE id = ? AND name = :name AND age > ?"
        ))
    }

    @Test
    fun parseParameterInsideStringLiteralIgnored() {
        assertEquals(
            mapOf(":age" to 1),
            parseNamedParameters("SELECT * FROM users WHERE name = ':notAParam' AND age = :age")
        )
    }

    @Test
    fun parseParameterInsideDoubleQuotedIdentifierIgnored() {
        assertEquals(
            mapOf(":age" to 1),
            parseNamedParameters("SELECT * FROM \":notATable\" WHERE age = :age")
        )
    }

    @Test
    fun parseParameterInsideBacktickIdentifierIgnored() {
        assertEquals(
            mapOf(":age" to 1),
            parseNamedParameters("SELECT * FROM `:notATable` WHERE age = :age")
        )
    }

    @Test
    fun parseParameterInsideBracketIdentifierIgnored() {
        assertEquals(
            mapOf(":age" to 1),
            parseNamedParameters("SELECT * FROM [:notATable] WHERE age = :age")
        )
    }

    @Test
    fun parseParameterInsideLineCommentIgnored() {
        assertEquals(
            mapOf(":age" to 1),
            parseNamedParameters("SELECT * FROM users -- WHERE name = :notAParam\nWHERE age = :age")
        )
    }

    @Test
    fun parseParameterInsideBlockCommentIgnored() {
        assertEquals(
            mapOf(":age" to 1),
            parseNamedParameters("SELECT * FROM users /* WHERE name = :notAParam */ WHERE age = :age")
        )
    }

    @Test
    fun parseDuplicateParameterReturnsFirstIndex() {
        assertEquals(
            mapOf(":name" to 1),
            parseNamedParameters("SELECT * FROM users WHERE name = :name OR alias = :name")
        )
    }

    @Test
    fun repeatedNamedParameterReusesItsSlot() {
        assertEquals(
            mapOf(":x" to 1, ":y" to 2),
            parseNamedParameters("SELECT :x, :x, :y")
        )
    }

    @Test
    fun parseParameterWithUnderscores() {
        assertEquals(
            mapOf(":first_name" to 1),
            parseNamedParameters("SELECT * FROM users WHERE first_name = :first_name")
        )
    }

    @Test
    fun parseParameterWithNumbers() {
        assertEquals(
            mapOf(":id1" to 1, ":code2" to 2),
            parseNamedParameters("SELECT * FROM users WHERE id = :id1 AND code = :code2")
        )
    }

    @Test
    fun parseNumberedQuestionMark() {
        assertEquals(
            mapOf(":name" to 2),
            parseNamedParameters("SELECT * FROM users WHERE id = ?1 AND name = :name AND code = ?2")
        )
    }

    @Test
    fun explicitParameterNumberAdvancesHighestSlot() {
        assertEquals(mapOf(":name" to 6), parseNamedParameters("SELECT ?5, :name"))
    }

    @Test
    fun explicitParameterNumberSupportsIntBoundary() {
        assertEquals(
            mapOf(":name" to Int.MAX_VALUE),
            parseNamedParameters("SELECT ?2147483646, :name")
        )
    }

    @Test
    fun overflowingExplicitParameterNumberDoesNotAdvanceHighestSlot() {
        assertEquals(mapOf(":name" to 1), parseNamedParameters("SELECT ?2147483648, :name"))
    }

    @Test
    fun namedParameterAfterMaximumSlotIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            parseNamedParameters("SELECT ?2147483647, :name")
        }
    }

    @Test
    fun anonymousParameterAfterMaximumSlotIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            parseNamedParameters("SELECT ?2147483647, ?")
        }
    }

    @Test
    fun explicitZeroDoesNotAdvanceHighestSlot() {
        assertEquals(mapOf(":name" to 1), parseNamedParameters("SELECT ?0, :name"))
    }

    @Test
    fun mixedParametersCannotAdvanceBeyondMaximumSlot() {
        assertFailsWith<IllegalArgumentException> {
            parseNamedParameters("SELECT ?2147483646, ?, :name")
        }
    }

    @Test
    fun repeatedNamedParameterCanReuseItsSlotAtMaximumSlot() {
        assertEquals(
            mapOf(":name" to 1),
            parseNamedParameters("SELECT :name, ?2147483647, :name")
        )
    }

    @Test
    fun explicitParameterBelowHighestSlotDoesNotMoveItBackwards() {
        assertEquals(mapOf(":name" to 6), parseNamedParameters("SELECT ?, ?, ?, ?, ?, ?2, :name"))
    }

    @Test
    fun anonymousParameterUsesSlotAfterHighestExplicitSlot() {
        assertEquals(mapOf(":name" to 7), parseNamedParameters("SELECT ?5, ?, :name"))
    }

    @Test
    fun bareNamedParameterPrefixesDoNotConsumeSlots() {
        assertEquals(mapOf(":name" to 1), parseNamedParameters("SELECT :, @, \$, :name"))
    }

    @Test
    fun parseEmptySql() {
        assertTrue(parseNamedParameters("").isEmpty())
    }

    @Test
    fun parseSqlWithNoParameters() {
        assertTrue(parseNamedParameters("SELECT * FROM users").isEmpty())
    }

    @Test
    fun parseEscapedQuoteInStringLiteral() {
        assertEquals(mapOf(":age" to 1), parseNamedParameters(
            "SELECT * FROM users WHERE name = 'O''Brien' AND age = :age"
        ))
    }

    @Test
    fun unterminatedQuotedTextAndCommentsContainNoParameters() {
        listOf(
            "SELECT ':ignored",
            "SELECT \"@ignored",
            "SELECT `\u0024ignored",
            "SELECT [:ignored",
            "SELECT 1 -- :ignored",
            "SELECT 1 /* :ignored"
        ).forEach { sql -> assertTrue(parseNamedParameters(sql).isEmpty(), sql) }
    }
}
