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

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("androidx.benchmark:benchmark-gradle-plugin:${Versions.ANDROID_BENCHMARK_GRADLE_PLUGIN}")
    }
}

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.kotlinx.kover")
}

apply {
    plugin("androidx.benchmark")
}

repositories {
    mavenCentral()
    google()
}

android {
    compileSdkVersion(Versions.ANDROID_SDK.version.toInt())
    buildToolsVersion(Versions.ANDROID_BUILD_TOOLS.version)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(32)

        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
        testInstrumentationRunnerArguments.putAll(arrayOf(
            "androidx.benchmark.suppressErrors" to "EMULATOR,LOW_BATTERY,UNLOCKED"
        ))
    }

    arrayOf("androidTest").forEach {
        sourceSets[it].java.srcDir("src/$it/kotlin")
    }

    configurations.all {
        resolutionStrategy {
            // FIXME Please remove as soon as the project compiles without.
            force("org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}")
            force("org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.KOTLIN}")
        }
    }

    lintOptions {
        disable("OldTargetApi")
    }
}

dependencies {
    androidTestImplementation(project(":AndroidLib"))
    androidTestImplementation("junit:junit:${Versions.JUNIT4}")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation(androidX("benchmark", "junit4", "1.0.0"))
    androidTestImplementation(kotlin("test", Versions.KOTLIN.version))
    androidTestImplementation(kotlin("test-junit", Versions.KOTLIN.version))
    androidTestImplementation(kotlinX("coroutines-core", Versions.KOTLIN_COROUTINES.version))
    androidTestImplementation(kotlinX("coroutines-jdk8", Versions.KOTLIN_COROUTINES.version))
}

kover {
    isDisabled.set(true)
}
