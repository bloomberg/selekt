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

description = "Selekt SQLCipher library."

plugins {
    `java-library`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("intermediates/libs"))
}

fun osName() = System.getProperty("os.name").lowercase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

fun platformIdentifier(): String = "${osName()}-${System.getProperty("os.arch")}".let {
    if (System.getenv("SELEKT_LIBC") == "musl") { "$it-musl" } else { it }
}

tasks.named<Jar>("jar") {
    archiveClassifier.set(platformIdentifier())
    metaInf {
        from("$rootDir/SQLCIPHER_LICENSE")
    }
}

tasks.withType<ProcessResources>().configureEach {
    dependsOn("buildNativeHost", "copyJniLibs")
}

tasks.register<Task>("buildNativeHost") {
    dependsOn(":SQLite3:buildHost")
    finalizedBy("copyJniLibs")
}

tasks.register<Copy>("copyJniLibs") {
    from(fileTree(project(":SQLite3").layout.buildDirectory.dir("intermediates/libs")))
    into(layout.buildDirectory.dir("intermediates/libs/jni"))
    mustRunAfter("buildNativeHost")
}

val platforms = listOf(
    "darwin-aarch64",
    "linux-aarch64",
    "linux-aarch64-musl",
    "linux-amd64",
    "linux-amd64-musl",
    "windows-aarch64",
    "windows-amd64"
)

val uberJar by tasks.registering(Jar::class) {
    description = "Assembles a JAR bundling native libraries for all supported platforms."
    archiveClassifier.set("")
    platforms.forEach {
        from(zipTree(file("build/libs/${project.name}-$version-$it.jar"))) {
            include("jni/**")
        }
    }
    metaInf {
        from("$rootDir/SQLCIPHER_LICENSE")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications.register<MavenPublication>("native") {
        artifact(uberJar)
        pom {
            commonInitialisation(project)
            licenses {
                license {
                    name.set("Zetetic LLC")
                    url.set("https://www.zetetic.net/sqlcipher/license")
                }
            }
        }
    }
}
