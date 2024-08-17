import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask

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
    alias(libs.plugins.dokka)
    alias(libs.plugins.cash.licensee)
    alias(libs.plugins.ksp)
    `maven-publish`
    signing
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
    google()
}

android {
    compileSdk = Versions.ANDROID_SDK.version.toInt()
    buildToolsVersion = "34.0.0"
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
    sourceSets["test"].assets.srcDir(layout.buildDirectory.dir("intermediates/libs"))
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
    compileOnly(libs.androidx.room.runtime)
    implementation(projects.selektJava)
    implementation(projects.selektSqlite3Classes)
    kspTest(libs.androidx.room.compiler)
    testImplementation(libs.androidx.lifecycle.livedata.ktx)
    testImplementation(libs.androidx.room.runtime)
    testImplementation(libs.androidx.room.ktx)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(projects.selektAndroidSqlcipher)
    testRuntimeOnly(libs.robolectric.android.all)
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
    mustRunAfter("buildNativeHost")
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

tasks.withType<AndroidLintAnalysisTask>().configureEach {
    dependsOn("copyJniLibs")
}

tasks.withType<LintModelWriterTask>().configureEach {
    dependsOn("copyJniLibs")
}

arrayOf("Debug", "Release").map { "merge${it}UnitTestAssets" }.forEach {
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
