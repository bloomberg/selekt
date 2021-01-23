/*
 * Copyright 2021 Bloomberg Finance L.P.
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

import java.util.Locale

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.dokka")
    id("bb-jacoco-android")
    kotlin("kapt")
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

android {
    compileSdkVersion(Versions.ANDROID_SDK.version.toInt())
    buildToolsVersion(Versions.ANDROID_BUILD_TOOLS.version)

    defaultConfig {
        minSdkVersion(21)
        versionCode = 1
        versionName = selektVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    defaultPublishConfig = "release"

    arrayOf("main", "test").forEach {
        sourceSets[it].java.srcDir("src/$it/kotlin")
    }

    sourceSets["test"].resources.srcDir("$buildDir/intermediates/libs")

    buildTypes {
        release {
            isMinifyEnabled = false
            consumerProguardFile("proguard-consumer-rules.pro")
        }
    }

    testOptions {
        unitTests.apply {
            if (project.hasProperty("robolectricDependencyRepoUrl")) {
                all {
                    it.systemProperty("robolectric.dependency.repo.url",
                        requireNotNull(project.properties["robolectricDependencyRepoUrl"]))
                }
            }
            isIncludeAndroidResources = true
        }
    }
}

description = "Selekt Android Support library."

dependencies {
    implementation(selekt("android", selektVersionName))
    api(selekt("api", selektVersionName))
    implementation(androidX("sqlite", "ktx", Versions.ANDROIDX_SQLITE.version))
    implementation(androidX("room", "runtime", Versions.ANDROIDX_ROOM.version))
    testImplementation("org.robolectric:robolectric:${Versions.ROBOLECTRIC}")
    testImplementation(kotlinX("coroutines-core", Versions.KOTLIN_COROUTINES.version))
    testImplementation(kotlinX("coroutines-android", Versions.KOTLIN_COROUTINES.version))
    testImplementation(androidX("lifecycle", "livedata-ktx", Versions.ANDROIDX_LIVE_DATA.version))
    kaptTest(androidX("room", "compiler", Versions.ANDROIDX_ROOM.version))
}

tasks.register("assembleSelekt") {
    dependsOn("assembleRelease")
}

tasks.register<Copy>("copyJniLibs") {
    from(fileTree("${project(":SQLite3").buildDir.absolutePath}/intermediates/libs"))
    into("${buildDir.path}/intermediates/libs/jni")
}

tasks.register<Task>("buildHostSQLite") {
    dependsOn(":SQLite3:buildHost")
    finalizedBy("copyJniLibs")
}

afterEvaluate {
    arrayOf("debug", "release").forEach {
        @UseExperimental(ExperimentalStdlibApi::class)
        tasks.getByName("pre${it.capitalize(Locale.US)}UnitTestBuild").dependsOn("buildHostSQLite")
    }
}
