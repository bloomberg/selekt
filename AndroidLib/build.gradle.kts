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

import java.util.Locale
import kotlinx.kover.api.KoverTaskExtension
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.dokka")
    id("bb-jacoco-android")
    id("app.cash.licensee") version Versions.GRADLE_LICENSEE_PLUGIN.version
    kotlin("kapt")
    signing
    `maven-publish`
}

repositories {
    mavenCentral()
    google()
}

android {
    compileSdk = Versions.ANDROID_SDK.version.toInt()
    buildToolsVersion = Versions.ANDROID_BUILD_TOOLS.version

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "USE_EMBEDDED_LIBS", "true")
        }

        release {
            isMinifyEnabled = false
            buildConfigField("Boolean", "USE_EMBEDDED_LIBS", "false")
            buildConfigField("String", "gitCommitSha1", "\"${gitCommit()}\"")
        }
    }

    arrayOf("debug", "main", "release", "test").forEach {
        sourceSets[it].java.srcDir("src/$it/kotlin")
    }
    sourceSets["test"].resources.srcDir("$buildDir/intermediates/libs")

    testOptions {
        unitTests.all {
            if (!it.name.contains("debug", ignoreCase = true)) {
                it.extensions.configure<KoverTaskExtension> {
                    isDisabled = true
                }
            }
        }
    }
}

dependencies {
    api(selekt("api", selektVersionName))
    compileOnly(selekt("annotations", selektVersionName))
    compileOnly(androidX("room", "runtime", Versions.ANDROIDX_ROOM.version))
    implementation(selekt("android-sqlcipher", sqlcipherVersionName))
    implementation(selekt("java", selektVersionName))
    implementation(selekt("sqlite3", selektVersionName))
    testImplementation(androidX("lifecycle", "livedata-ktx", Versions.ANDROIDX_LIVE_DATA.version))
    testImplementation(androidX("room", "runtime", Versions.ANDROIDX_ROOM.version))
    testImplementation(androidX("room", "ktx", Versions.ANDROIDX_ROOM.version))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.JUNIT5}")
    testRuntimeOnly("org.robolectric:android-all:${Versions.ROBOLECTRIC_ANDROID_ALL}")
    kaptTest(androidX("room", "compiler", Versions.ANDROIDX_ROOM.version))
}

tasks.register<Copy>("copyJniLibs") {
    from(fileTree("${project(":SQLite3").buildDir.absolutePath}/intermediates/libs"))
    from(fileTree("${project(":Selektric").buildDir.absolutePath}/intermediates/libs"))
    into("${buildDir.path}/intermediates/libs/jni")
}

tasks.register<Task>("buildNativeHost") {
    dependsOn(":SQLite3:buildHost")
    dependsOn(":Selektric:buildHost")
    finalizedBy("copyJniLibs")
}

afterEvaluate {
    arrayOf("debug", "release").forEach {
        tasks.getByName("pre${it.capitalize(Locale.ROOT)}UnitTestBuild").dependsOn("buildNativeHost")
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        events("standardOut", "standardError", "started", "passed", "skipped", "failed")
    }
}

tasks.register("assembleSelekt") {
    dependsOn("assembleRelease")
    dependsOn("sourcesJar")
    dependsOn("dokkaHtmlJar")
}

tasks.register<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    setProperty("archiveBaseName", "selekt")
    setProperty("archiveClassifier", "sources")
}

tasks.withType<DokkaTaskPartial>().configureEach {
    moduleName.set("Selekt")
}

tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn("dokkaHtml")
    setProperty("archiveBaseName", "selekt")
    setProperty("archiveClassifier", "kdoc")
    from("$buildDir/dokka/html")
}

licensee {
    allow("Apache-2.0")
}

afterEvaluate {
    publishing {
        publications.create<MavenPublication>("main") {
            groupId = selektGroupId
            artifactId = "selekt-android"
            version = selektVersionName
            from(components["release"])
            pom {
                commonInitialisation(project)
                description.set("Selekt Android SQLite library.")
            }
            artifact("$buildDir/libs/selekt-sources.jar") { classifier = "sources" }
            artifact("$buildDir/libs/selekt-kdoc.jar") { classifier = "javadoc" }
        }.also {
            signing { sign(it) }
        }
    }
}
