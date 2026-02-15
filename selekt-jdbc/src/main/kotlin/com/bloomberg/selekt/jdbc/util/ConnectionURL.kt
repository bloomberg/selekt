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

package com.bloomberg.selekt.jdbc.util

import java.net.URLDecoder
import java.sql.SQLException
import java.util.Properties

/**
 * Supported format: jdbc:sqlite:path/to/database.sqlite[?property=value&...]
 *
 * Supported properties:
 * - key: Encryption key (hex string or file path)
 * - poolSize: Maximum connection pool size (integer)
 * - busyTimeout: SQLite busy timeout in milliseconds (integer)
 * - journalMode: SQLite journal mode (DELETE, WAL, MEMORY, etc.)
 * - foreignKeys: Enable foreign key constraints (true/false)
 */
@Suppress("TooGenericExceptionCaught")
internal class ConnectionURL private constructor(
    val databasePath: String,
    val properties: Properties
) {
    companion object {
        private const val JDBC_PREFIX = "jdbc:"
        private const val SELEKT_SUBPROTOCOL = "sqlite:"
        private const val FULL_PREFIX = "$JDBC_PREFIX$SELEKT_SUBPROTOCOL"

        @JvmStatic
        fun parse(url: String): ConnectionURL {
            if (!url.startsWith(FULL_PREFIX)) {
                throw SQLException(
                    "Invalid JDBC URL format. Expected format: jdbc:sqlite:path/to/database.sqlite[?properties...]"
                )
            }
            return runCatching {
                parsePathAndProperties(url.substring(FULL_PREFIX.length)).run {
                    ConnectionURL(first, second)
                }
            }.getOrElse {
                throw SQLException("Failed to parse JDBC URL: $url", it)
            }
        }

        @JvmStatic
        fun isValidUrl(url: String?): Boolean {
            if (url == null || !url.startsWith(FULL_PREFIX)) {
                return false
            }
            val pathPart = url.substring(FULL_PREFIX.length)
            val questionMarkIndex = pathPart.indexOf('?')
            val databasePath = if (questionMarkIndex == -1) {
                pathPart
            } else {
                pathPart.substring(0, questionMarkIndex)
            }
            return databasePath.isNotBlank()
        }

        private fun parsePathAndProperties(urlPart: String): Pair<String, Properties> {
            val questionMarkIndex = urlPart.indexOf('?')
            val databasePath = if (questionMarkIndex == -1) {
                urlPart
            } else {
                urlPart.substring(0, questionMarkIndex)
            }
            require(databasePath.isNotBlank()) { "Database path cannot be empty" }
            val properties = Properties()
            if (questionMarkIndex != -1 && questionMarkIndex < urlPart.length - 1) {
                val queryString = urlPart.substring(questionMarkIndex + 1)
                parseQueryString(queryString, properties)
            }
            return databasePath to properties
        }

        private fun parseQueryString(queryString: String, properties: Properties) {
            queryString.split('&').forEach { param ->
                val index = param.indexOf('=')
                if (index != -1) {
                    val key = param.substring(0, index).trim()
                    if (key.isNotEmpty()) {
                        properties.setProperty(
                            key,
                            URLDecoder.decode(param.substring(index + 1).trim(), Charsets.UTF_8)
                        )
                    }
                }
            }
        }
    }

    fun getProperty(key: String): String? = properties.getProperty(key)

    fun getProperty(key: String, defaultValue: String): String = properties.getProperty(key, defaultValue)

    fun getBooleanProperty(
        key: String,
        defaultValue: Boolean = false)
    : Boolean = (properties.getProperty(key) ?: return defaultValue).let {
        it.equals("true", ignoreCase = true) || it == "1"
    }

    fun getIntProperty(
        key: String,
        defaultValue: Int = 0
    ): Int = (properties.getProperty(key) ?: return defaultValue).toInt()

    override fun toString(): String = "$FULL_PREFIX$databasePath" +
        "${if (properties.isNotEmpty()) { "?" } else { "" } }${propertiesToQueryString()}"

    private fun propertiesToQueryString(): String = properties.entries.joinToString("&") { (key, value) ->
        "$key=${java.net.URLEncoder.encode(value.toString(), "UTF-8")}"
    }
}
