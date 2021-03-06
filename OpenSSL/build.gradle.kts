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

import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import java.util.Locale

repositories {
    mavenCentral()
    jcenter()
}

fun Project.openSslVersion() = arrayOf(
    "${property("openssl.version")}",
    "${property("openssl.version.suffix")}"
).joinToString("")

fun Project.openSslBaseUrl() = "https://www.openssl.org/source/old/${project.property("openssl.version")}"
fun Project.openSslUrl() = "${openSslBaseUrl()}/openssl-${openSslVersion()}.tar.gz"
fun Project.openSslPgpUrl() = "${openSslBaseUrl()}/openssl-${openSslVersion()}.tar.gz.asc"

val archive = File("$buildDir/tmp/openssl-${openSslVersion()}.tar.gz")
val archivePgp = File("${archive.path}.asc")

tasks.register<Download>("downloadOpenSslPgp") {
    val url = openSslPgpUrl()
    src(url)
    dest(archivePgp)
    overwrite(false)
    inputs.property("url", url)
    outputs.files(archivePgp)
    finalizedBy("verifyOpenSslPgpChecksum")
}

tasks.register<Download>("downloadOpenSsl") {
    val url = openSslUrl()
    src(url)
    dest(archive)
    overwrite(false)
    inputs.property("url", url)
    outputs.files(archive)
    dependsOn("downloadOpenSslPgp")
    finalizedBy("verifyOpenSslChecksum", "verifyOpenSslSignature")
}

tasks.register<Verify>("verifyOpenSslPgpChecksum") {
    src(archivePgp)
    algorithm("SHA-256")
    checksum("${project.property("openssl.pgp.sha256")}")
}

tasks.register<Verify>("verifyOpenSslChecksum") {
    src(archive)
    algorithm("SHA-256")
    checksum("${project.property("openssl.sha256")}")
    mustRunAfter("downloadOpenSsl")
}

tasks.register<Exec>("verifyOpenSslSignature") {
    inputs.files(archivePgp, archive)
    commandLine(
        "gpg", "--no-default-keyring", "--keyring", "$projectDir/openssl.gpg", "--verify",
        archivePgp, archive
    )
}

tasks.register<Copy>("unpackOpenSsl") {
    from(tarTree(archive))
    into("$buildDir/generated/")
    dependsOn("downloadOpenSsl")
}

arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64").forEach {
    tasks.register<Exec>("assemble${it.capitalize(Locale.ROOT)}") {
        dependsOn("unpackOpenSsl")
        workingDir(projectDir)
        commandLine("./build_libraries.sh")
        args(
            archive.run { File("$buildDir/generated/${name.substringBefore(".tar.gz")}") }.path,
            it,
            21
        )
    }
}

fun osName() = System.getProperty("os.name").toLowerCase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

fun platformIdentifier() = "${osName()}-${System.getProperty("os.arch")}"

val openSslWorkingDir: String = archive.run {
    File("$buildDir/generated/${name.substringBefore(".tar.gz")}")
}.path

tasks.register<Exec>("configureHost") {
    dependsOn("unpackOpenSsl")
    workingDir(openSslWorkingDir)
    commandLine("./config")
}

tasks.register<Exec>("makeHost") {
    dependsOn("configureHost")
    workingDir(openSslWorkingDir)
    commandLine("make")
    args("build_libs")
}

tasks.register<Task>("assembleHost") {
    dependsOn("makeHost")
    doLast {
        "${buildDir.path}/libs/${platformIdentifier()}".let {
            mkdir(it)
            copy {
                logger.quiet("Copying to: $it")
                from(fileTree(openSslWorkingDir) {
                    arrayOf(".a").forEach { e ->
                        include("**/libcrypto$e")
                    }
                }.files)
                into(it)
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(buildDir)
}
