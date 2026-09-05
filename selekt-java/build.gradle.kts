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

@file:Suppress("UnstableApiUsage")

import me.champeau.jmh.JMHTask
import me.champeau.jmh.JmhBytecodeGeneratorTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "Selekt core library."

plugins {
    kotlin("jvm")
    id("com.android.lint")
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
    alias(libs.plugins.jmh)
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
    google()
}

disableKotlinCompilerAssertions()

java {
    withJavadocJar()
    withSourcesJar()
}

sourceSets {
    val integrationTest by creating {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        resources.srcDir(layout.buildDirectory.dir("intermediates/libs"))
    }
    named("jmh") {
        resources.srcDir(layout.buildDirectory.dir("intermediates/libs"))
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    implementation(projects.selektApi)
    implementation(projects.selektCommons)
    implementation(projects.selektSqlite3Api)
    integrationTestImplementation(projects.selektSqlite3Classes) {
        capabilities {
            requireCapability("com.bloomberg.selekt:selekt-sqlite3-classes-java17")
        }
    }
    jmhImplementation(projects.selektSqlite3Classes) {
        capabilities {
            requireCapability("com.bloomberg.selekt:selekt-sqlite3-classes-java17")
        }
    }
    jmhImplementation(projects.selektSqlite3Sqlcipher)
    jmhImplementation(libs.kotlinx.coroutines.core)
}

val jmhSqlite3ClassesJava25: Configuration by configurations.creating {
    isCanBeConsumed = false
}

dependencies {
    jmhSqlite3ClassesJava25(projects.selektSqlite3Classes) {
        capabilities {
            requireCapability("com.bloomberg.selekt:selekt-sqlite3-classes-java25")
        }
    }
}

jmh {
    resultFormat.set("JSON")
    if (hasProperty("jmh.includes")) {
        includes.add(property("jmh.includes").toString())
    }
    if (hasProperty("jmh.profilers")) {
        val jmhReportsDir = layout.buildDirectory.dir("reports/jmh").get().asFile.absolutePath
        property("jmh.profilers").toString().split(',').forEach {
            val profiler = it.trim()
            val resolved = when {
                profiler == "jfr" -> "jfr:dir=$jmhReportsDir"
                profiler.startsWith("jfr:") && !profiler.contains("dir=") -> "$profiler,dir=$jmhReportsDir"
                else -> profiler
            }
            profilers.add(resolved)
        }
    }
    if (hasProperty("jmh.params")) {
        property("jmh.params").toString().split(";").forEach { entry ->
            val (key, value) = entry.split("=", limit = 2)
            benchmarkParameters.put(key, objects.listProperty(String::class.java).also { it.set(value.split(",")) })
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                classes("com.bloomberg.selekt.jvm.*")
            }
        }
    }
}

publishing {
    publications.register<MavenPublication>("main") {
        from(components.getByName("java"))
        pom {
            commonInitialisation(project)
        }
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    outputs.upToDateWhen { false }
    outputs.cacheIf { false }
    dependsOn("buildHostSQLite")
    shouldRunAfter("test")
}

tasks.named<JMHTask>("jmh") {
    dependsOn("buildHostSQLite")
    shouldRunAfter("integrationTest")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

tasks.register("jmhJava17") {
    description = "Runs the jmh benchmarks using the Java 17 (JNI) SQLite variant"
    group = "benchmark"
    dependsOn("jmh")
}

tasks.register<JavaExec>("jmhJava25") {
    description = "Runs the jmh benchmarks using the Java 25 (FFM) SQLite variant"
    group = "benchmark"
    val jmhJar = tasks.named("jmhJar").get().outputs.files.singleFile
    classpath = jmhSqlite3ClassesJava25 + files(jmhJar)
    mainClass.set("org.openjdk.jmh.Main")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    dependsOn("jmhJar", "buildHostSQLite")
    outputs.upToDateWhen { false }
    outputs.cacheIf { false }
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if (project.hasProperty("jmh.includes")) {
        args(project.property("jmh.includes").toString())
    }
    if (project.hasProperty("jmh.params")) {
        project.property("jmh.params").toString().split(";").forEach { entry ->
            val (key, value) = entry.split("=", limit = 2)
            args("-p", "$key=$value")
        }
    }
    if (project.hasProperty("jmh.profilers")) {
        val jmhReportsDir = layout.buildDirectory.dir("reports/jmh").get().asFile.absolutePath
        project.property("jmh.profilers").toString().split(',').forEach {
            val profiler = it.trim()
            val resolved = when {
                profiler == "jfr" -> "jfr:dir=$jmhReportsDir"
                profiler.startsWith("jfr:") && !profiler.contains("dir=") -> "$profiler,dir=$jmhReportsDir"
                else -> profiler
            }
            args("-prof", resolved)
        }
    }
    args("-jvmArgsAppend", "--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JmhBytecodeGeneratorTask>().configureEach {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

tasks.named<JavaCompile>("compileJmhJava") {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
    options.release.set(17)
}
tasks.named<JavaCompile>("jmhCompileGeneratedClasses") {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
    options.release.set(17)
}
tasks.named<KotlinCompile>("compileJmhKotlin").configure {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    kotlinJavaToolchain.toolchain.use(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}
listOf("jmhCompileClasspath", "jmhRuntimeClasspath").forEach { name ->
    configurations.named(name) {
        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
        }
    }
}

tasks.register<Task>("buildHostSQLite") {
    dependsOn(":SQLite3:buildHost", "copyJniLibs")
}

tasks.register<Copy>("copyJniLibs") {
    from(fileTree(project(":SQLite3").layout.buildDirectory.dir("intermediates/libs")))
    into(layout.buildDirectory.dir("intermediates/libs/jni"))
    mustRunAfter(":SQLite3:buildHost")
}

tasks.withType<ProcessResources>().matching { it.name != "processResources" }.configureEach {
    dependsOn("buildHostSQLite")
}

val generateVersionProperties by tasks.registering {
    val version = project.version
    val outputDir = layout.buildDirectory.dir("generated/resources/version")
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        outputDir.get().asFile.resolve("com/bloomberg/selekt").also {
            it.mkdirs()
        }.resolve("selekt-version.properties").writeText("version=$version\n")
    }
}

sourceSets.main {
    resources.srcDir(generateVersionProperties)
}
