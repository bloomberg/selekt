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

import com.android.build.gradle.tasks.ExternalNativeBuildJsonTask

version = sqlcipherVersionName
logger.quiet("SQLCipher version: {}", sqlcipherVersionName)

plugins {
    id("com.android.library")
    `maven-publish`
    signing
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
    google()
}

android {
    compileSdk = Versions.ANDROID_SDK.version.toInt()
    buildToolsVersion = Versions.ANDROID_BUILD_TOOLS.version
    ndkVersion = Versions.ANDROID_NDK.version
    defaultConfig {
        minSdk = 21
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }
    externalNativeBuild {
        cmake {
            path("$rootDir/selekt-sqlite3/CMakeLists.txt")
            version = Versions.CMAKE.version
        }
    }
    publishing {
        singleVariant("release")
    }
}

tasks.withType<ExternalNativeBuildJsonTask>().configureEach {
    dependsOn(":OpenSSL:assembleAndroid")
    dependsOn(":selekt-sqlite3:amalgamate")
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
                        name.set("Dual OpenSSL and SSLeay License")
                        url.set("https://www.openssl.org/source/license-openssl-ssleay.txt")
                    }
                    license {
                        name.set("Zetetic LLC")
                        url.set("https://www.zetetic.net/sqlcipher/license")
                    }
                }
            }
        }
    }
}
