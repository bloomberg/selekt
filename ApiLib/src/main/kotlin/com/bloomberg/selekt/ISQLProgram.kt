/*
 * Copyright 2021 Bloomberg Finance L.P.
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

import java.io.Closeable

interface ISQLProgram : Closeable {
    /**
     * Bind a byte array value to this statement. The value remains bound until [clearBindings] is called.
     *
     * @param index The 1-based index to the parameter to bind.
     * @param value The value to bind.
     */
    fun bindBlob(index: Int, value: ByteArray)

    /**
     * Bind a double value to this statement. The value remains bound until [clearBindings] is called.
     *
     * @param index The 1-based index to the parameter to bind.
     * @param value The value to bind.
     */
    fun bindDouble(index: Int, value: Double)

    /**
     * Bind an integer value to this statement. The value remains bound until [clearBindings] is called.
     *
     * @param index The 1-based index to the parameter to bind.
     * @param value The value to bind.
     */
    fun bindInt(index: Int, value: Int)

    /**
     * Bind a long value to this statement. The value remains bound until [clearBindings] is called.
     *
     * @param index The 1-based index to the parameter to bind.
     * @param value The value to bind.
     */
    fun bindLong(index: Int, value: Long)

    /**
     * Bind a null value to this statement. The value remains bound until [clearBindings] is called.
     *
     * @param index The 1-based index to the parameter to bind.
     */
    fun bindNull(index: Int)

    /**
     * Bind a String value to this statement. The value remains bound until [clearBindings] is called.
     *
     * @param index The 1-based index to the parameter to bind.
     * @param value The value to bind.
     */
    fun bindString(index: Int, value: String)

    /**
     * Clears all existing bindings. Unset bindings are treated as null.
     */
    fun clearBindings()
}
