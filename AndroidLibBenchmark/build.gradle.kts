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

plugins {
    id("com.android.library")
    id("kotlin-android")
    alias(libs.plugins.androidx.benchmark)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
    google()
}

android {
    compileSdkVersion(Versions.ANDROID_SDK.version.toInt())
    buildToolsVersion("34.0.0")
    namespace = "com.bloomberg.selekt.android.benchmark"
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(34)
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
        testInstrumentationRunnerArguments.putAll(arrayOf(
            "androidx.benchmark.suppressErrors" to "EMULATOR,LOW_BATTERY,UNLOCKED"
        ))
    }
    arrayOf("androidTest").forEach {
        sourceSets[it].java.srcDir("src/$it/kotlin")
    }
    lintOptions {
        disable("OldTargetApi")
    }
}

dependencies {
    androidTestImplementation(projects.selektAndroid)
    androidTestImplementation(projects.selektJava)
    androidTestImplementation(libs.androidx.benchmark.junit4)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.junit.junit)
    androidTestImplementation(libs.kotlin.test)
    androidTestImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.core)
}
