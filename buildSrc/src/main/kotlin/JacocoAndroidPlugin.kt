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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Locale

class JacocoAndroidPlugin : Plugin<Project> {
    @ExperimentalStdlibApi
    override fun apply(project: Project) {
        project.extensions.create(JACOCO_ANDROID_TEST_REPORT, JacocoAndroidUnitTestReportExtension::class.java)
        project.afterEvaluate {
            takeIf { it.isAndroid() }?.run {
                plugins.apply(JacocoPlugin::class.java)
                when {
                    isAndroidApp() -> androidAppExtension().applicationVariants
                    isAndroidLibrary() -> androidLibraryExtension().libraryVariants
                    else -> error("Unrecognised Android project type for project '$name'.")
                }.all {
                    val testTask = this@run.tasks.unitTestTask(this@all).apply {
                        jacocoTaskExtension().isIncludeNoLocationClasses = true
                    }
                    tasks.register(
                        "jacocoTest${name.capitalize(Locale.US)}UnitTestReport",
                        JacocoReport::class.java
                    ) {
                        group = "verification"
                        description = "Generates code coverage report for the ${testTask.name} variant unit test task."
                        sourceDirectories.from(
                            this@all.sourceSets.flatMap { it.javaDirectories },
                            "${projectDir.path}/src/main/kotlin"
                        )
                        classDirectories.from(
                            fileTree(
                                mapOf(
                                    "dir" to this@all.javaCompileProvider.get().destinationDir,
                                    "exclude" to jacocoAndroidUnitTestReportExtension().excludes
                                )
                            ) + fileTree(
                                mapOf(
                                    "dir" to "$buildDir/tmp/kotlin-classes/${this@all.name}",
                                    "exclude" to jacocoAndroidUnitTestReportExtension().excludes
                                )
                            )
                        )
                        executionData.from(testTask.jacocoTaskExtension().destinationFile!!.path)
                        reports {
                            csv.isEnabled = false
                            html.isEnabled = true
                            xml.isEnabled = true
                        }
                    }
                }
            } ?: error(
                "'${this.name}' is not an Android project, and the JaCoCo Android Plugin for Gradle " +
                    "may only be applied to an Android project."
            )
        }
    }

    private fun Project.jacocoAndroidUnitTestReportExtension() =
        extensions.getByType(JacocoAndroidUnitTestReportExtension::class.java)

    private companion object {
        const val JACOCO_ANDROID_TEST_REPORT = "jacocoAndroidUnitTestReport"
    }
}

open class JacocoAndroidUnitTestReportExtension(
    val excludes: Collection<String>,
    var preferredVariant: String = "debug"
) {
    constructor() : this(defaultExcludes)

    companion object {
        private val androidDataBindingExcludes = listOf(
            "android/databinding/**/*.class",
            "**/android/databinding/*Binding.class",
            "**/BR.*"
        )

        private val androidExcludes = listOf(
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/R.class",
            "**/R$*.class"
        )

        private val androidRoomExcludes = listOf(
            "**/*_Impl.class"
        )

        val defaultExcludes = androidDataBindingExcludes + androidExcludes + androidRoomExcludes
    }
}
