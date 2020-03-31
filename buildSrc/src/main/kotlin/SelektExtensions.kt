/*
 * Copyright 2020 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.KotlinClosure1
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

fun com.android.build.gradle.internal.dsl.TestOptions.UnitTestOptions.all(block: Test.() -> Unit) =
    all(KotlinClosure1<Any, Test>({ (this as Test).apply(block) }, owner = this))

fun DependencyHandler.androidX(module: String, suffix: String? = null, version: String? = null): Any =
    "androidx.$module:$module${suffix?.let { "-$it" } ?: ""}${version?.let { ":$it" } ?: ""}"

fun DependencyHandler.kotlinX(module: String, version: String? = null): Any =
    "org.jetbrains.kotlinx:kotlinx-$module${version?.let { ":$version" } ?: ""}"

fun DependencyHandler.selekt(module: String, version: String? = null): Any =
    "com.bloomberg.selekt:selekt-$module${version?.let { ":$version" } ?: ""}"

val <T> NamedDomainObjectContainer<T>.debug: T get() = requireNotNull(getByName("debug"))

fun <T> NamedDomainObjectContainer<T>.debug(configure: T.() -> Unit) = requireNotNull(getByName("debug", configure))

fun <T> NamedDomainObjectContainer<T>.release(configure: T.() -> Unit) = requireNotNull(getByName("release", configure))

fun Project.gitCommit() = ByteArrayOutputStream().apply {
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = this@apply
    }
}.run {
    toString(StandardCharsets.UTF_8.name()).trim()
}

fun Project.gitCommitShort() = ByteArrayOutputStream().apply {
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = this@apply
    }
}.run {
    toString(StandardCharsets.UTF_8.name()).trim()
}

fun Project.isRelease() = "true" == properties["release"]

val Project.selektVersionName: String
    get() = "${properties["selekt.versionName"]}${if (isRelease()) "" else "-SNAPSHOT"}"

fun Project.disableKotlinCompilerAssertions() {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        tasks.withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf(
                    "-Xno-call-assertions",
                    "-Xno-receiver-assertions",
                    "-Xno-param-assertions"
                )
            }
        }
    }
}

fun MavenPom.commonInitialisation(project: Project) {
    @Suppress("UnstableApiUsage")
    scm {
        connection.set("https://github.com/bloomberg/Selekt.git")
        developerConnection.set("https://github.com/bloomberg/Selekt.git")
        tag.set(project.gitCommit())
        url.set("https://github.com/bloomberg/Selekt.git")
    }
}
