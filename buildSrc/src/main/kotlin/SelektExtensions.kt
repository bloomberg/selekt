/*
 * Copyright 2021 Bloomberg Finance L.P.
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
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.charset.StandardCharsets

fun DependencyHandler.androidX(module: String, suffix: String? = null, version: String? = null): Any =
    "androidx.$module:$module${suffix?.let { "-$it" } ?: ""}${version?.let { ":$it" } ?: ""}"

fun DependencyHandler.kotlinX(module: String, version: String? = null): Any =
    "org.jetbrains.kotlinx:kotlinx-$module${version?.let { ":$version" } ?: ""}"

fun DependencyHandler.selekt(module: String, version: String? = null): Any =
    "com.bloomberg:selekt-$module${version?.let { ":$version" } ?: ""}"

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

fun Project.resolvedOSSSonatypeURI() = URI(if (isRelease()) {
    "https://oss.sonatype.org/service/local/staging/deploy/maven2"
} else {
    "https://oss.sonatype.org/content/repositories/snapshots"
})

val Project.selektGroupId: String
    get() = "com.bloomberg"

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
    ciManagement {
        name.set("GitHub Actions")
        url.set("https://github.com/bloomberg/selekt/actions")
    }
    developers {
        developer {
            id.set("kennethshackleton")
            email.set("kshackleton1@bloomberg.net")
            name.set("Kenneth J. Shackleton")
            organization.set("Bloomberg LP")
            organizationUrl.set("https://github.com/bloomberg")
        }
        developer {
            id.set("xouabita")
            email.set("aabita@bloomberg.net")
            name.set("Alexandre Abita")
            organization.set("Bloomberg LP")
            organizationUrl.set("https://github.com/bloomberg")
        }
    }
    inceptionYear.set("2019")
    issueManagement {
        system.set("GitHub")
        url.set("https://github.com/bloomberg/Selekt/issues")
    }
    licenses {
        license {
            distribution.set("repo")
            name.set("The Apache Software License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    organization {
        name.set("Bloomberg LP")
        url.set("https://www.bloomberg.com")
    }
    scm {
        connection.set("git@github.com:bloomberg/selekt.git")
        developerConnection.set("git@github.com:bloomberg/selekt.git")
        tag.set(project.gitCommit())
        url.set("https://github.com/bloomberg/Selekt")
    }
    url.set("https://bloomberg.github.io/selekt/")
}
