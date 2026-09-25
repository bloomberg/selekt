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

package com.bloomberg.selekt.jdbc

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Wrapper

/**
 * Kotlin shorthand for [Wrapper.unwrap].
 *
 * @since 1.6.9
 */
@Throws(SQLException::class)
inline fun <reified T : Any> Wrapper.unwrap(): T = unwrap(T::class.java)

/**
 * Kotlin shorthand for [Wrapper.isWrapperFor].
 *
 * @since 1.6.9
 */
@Throws(SQLException::class)
inline fun <reified T : Any> Wrapper.isWrapperFor(): Boolean = isWrapperFor(T::class.java)

/**
 * Kotlin shorthand for [ResultSet.getObject] using a column index.
 *
 * @since 1.6.9
 */
@Throws(SQLException::class)
inline fun <reified T : Any> ResultSet.getObject(columnIndex: Int): T? = getObject(columnIndex, T::class.java)

/**
 * Kotlin shorthand for [ResultSet.getObject] using a column label.
 *
 * @since 1.6.9
 */
@Throws(SQLException::class)
inline fun <reified T : Any> ResultSet.getObject(columnLabel: String): T? = getObject(columnLabel, T::class.java)
