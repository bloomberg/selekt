/*
 * Copyright 2022 Bloomberg Finance L.P.
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
    ANDROID_BENCHMARK("1.2.0-alpha13", URL("https://developer.android.com/studio/profile/benchmark")),
    ANDROID_BUILD_TOOLS("33.0.1", URL("https://developer.android.com/studio/releases/build-tools")),
    ANDROID_GRADLE_PLUGIN("8.0.1", URL("https://developer.android.com/tools/revisions/gradle-plugin.html")),
    ANDROID_LINT("30.0.2", URL("https://github.com/googlesamples/android-custom-lint-rules")),
    ANDROID_NDK("25.2.9519653", URL("https://developer.android.com/ndk")),
    ANDROID_SDK("33", URL("https://developer.android.com/sdk")),
    ANDROIDX_LIVE_DATA("2.5.1", URL("https://developer.android.com/topic/libraries/architecture/livedata")),
    ANDROIDX_ROOM("2.5.1", URL("https://developer.android.com/jetpack/androidx/releases/room")),
    CMAKE("3.22.1", URL("https://cmake.org")),
    DETEKT("1.22.0", URL("https://github.com/arturbosch/detekt")),
    DOKKA("1.8.10", URL("https://github.com/Kotlin/dokka")),
    GRADLE_DOWNLOAD_TASK_PLUGIN("5.3.0", URL("https://github.com/michel-kraemer/gradle-download-task")),
    GRADLE_LICENSEE_PLUGIN("1.6.0", URL("https://github.com/cashapp/licensee")),
    IDE_EXT_GRADLE_PLUGIN("1.1.7", URL("https://github.com/JetBrains/gradle-idea-ext-plugin")),
    JMH("1.36", URL("https://openjdk.java.net/projects/code-tools/jmh/")),
    JSR_305("3.0.2", URL("https://code.google.com/archive/p/jsr-305/")),
    JUNIT4("4.13.2", URL("https://github.com/junit-team/junit4")),
    JUNIT5("5.9.2", URL("https://junit.org/junit5/")),
    KOTLIN("1.8.21", URL("https://github.com/JetBrains/kotlin")),
    KOTLIN_TEST(KOTLIN.version, URL("https://github.com/JetBrains/kotlin")),
    KOTLINX_COROUTINES("1.6.4", URL("https://github.com/Kotlin/kotlinx.coroutines")),
    KOTLINX_KOVER("0.6.1", URL("https://github.com/Kotlin/kotlinx-kover")),
    KTLINT("0.45.2", URL("https://github.com/pinterest/ktlint")),
    KTLINT_GRADLE_PLUGIN("11.0.0", URL("https://github.com/JLLeitschuh/ktlint-gradle")),
    MOCKITO("5.3.0", URL("https://github.com/mockito/mockito")),
    MOCKITO_KOTLIN("4.1.0", URL("https://github.com/mockito/mockito-kotlin")),
    NEXUS_PLUGIN("1.1.0", URL("https://github.com/gradle-nexus/publish-plugin")),
    QODANA_PLUGIN("0.1.12", URL("https://www.jetbrains.com/help/qodana/qodana-gradle-plugin.html")),
    ROBOLECTRIC_ANDROID_ALL("12.1-robolectric-8229987", URL("https://github.com/robolectric/robolectric"));

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
