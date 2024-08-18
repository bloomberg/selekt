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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

class JmhPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = target.run {
        sourceSets.create("jmh") {
            java.srcDirs("src/jmh/kotlin")
        }
        dependencies.apply {
            configurations.getByName("jmhImplementation") {
                extendsFrom(configurations.getByName("implementation"))
                add(name, project)
                add(name, "org.openjdk.jmh:jmh-core:${Versions.JMH}")
            }
            configurations.getByName("kspJmh") {
                add(name, "org.openjdk.jmh:jmh-generator-annprocess:${Versions.JMH}")
            }
        }
        tasks.register<JavaExec>("jmh") {
            val reportDir = layout.buildDirectory.dir("reports/jmh")
            val reportFile = layout.buildDirectory.file("reports/jmh/jmh.json")
            group = "benchmark"
            dependsOn("jmhClasses")
            mainClass.set("org.openjdk.jmh.Main")
            args(
                listOfNotNull(
                    properties["jmh.include"]?.toString(),
                    "-rf", "json",
                    "-rff", reportFile.get().asFile.absolutePath
                )
            )
            classpath(sourceSets.getByName("jmh").runtimeClasspath)
            doFirst { reportDir.get().asFile.mkdir() }
            outputs.apply {
                file(reportFile.get().asFile)
                upToDateWhen { false }
            }
        }
    }
}

private val Project.sourceSets: SourceSetContainer get() = extensions["sourceSets"] as SourceSetContainer
