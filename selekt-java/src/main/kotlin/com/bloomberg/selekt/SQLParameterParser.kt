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

private const val DECIMAL_RADIX = 10
private const val INVALID_PARAMETER_INDEX = -1

/**
 * @since 0.28.0
 */
/**
 * Parses SQL to extract named parameter positions.
 *
 * SQLite supports these named parameter forms:
 * - :name (colon prefix)
 * - @name (at sign prefix)
 * - $name (dollar sign prefix)
 *
 * Parameter positions follow SQLite's slot-numbering rules. Explicitly numbered parameters (`?NNN`) use slot NNN,
 * anonymous parameters (`?`) and new named parameters use the next slot after the highest slot seen so far, and a
 * repeated named parameter reuses its original slot.
 *
 * This parser handles:
 * - String literals (single quotes), parameters inside are ignored
 * - Identifiers (double quotes or backticks), parameters inside are ignored
 * - Comments (-- line comments and block comments), parameters inside are ignored
 */
@Suppress("Detekt.CognitiveComplexMethod")
internal fun parseNamedParameters(sql: String): Map<String, Int> {
    val result = mutableMapOf<String, Int>()
    var parameterIndex = 0
    var i = 0
    while (i < sql.length) {
        when (sql[i]) {
            '\'' -> i = skipStringLiteral(sql, i, '\'')
            '"' -> i = skipStringLiteral(sql, i, '"')
            '`' -> i = skipStringLiteral(sql, i, '`')
            '[' -> i = skipBracketIdentifier(sql, i)
            '-' if i + 1 < sql.length && sql[i + 1] == '-' -> i = skipLineComment(sql, i)
            '/' if i + 1 < sql.length && sql[i + 1] == '*' -> i = skipBlockComment(sql, i)
            '?' -> if (i + 1 < sql.length && sql[i + 1].isDigit()) {
                val endIndex = skipDigits(sql, i + 1)
                val explicitIndex = parseParameterIndex(sql, i + 1, endIndex)
                parameterIndex = maxOf(parameterIndex, explicitIndex)
                i = endIndex
            } else {
                parameterIndex = nextParameterIndex(parameterIndex)
                i++
            }
            ':', '@', '$' -> {
                val startIndex = i++
                while (i < sql.length && sql[i].isParameterNameChar()) {
                    i++
                }
                if (i - startIndex > 1) {
                    val name = sql.substring(startIndex, i)
                    parameterIndex = result.putNamedParameter(name, parameterIndex)
                }
            }
            else -> i++
        }
    }
    return result
}

private fun MutableMap<String, Int>.putNamedParameter(name: String, highestIndex: Int): Int {
    return if (name in this) {
        highestIndex
    } else {
        val nextIndex = nextParameterIndex(highestIndex)
        put(name, nextIndex)
        nextIndex
    }
}

private fun nextParameterIndex(highestIndex: Int): Int {
    require(highestIndex < Int.MAX_VALUE) { "Parameter slot index exceeds Int.MAX_VALUE." }
    return highestIndex + 1
}

private fun parseParameterIndex(sql: String, startIndex: Int, endIndex: Int): Int {
    var value = 0
    var index = startIndex
    while (index < endIndex) {
        val digit = Character.digit(sql[index], DECIMAL_RADIX)
        if (digit < 0 || value > (Int.MAX_VALUE - digit) / DECIMAL_RADIX) {
            return INVALID_PARAMETER_INDEX
        }
        value = value * DECIMAL_RADIX + digit
        ++index
    }
    return value
}

private fun Char.isParameterNameChar(): Boolean = isLetterOrDigit() || this == '_'

private fun skipStringLiteral(sql: String, start: Int, quote: Char): Int {
    var i = start + 1
    while (i < sql.length) {
        if (sql[i] == quote) {
            if (i + 1 < sql.length && sql[i + 1] == quote) {
                i += 2
            } else {
                return i + 1
            }
        } else {
            i++
        }
    }
    return i
}

private fun skipBracketIdentifier(sql: String, start: Int): Int {
    var i = start + 1
    while (i < sql.length && sql[i] != ']') {
        i++
    }
    return if (i < sql.length) i + 1 else i
}

private fun skipLineComment(sql: String, start: Int): Int {
    var i = start + 2
    while (i < sql.length && sql[i] != '\n') {
        i++
    }
    return if (i < sql.length) i + 1 else i
}

private fun skipBlockComment(sql: String, start: Int): Int {
    var i = start + 2
    while (i + 1 < sql.length) {
        if (sql[i] == '*' && sql[i + 1] == '/') {
            return i + 2
        }
        i++
    }
    return sql.length
}

private fun skipDigits(sql: String, start: Int): Int {
    var i = start
    while (i < sql.length && sql[i].isDigit()) {
        i++
    }
    return i
}
