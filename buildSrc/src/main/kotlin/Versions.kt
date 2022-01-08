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

import java.net.URL

enum class Versions(
    val version: String,
    private val url: URL
) {
    ANDROID_BENCHMARK_GRADLE_PLUGIN("1.1.0-alpha06", URL("https://developer.android.com/studio/profile/benchmark")),
    ANDROID_BUILD_TOOLS("31.0.0", URL("https://developer.android.com/studio/releases/build-tools")),
    ANDROID_GRADLE_PLUGIN("7.0.4", URL("https://developer.android.com/tools/revisions/gradle-plugin.html")),
    ANDROID_LINT("30.0.2", URL("https://github.com/googlesamples/android-custom-lint-rules")),
    ANDROID_NDK("23.1.7779620", URL("https://developer.android.com/ndk")),
    ANDROID_SDK("31", URL("https://developer.android.com/sdk")),
    ANDROIDX_LIVE_DATA("2.2.0", URL("https://developer.android.com/topic/libraries/architecture/livedata")),
    ANDROIDX_ROOM("2.2.5", URL("https://developer.android.com/jetpack/androidx/releases/room")),
    ASSERT_J("3.22.0", URL("https://joel-costigliola.github.io/assertj")),
    CMAKE("3.18.1", URL("https://cmake.org")),
    DETEKT("1.19.0", URL("https://github.com/arturbosch/detekt")),
    DOKKA("1.6.10", URL("https://github.com/Kotlin/dokka")),
    GRADLE_LICENSEE_PLUGIN("1.1.0", URL("https://github.com/cashapp/licensee")),
    JACOCO("0.8.7", URL("https://www.jacoco.org/jacoco/trunk/doc/changes.html")),
    JMH("1.34", URL("https://openjdk.java.net/projects/code-tools/jmh/")),
    JSR_305("3.0.2", URL("https://code.google.com/archive/p/jsr-305/")),
    JUNIT4("4.13.1", URL("https://github.com/junit-team/junit4")),
    JUNIT5("5.7.2", URL("https://junit.org/junit5/")),
    JUNIT5_PLATFORM("1.7.2", URL("https://junit.org/junit5/")),
    KOTLIN("1.6.10", URL("https://github.com/JetBrains/kotlin")),
    KOTLIN_COROUTINES("1.5.2", URL("https://github.com/Kotlin/kotlinx.coroutines")),
    KOTLIN_TEST("1.4.32", URL("https://github.com/JetBrains/kotlin")),
    KOTLINX_KOVER("0.5.0-RC", URL("https://github.com/Kotlin/kotlinx-kover")),
    KTLINT("0.43.2", URL("https://github.com/pinterest/ktlint")),
    KTLINT_GRADLE_PLUGIN("10.2.1", URL("https://github.com/JLLeitschuh/ktlint-gradle")),
    MOCKITO("4.0.0", URL("https://github.com/mockito/mockito")),
    MOCKITO_KOTLIN(MOCKITO.version, URL("https://github.com/mockito/mockito-kotlin")),
    NEXUS_PLUGIN("1.1.0", URL("https://github.com/gradle-nexus/publish-plugin")),
    ROBOLECTRIC("4.7.3", URL("https://github.com/robolectric/robolectric"));

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
