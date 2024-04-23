/*
 * Copyright 2020 Bloomberg Finance L.P.
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

import java.lang.StringBuilder
import javax.annotation.concurrent.NotThreadSafe

@NotThreadSafe
class ManagedStringBuilder(
    private val defaultLength: Int = 128,
    private val maxRetainedLength: Int = 512
) {
    init {
        require(maxRetainedLength > 0)
    }

    @get:JvmSynthetic @set:JvmSynthetic
    @PublishedApi
    @JvmField
    internal var builder = StringBuilder(defaultLength)

    inline fun <T> use(block: StringBuilder.() -> T) = try {
        block(builder)
    } finally {
        if (validate()) {
            passivate()
        } else {
            reset()
        }
    }

    @JvmSynthetic
    @PublishedApi
    internal fun validate() = builder.length <= maxRetainedLength

    @JvmSynthetic
    @PublishedApi
    internal fun passivate() {
        builder.clear()
    }

    @JvmSynthetic
    @PublishedApi
    internal fun reset() {
        builder = StringBuilder(defaultLength)
    }
}
