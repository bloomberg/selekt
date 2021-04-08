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
    signing
    `maven-publish`
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

        ndk {
            abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "USE_EMBEDDED_LIBS", "true")
        }

        release {
            buildConfigField("Boolean", "USE_EMBEDDED_LIBS", "false")
        }
    }

    defaultPublishConfig = "release"

    sourceSets["test"].resources.srcDir("$buildDir/intermediates/libs")

    buildTypes {
        release {
            isMinifyEnabled = false
            buildConfigField("String", "gitCommitSha1", "\"${gitCommit()}\"")
        }
    }

    externalNativeBuild {
        cmake {
            path("$rootDir/SQLite3/CMakeLists.txt")
        }
    }
}

dependencies {
    api(selekt("api", selektVersionName))
    compileOnly(selekt("annotations", selektVersionName))
    compileOnly(androidX("room", "runtime", Versions.ANDROIDX_ROOM.version))
    implementation(selekt("java", selektVersionName))
    implementation(selekt("sqlite3", selektVersionName))
    testImplementation("org.robolectric:robolectric:${Versions.ROBOLECTRIC}")
    testImplementation(androidX("lifecycle", "livedata-ktx", Versions.ANDROIDX_LIVE_DATA.version))
    testImplementation(androidX("room", "runtime", Versions.ANDROIDX_ROOM.version))
    kaptTest(androidX("room", "compiler", Versions.ANDROIDX_ROOM.version))
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

tasks.register("assembleSelekt") {
    dependsOn("assembleRelease")
    dependsOn("sourcesJar")
}

tasks.register<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    setProperty("archiveBaseName", "selekt")
    setProperty("archiveClassifier", "sources")
}

afterEvaluate {
    publishing {
        publications.create<MavenPublication>("main") {
            groupId = selektGroupId
            artifactId = "selekt-android"
            version = android.defaultConfig.versionName
            from(components["release"])
            pom {
                commonInitialisation(project)
                description.set("Selekt Android SQLite library.")
            }
            artifact("$buildDir/libs/selekt-sources.jar") { classifier = "sources" }
        }.also {
            signing { sign(it) }
        }
    }
}
