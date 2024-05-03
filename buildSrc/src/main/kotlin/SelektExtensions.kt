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

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.publish.maven.MavenPom
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

val <T> NamedDomainObjectContainer<T>.debug: T get() = getByName("debug")

fun <T> NamedDomainObjectContainer<T>.debug(configure: T.() -> Unit) = getByName("debug", configure)

fun <T> NamedDomainObjectContainer<T>.release(configure: T.() -> Unit) = getByName("release", configure)

fun Project.gitCommit(): Provider<String> = providers.exec {
    commandLine("git", "rev-parse", "HEAD")
}.standardOutput.asText.map { it.trim() }

fun Project.gitCommitShort(): Provider<String> = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.map { it.trim() }

fun Project.isRelease() = hasProperty("release")

fun Project.resolvedOSSSonatypeURI() = URI(if (isRelease()) {
    "https://oss.sonatype.org/service/local/staging/deploy/maven2"
} else {
    "https://oss.sonatype.org/content/repositories/snapshots"
})

val Project.selektGroupId: String
    get() = "com.bloomberg"

val Project.selektVersionName: String
    get() = if (isRelease()) {
        checkNotNull(properties["selekt.versionName"]).toString()
    } else {
        "${checkNotNull(properties["selekt.nextVersionName"])}-SNAPSHOT"
    }

val Project.sqlcipherVersionName: String
    get() = "${checkNotNull(properties["sqlcipher.versionName"])}-$selektVersionName"

fun Project.disableKotlinCompilerAssertions() {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xno-call-assertions",
                "-Xno-receiver-assertions",
                "-Xno-param-assertions"
            )
        }
    }
}

fun MavenPom.commonInitialisation(project: Project) {
    val developerOrganization = "Bloomberg LP"
    ciManagement {
        name.set("Selekt")
        url.set("https://github.com/bloomberg/selekt/actions")
    }
    developers {
        developer {
            id.set("kennethshackleton")
            email.set("kshackleton1@bloomberg.net")
            name.set("Kenneth J. Shackleton")
            organization.set(developerOrganization)
            organizationUrl.set("https://github.com/bloomberg")
        }
        developer {
            id.set("xouabita")
            email.set("aabita@bloomberg.net")
            name.set("Alexandre Abita")
            organization.set(developerOrganization)
            organizationUrl.set("https://github.com/bloomberg")
        }
    }
    inceptionYear.set("2019")
    issueManagement {
        system.set("GitHub")
        url.set("https://github.com/bloomberg/selekt/issues")
    }
    licenses {
        license {
            distribution.set("repo")
            name.set("The Apache Software License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    organization {
        name.set(developerOrganization)
        url.set("https://www.bloomberg.com")
    }
    scm {
        connection.set("git@github.com:bloomberg/selekt.git")
        developerConnection.set("git@github.com:bloomberg/selekt.git")
        tag.set(project.gitCommit())
        url.set("https://github.com/bloomberg/selekt")
    }
    url.set("https://bloomberg.github.io/selekt/")
}
