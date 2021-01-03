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

import java.net.URL

enum class Versions(
    val version: String,
    private val url: URL
) {
    ANDROID_BUILD_TOOLS("30.0.0", URL("https://developer.android.com/studio/releases/build-tools")),
    ANDROID_GRADLE_PLUGIN("4.0.0", URL("https://developer.android.com/tools/revisions/gradle-plugin.html")),
    ANDROID_SDK("30", URL("https://developer.android.com/sdk")),
    ANDROIDX_LIVE_DATA("2.2.0", URL("https://developer.android.com/topic/libraries/architecture/livedata")),
    ANDROIDX_ROOM("2.2.5", URL("https://developer.android.com/jetpack/androidx/releases/room")),
    ANDROIDX_SQLITE("2.1.0", URL("https://developer.android.com/jetpack/androidx/releases/sqlite")),
    ASSERT_J("3.12.2", URL("https://joel-costigliola.github.io/assertj")),
    DOKKA("0.9.18", URL("https://github.com/Kotlin/dokka")),
    ESPRESSO_CORE("3.1.1",
        URL("https://android.googlesource.com/platform/frameworks/testing/+/android-support-test/espresso")),
    KOTLIN("1.3.72", URL("https://github.com/JetBrains/kotlin")),
    KOTLIN_COROUTINES("1.3.3", URL("https://github.com/Kotlin/kotlinx.coroutines")),
    KTLINT("0.35.0", URL("https://github.com/pinterest/ktlint")),
    JACOCO("0.8.6", URL("https://www.jacoco.org/jacoco/trunk/doc/changes.html")),
    JSR_305("3.0.2", URL("https://code.google.com/archive/p/jsr-305/")),
    JUNIT4("4.13", URL("https://github.com/junit-team/junit4")),
    JUNIT5("5.5.2", URL("https://junit.org/junit5/")),
    JUNIT5_PLATFORM("1.5.2", URL("https://junit.org/junit5/")),
    MOCKITO_ANDROID("3.0.0", URL("https://github.com/mockito/mockito")),
    MOCKITO_CORE("3.0.0", URL("https://github.com/mockito/mockito")),
    MOCKITO_KOTLIN("2.1.0", URL("https://github.com/nhaarman/mockito-kotlin")),
    ROBOLECTRIC("4.3.1", URL("https://github.com/robolectric/robolectric"));

    override fun toString() = version
}
