/*
 * Copyright 2024 Bloomberg Finance L.P.
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
import org.gradle.api.component.AdhocComponentWithVariants
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "Selekt SQLite classes library."

plugins {
    kotlin("jvm")
    `maven-publish`
    signing
    alias(libs.plugins.jmh)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
}

disableKotlinCompilerAssertions()

java {
    withJavadocJar()
    withSourcesJar()
}

(components["java"] as AdhocComponentWithVariants).run {
    withVariantsFromConfiguration(configurations["apiElements"]) { skip() }
    withVariantsFromConfiguration(configurations["runtimeElements"]) { skip() }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

listOf(
    JvmTarget.JVM_17,
    JvmTarget.JVM_25
).forEach {
    val variantName = "java${it.target}"
    sourceSets {
        create(variantName) {
            kotlin {
                srcDir("src/$variantName/kotlin")
            }
        }
    }
    configurations["${variantName}CompileOnly"].extendsFrom(configurations.compileOnly.get())
    configurations["${variantName}Implementation"].extendsFrom(configurations.implementation.get())
    java {
        registerFeature(variantName) {
            usingSourceSet(sourceSets[variantName])
            capability(project.group.toString(), "${project.name}-$variantName", project.version.toString())
        }
    }
    tasks.named<JavaCompile>("compileJava${it.target}Java") {
        javaCompiler.set(javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(it.target))
        })
        options.release.set(it.target.toInt())
    }
    tasks.named<KotlinCompile>("compileJava${it.target}Kotlin").configure {
        compilerOptions {
            jvmTarget = it
        }
        kotlinJavaToolchain.toolchain.use(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(it.target))
        })
    }
}

sourceSets {
    named("jmh") {
        resources.srcDir(layout.buildDirectory.dir("intermediates/libs"))
    }
    named("test") {
        compileClasspath += sourceSets["java17"].output
        runtimeClasspath += sourceSets["java17"].output
    }
}

dependencies {
    compileOnly(libs.findbugs.jsr305)
    implementation(projects.selektCommons)
    implementation(projects.selektSqlite3Api)
    jmhImplementation(projects.selektCommons)
    jmhImplementation(projects.selektSqlite3Api)
    jmhImplementation(sourceSets["java17"].output)
    testImplementation(projects.selektCommons)
    testImplementation(projects.selektSqlite3Api)
    testImplementation(libs.kotlin.test)
}

listOf(
    JvmTarget.JVM_17,
    JvmTarget.JVM_25
).forEach {
    val variantName = "java${it.target}"
    sourceSets {
        create("${variantName}Test") {
            kotlin {
                srcDir(sourceSets["test"].kotlin.srcDirs)
            }
            compileClasspath += sourceSets[variantName].output + sourceSets["test"].output
            runtimeClasspath += output + compileClasspath
            resources.srcDir(layout.buildDirectory.dir("intermediates/libs"))
        }
    }

    configurations["${variantName}TestImplementation"].run {
        extendsFrom(configurations.getByName("${variantName}Implementation"))
        extendsFrom(configurations.getByName("testImplementation"))
    }

    configurations["${variantName}TestRuntimeOnly"].extendsFrom(configurations.getByName("testRuntimeOnly"))

    tasks.named<KotlinCompile>("compile${variantName.replaceFirstChar(Char::uppercase)}TestKotlin").configure {
        compilerOptions {
            jvmTarget = it
        }
        kotlinJavaToolchain.toolchain.use(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(it.target))
        })
    }

    tasks.named<JavaCompile>("compileJava${it.target}TestJava") {
        javaCompiler.set(javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(it.target))
        })
        options.release.set(it.target.toInt())
    }

    tasks.register<Test>("test${variantName.replaceFirstChar(Char::uppercase)}") {
        description = "Runs tests for the Java ${it.target} variant"
        group = "verification"
        testClassesDirs = sourceSets["${variantName}Test"].output.classesDirs
        classpath = sourceSets["${variantName}Test"].runtimeClasspath
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(it.target))
        })
        useJUnitPlatform()
        dependsOn("copyJniLibs")
        systemProperty(
            "com.bloomberg.selekt.library_path",
            layout.buildDirectory.dir("intermediates/libs").get().asFile.toString()
        )
        if (it.target.toInt() >= 25) {
            jvmArgs("--enable-native-access=ALL-UNNAMED")
        }
    }
}

tasks.named<Test>("test") {
    enabled = false
}

val java17CrossRuntimeDatabase = layout.buildDirectory.file("cross-runtime/java17.db")
val java25CrossRuntimeDatabase = layout.buildDirectory.file("cross-runtime/java25.db")

val writeCrossRuntimeDatabaseWithJava17 = tasks.register<JavaExec>("writeCrossRuntimeDatabaseWithJava17") {
    group = "verification"
    description = "Writes a raw-key database using the Java 17 JNI backend."
    classpath = sourceSets["java17Test"].runtimeClasspath
    mainClass.set("com.bloomberg.selekt.CrossRuntimeRawKeyMainKt")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
    systemProperty(
        "com.bloomberg.selekt.library_path",
        layout.buildDirectory.dir("intermediates/libs").get().asFile.toString()
    )
    args("write", java17CrossRuntimeDatabase.get().asFile.absolutePath)
    dependsOn("compileJava17TestKotlin", "copyJniLibs")
}

val readCrossRuntimeDatabaseWithJava25 = tasks.register<JavaExec>("readCrossRuntimeDatabaseWithJava25") {
    group = "verification"
    description = "Reads the Java 17 raw-key database using the Java 25 FFM backend."
    classpath = sourceSets["java25Test"].runtimeClasspath
    mainClass.set("com.bloomberg.selekt.CrossRuntimeRawKeyMainKt")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    systemProperty(
        "com.bloomberg.selekt.library_path",
        layout.buildDirectory.dir("intermediates/libs").get().asFile.toString()
    )
    args("read", java17CrossRuntimeDatabase.get().asFile.absolutePath)
    dependsOn(writeCrossRuntimeDatabaseWithJava17, "compileJava25TestKotlin", "copyJniLibs")
}

val writeCrossRuntimeDatabaseWithJava25 = tasks.register<JavaExec>("writeCrossRuntimeDatabaseWithJava25") {
    group = "verification"
    description = "Writes a raw-key database using the Java 25 FFM backend."
    classpath = sourceSets["java25Test"].runtimeClasspath
    mainClass.set("com.bloomberg.selekt.CrossRuntimeRawKeyMainKt")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    systemProperty(
        "com.bloomberg.selekt.library_path",
        layout.buildDirectory.dir("intermediates/libs").get().asFile.toString()
    )
    args("write", java25CrossRuntimeDatabase.get().asFile.absolutePath)
    dependsOn("compileJava25TestKotlin", "copyJniLibs")
}

val readCrossRuntimeDatabaseWithJava17 = tasks.register<JavaExec>("readCrossRuntimeDatabaseWithJava17") {
    group = "verification"
    description = "Reads the Java 25 raw-key database using the Java 17 JNI backend."
    classpath = sourceSets["java17Test"].runtimeClasspath
    mainClass.set("com.bloomberg.selekt.CrossRuntimeRawKeyMainKt")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
    systemProperty(
        "com.bloomberg.selekt.library_path",
        layout.buildDirectory.dir("intermediates/libs").get().asFile.toString()
    )
    args("read", java25CrossRuntimeDatabase.get().asFile.absolutePath)
    dependsOn(writeCrossRuntimeDatabaseWithJava25, "compileJava17TestKotlin", "copyJniLibs")
}

tasks.register("testCrossRuntimeKeyCompatibility") {
    group = "verification"
    description = "Verifies raw-key database compatibility between the Java 17 JNI and Java 25 FFM backends."
    dependsOn(readCrossRuntimeDatabaseWithJava25, readCrossRuntimeDatabaseWithJava17)
}

tasks.named("check") {
    dependsOn("testCrossRuntimeKeyCompatibility")
}

tasks.register<Copy>("copyJniLibs") {
    from(fileTree(project(":SQLite3").layout.buildDirectory.dir("intermediates/libs")))
    into(layout.buildDirectory.dir("intermediates/libs/jni"))
    dependsOn(":selekt-sqlite3-sqlcipher:buildNativeHost")
}

listOf(
    "processJmhResources",
    "processJava17TestResources",
    "processJava25TestResources",
).forEach { taskName ->
    tasks.named<ProcessResources>(taskName) {
        dependsOn("copyJniLibs")
    }
}

tasks.named<JMHTask>("jmh") {
    dependsOn("copyJniLibs", ":selekt-sqlite3-sqlcipher:buildNativeHost")
}

listOf(
    JvmTarget.JVM_17,
    JvmTarget.JVM_25
).forEach {
    val variantName = "java${it.target}"
    tasks.register<JavaExec>("jmh${variantName.replaceFirstChar(Char::uppercase)}") {
        description = "Runs JMH benchmarks using Java ${it.target}"
        group = "benchmark"
        val jmhJar = tasks.named("jmhJar").get().outputs.files.singleFile
        classpath = sourceSets[variantName].output + files(jmhJar)
        mainClass.set("org.openjdk.jmh.Main")
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(it.target))
        })
        dependsOn("jmhJar", "copyJniLibs")
        outputs.upToDateWhen { false }
        outputs.cacheIf { false }
        systemProperty(
            "com.bloomberg.selekt.library_path",
            layout.buildDirectory.dir("intermediates/libs").get().asFile.toString()
        )
        if (it.target.toInt() >= 25) {
            jvmArgs("--enable-native-access=ALL-UNNAMED")
        }
    }
}

detekt {
    listOf(
        "java17",
        "java25",
        "jmh",
        "test"
    ).forEach {
        source.setFrom("src/$it/kotlin")
    }
    buildUponDefaultConfig = true
}

kover {
    reports {
        filters {
            excludes {
                classes("*Test*")
            }
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("main") {
            from(components.getByName("java"))
            pom {
                commonInitialisation(project)
            }
        }
    }
}
