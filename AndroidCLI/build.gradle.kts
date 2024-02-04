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

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
    google()
}

android {
    compileSdk = Versions.ANDROID_SDK.version.toInt()
    buildToolsVersion = Versions.ANDROID_BUILD_TOOLS.version
    namespace = "com.bloomberg.selekt.cli"
    defaultConfig {
        applicationId = "com.bloomberg.selekt.cli"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "0.1"
        ndk {
            abiFilters.addAll(arrayOf("arm64-v8a", "x86_64"))
        }
    }
    arrayOf("main").forEach {
        sourceSets[it].java.srcDir("src/$it/kotlin")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        viewBinding = true
    }
    lint {
        disable.add("OldTargetApi")
    }
}

dependencies {
    implementation(projects.selektAndroid)
    implementation(androidX("appcompat", version = "1.0.2"))
    implementation(androidX("core", "ktx", version = "1.0.2"))
    implementation(androidX("paging", "runtime", "2.1.0"))
    implementation(androidX("constraintlayout", version = "1.1.3"))
}
