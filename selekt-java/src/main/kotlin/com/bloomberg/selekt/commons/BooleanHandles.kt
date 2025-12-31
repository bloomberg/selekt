/*
 * Copyright 2025 Bloomberg Finance L.P.
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

/**
 * Creates an optimized boolean field handle using VarHandle when available, falling back to
 * atomic field updaters on older platforms. Uses int values (0=false, 1=true).
 */
@Suppress("FunctionName")
internal inline fun <reified T> BooleanHandle(
    fieldName: String
) = IntegerHandle<T>(fieldName)

internal inline fun <reified T> getBoolean(
    handle: Any,
    instance: T
): Boolean = getInt(handle, instance) != 0

internal inline fun <reified T> setBoolean(
    handle: Any,
    instance: T,
    value: Boolean
) = setInt(handle, instance, if (value) { 1 } else { 0 })

internal inline fun <reified T> compareAndSetBoolean(
    handle: Any,
    instance: T,
    expected: Boolean,
    updated: Boolean
): Boolean = compareAndSetInt(
    handle,
    instance,
    if (expected) { 1 } else { 0 },
    if (updated) { 1 } else { 0 }
)

internal inline fun <reified T> getAndSetBoolean(
    handle: Any,
    instance: T,
    value: Boolean
): Boolean = getAndSetInt(handle, instance, if (value) { 1 } else { 0 }) != 0
