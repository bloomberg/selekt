/*
 * Copyright 2025 Bloomberg Finance L.P.
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

description = "Selekt native library."

plugins {
    `java-library`
    `maven-publish`
    signing
}

sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("intermediates/libs"))
}

tasks.withType<ProcessResources>().configureEach {
    dependsOn("build${nativeHost()}", "copyJniLibs")
}

fun nativeHost() = osName().replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(Locale.US)
    } else {
        it.toString()
    }
} + System.getProperty("os.arch").replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(Locale.US)
    } else {
        it.toString()
    }
}

tasks.register<Task>("build${nativeHost()}") {
    dependsOn(":SQLite3:buildHost")
    finalizedBy("copyJniLibs")
}

tasks.register<Copy>("copyJniLibs") {
    from(fileTree(project(":SQLite3").layout.buildDirectory.dir("intermediates/libs")))
    into(layout.buildDirectory.dir("intermediates/libs/jni"))
    mustRunAfter("build${nativeHost()}")
}

fun osName() = System.getProperty("os.name").lowercase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

fun platformIdentifier() = "${osName()}-${System.getProperty("os.arch")}"

publishing {
    publications.register<MavenPublication>(
        nativeHost().replaceFirstChar(Char::lowercase)
    ) {
        from(components["java"])
        artifactId = "selekt-sqlite3-${platformIdentifier()}"
        pom {
            commonInitialisation(project)
            description.set("Selekt native library.")
        }
    }
}
