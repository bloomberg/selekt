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
    id("org.jetbrains.dokka")
    id("app.cash.licensee") version Versions.GRADLE_LICENSEE_PLUGIN.version
    kotlin("kapt")
    `maven-publish`
    signing
    id("org.jetbrains.kotlinx.kover")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
    google()
}

android {
    compileSdk = Versions.ANDROID_SDK.version.toInt()
    @Suppress("UnstableApiUsage")
    buildToolsVersion = Versions.ANDROID_BUILD_TOOLS.version
    namespace = "com.bloomberg.selekt.android"
    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            buildConfigField("String", "gitCommitSha1", "\"${gitCommit()}\"")
        }
    }
    sourceSets["test"].resources.srcDir(layout.buildDirectory.dir("intermediates/libs"))
    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    api(projects.selektApi)
    compileOnly(projects.selektAndroidSqlcipher)
    compileOnly(androidX("room", "runtime", Versions.ANDROIDX_ROOM.version))
    implementation(projects.selektJava)
    implementation(projects.selektSqlite3Classes)
    kaptTest(androidX("room", "compiler", Versions.ANDROIDX_ROOM.version))
    testImplementation(androidX("lifecycle", "livedata-ktx", Versions.ANDROIDX_LIVE_DATA.version))
    testImplementation(androidX("room", "runtime", Versions.ANDROIDX_ROOM.version))
    testImplementation(androidX("room", "ktx", Versions.ANDROIDX_ROOM.version))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.JUNIT5}")
    testRuntimeOnly(projects.selektAndroidSqlcipher)
    testRuntimeOnly("org.robolectric:android-all:${Versions.ROBOLECTRIC_ANDROID_ALL}")
}

koverReport {
    defaults {
        mergeWith("debug")
    }
    androidReports("debug") {
        filters {
            excludes {
                classes(
                    "*.BuildConfig"
                )
            }
        }
    }
}

tasks.register<Copy>("copyJniLibs") {
    from(
        fileTree(project(":SQLite3").layout.buildDirectory.dir("intermediates/libs")),
        fileTree(project(":Selektric").layout.buildDirectory.dir("intermediates/libs"))
    )
    into(layout.buildDirectory.dir("intermediates/libs/jni"))
}

tasks.register<Task>("buildNativeHost") {
    dependsOn(":SQLite3:buildHost", ":Selektric:buildHost")
    finalizedBy("copyJniLibs")
}

arrayOf("Debug", "Release").map { "pre${it}UnitTestBuild" }.forEach {
    tasks.whenTaskAdded {
        if (it == name) {
            dependsOn("buildNativeHost")
        }
    }
}

arrayOf("Debug", "Release").map { "process${it}UnitTestJavaRes" }.forEach {
    tasks.whenTaskAdded {
        if (it == name) {
            dependsOn("copyJniLibs")
        }
    }
}

licensee {
    allow("Apache-2.0")
}

components.matching { "release" == it.name }.configureEach {
    publishing {
        publications.register<MavenPublication>("main") {
            from(this@configureEach)
            pom {
                commonInitialisation(project)
                description.set("Selekt Android SQLite library.")
            }
        }
    }
}
