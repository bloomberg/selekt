/*
 * Copyright 2020 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.pools

internal val anyKeyedObject = KeyedObject()

internal fun <T : IKeyedObject<String>> IObjectPool<String, T>.borrowObject() = borrowObject(anyKeyedObject.key)

internal class KeyedObject(
    override val isPrimary: Boolean = false,
    @Volatile var key: String = Thread.currentThread().id.toString()
) : IKeyedObject<String> {
    override fun matches(key: String) = key == this.key
}
