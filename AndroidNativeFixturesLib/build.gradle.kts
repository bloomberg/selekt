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

plugins {
    id("cpp-library")
}

repositories {
    mavenCentral()
}

library {
    targetMachines.addAll(
        machines.linux.x86_64,
        machines.macOS.x86_64 // FIXME M1.
    )
}

fun osName() = System.getProperty("os.name").toLowerCase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

tasks.withType<CppCompile>().configureEach {
    val javaInclude = "${System.getProperty("java.home")}${File.separatorChar}include"
    includes(file(javaInclude))
    includes(file("$javaInclude${File.separatorChar}${osName()}"))
}
