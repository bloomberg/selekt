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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get

class JmhPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = target.run {
        sourceSets.create("jmh") {
            java.srcDirs("src/jmh/kotlin")
        }
        dependencies.apply {
            configurations.getByName("jmhImplementation") {
                add(name, project)
                add(name, "org.openjdk.jmh:jmh-core:${Versions.JMH}")
            }
            configurations.getByName("kaptJmh") {
                add(name, "org.openjdk.jmh:jmh-generator-annprocess:${Versions.JMH}")
            }
        }
        tasks.register("jmh", JavaExec::class.java) {
            val reportDir = "$buildDir/reports/jmh"
            val reportFile = "$reportDir/jmh.json"
            group = "benchmark"
            dependsOn("jmhClasses")
            mainClass.set("org.openjdk.jmh.Main")
            args(
                "-rf", "json",
                "-rff", reportFile
            )
            classpath(sourceSets.getByName("jmh").runtimeClasspath)
            doFirst { mkdir(reportDir) }
            outputs.apply {
                file(reportFile)
                upToDateWhen { false }
            }
        }
    }
}

private val Project.sourceSets: SourceSetContainer get() = extensions["sourceSets"] as SourceSetContainer
