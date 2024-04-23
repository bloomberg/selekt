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

import java.net.URL

enum class Versions(
    val version: String,
    private val url: URL
) {
    ANDROID_SDK("34", URL("https://developer.android.com/sdk")),
    JMH("1.36", URL("https://openjdk.java.net/projects/code-tools/jmh/"));

    override fun toString() = version

    private companion object {
        init {
            checkLexicographicOrder()
        }

        private fun checkLexicographicOrder() {
            values().map { it.name.replace('_', '-') }.run {
                slice(1 until size).forEachIndexed { index, version ->
                    check(this[index] < version) {
                        "Versions are not listed lexicographically near '${values()[index + 1].name}'."
                    }
                }
            }
        }
    }
}
