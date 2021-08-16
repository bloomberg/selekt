/*
 * Copyright 2021 Bloomberg Finance L.P.
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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// TODO Move me.
val kotlinVersion = "1.5.21"

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    google()
}

gradlePlugin {
    plugins {
        create("Selekt Plugin") {
            id = "selekt"
            implementationClass = "SelektPlugin"
        }
        create("Bloomberg JaCoCo Android Plugin") {
            id = "bb-jacoco-android"
            implementationClass = "JacocoAndroidPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    implementation(kotlin("stdlib-jdk8", version = kotlinVersion))
    implementation(kotlin("gradle-plugin", version = kotlinVersion))
    implementation("com.android.tools.build:gradle:7.0.0")
}
