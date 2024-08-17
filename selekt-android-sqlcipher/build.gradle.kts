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

import com.android.build.gradle.tasks.ExternalNativeBuildJsonTask

version = sqlcipherVersionName
logger.quiet("SQLCipher version: {}", sqlcipherVersionName)

plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    signing
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
    google()
}

val developmentABIs = listOf("arm64-v8a")
val allABIs = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

android {
    compileSdk = Versions.ANDROID_SDK.version.toInt()
    buildToolsVersion = "34.0.0"
    namespace = "com.bloomberg.selekt.android.sqlcipher"
    ndkVersion = "27.0.12077973"
    defaultConfig {
        minSdk = 21
    }
    buildTypes {
        debug {
            ndk {
                abiFilters.addAll(developmentABIs)
            }
        }
        release {
            isMinifyEnabled = false
            ndk {
                abiFilters.addAll(allABIs)
            }
        }
    }
    externalNativeBuild {
        cmake {
            path("$rootDir/SQLite3/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(projects.selektJava)
    implementation(projects.selektSqlite3Classes)
}

allABIs.forEach { abi ->
    tasks.matching {
        it is ExternalNativeBuildJsonTask && it.name.contains(abi)
    }.configureEach {
        dependsOn(":OpenSSL:assemble${abi.replaceFirstChar(Char::uppercaseChar)}", ":SQLite3:amalgamate")
    }
}

components.matching { "release" == it.name }.configureEach {
    publishing {
        publications.register<MavenPublication>("main") {
            from(this@configureEach)
            pom {
                commonInitialisation(project)
                description.set("SQLCipher for Selekt's Android Library.")
                licenses {
                    license {
                        name.set("Zetetic LLC")
                        url.set("https://www.zetetic.net/sqlcipher/license")
                    }
                }
            }
        }
    }
}
