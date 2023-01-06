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

import java.util.Locale

repositories {
    mavenCentral()
}

plugins {
    base
}

tasks.register<Exec>("cmakeSelektric") {
    doFirst {
        mkdir(".cxx-host")
    }
    workingDir(".cxx-host")
    commandLine("cmake")
    args(
        "-DCMAKE_BUILD_TYPE=Release",
        "-DUSE_CCACHE=1",
        "-DSLKT_TARGET_ABI=${platformIdentifier()}",
        projectDir
    )
}

tasks.register<Exec>("makeSelektric") {
    dependsOn("cmakeSelektric")
    workingDir(".cxx-host")
    commandLine("make", "selektric")
}

fun osName() = System.getProperty("os.name").toLowerCase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

fun platformIdentifier() = "${osName()}-${System.getProperty("os.arch")}"

tasks.register<Task>("buildHost") {
    dependsOn("makeSelektric")
    doLast {
        "${buildDir.path}/intermediates/libs/${platformIdentifier()}".let {
            mkdir(it)
            copy {
                logger.quiet("Copying to: $it")
                from(fileTree(".cxx-host") {
                    include("**/*.dll", "**/*.dylib", "**/*.so")
                }.files)
                into(it)
            }
        }
    }
}

tasks.register<Delete>("deleteCxxHost") {
    delete(".cxx-host")
}

tasks.getByName("clean") {
    dependsOn("deleteCxxHost")
}
