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

package com.bloomberg.selekt.commons

import com.bloomberg.selekt.annotations.Generated

@Generated("Jacoco does not report coverage for inline methods")
internal inline fun String.trimStartByIndex(predicate: (Char) -> Boolean): String {
    var i = 0
    while (i < length) {
        if (!predicate(this[i])) {
            return substring(i)
        }
        ++i
    }
    return ""
}