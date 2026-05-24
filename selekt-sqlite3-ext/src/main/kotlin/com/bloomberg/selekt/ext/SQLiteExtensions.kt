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

package com.bloomberg.selekt.ext

/**
 * @property libraryPath path to the shared library implementing the extension.
 * @property entryPoint optional entry point symbol. If null, SQLite resolves the default symbol.
 * @since 0.34.0
 */
data class SQLiteExtension(
    val libraryPath: String,
    val entryPoint: String? = null
)

/**
 * @since 0.34.0
 */
object SQLiteExtensionSql {
    @JvmStatic
    fun loadExtensionStatement(extension: SQLiteExtension): String {
        require(extension.libraryPath.isNotBlank()) { "Extension path must not be blank." }
        val pathLiteral = extension.libraryPath.asSqlLiteral()
        val entryPointClause = extension.entryPoint?.let { ", ${it.asSqlLiteral()}" }.orEmpty()
        return "SELECT load_extension($pathLiteral$entryPointClause)"
    }

    private fun String.asSqlLiteral(): String = "'${replace("'", "''")}'"
}

/**
 * @since 0.34.0
 */
object Vec1Sql {
    const val INFO_SQL = "SELECT vec1_info()"
}
