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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.kotlin

private fun Test.configureJUnit5() {
    systemProperty("junit.jupiter.execution.parallel.enabled", true)
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.timeout.lifecycle.method.default", "60s")
    systemProperty("junit.jupiter.execution.timeout.mode", "disabled_on_debug")
    systemProperty("junit.jupiter.execution.timeout.testable.method.default", "60s")
}

class SelektPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        tasks.withType(Test::class.java) {
            systemProperty("com.bloomberg.selekt.lib.can_use_embedded", true)
        }
        plugins.apply {
            withType(JavaPlugin::class.java) {
                tasks.withType(Jar::class.java).configureEach {
                    metaInf {
                        from("$rootDir/LICENSE")
                    }
                }
            }
            withId("com.android.application") {
                androidExtension().apply {
                    lintOptions {
                        isWarningsAsErrors = true
                    }
                }
            }
            withId("com.android.library") {
                dependencies.apply {
                    configurations.getByName("androidTestImplementation") {
                        add(name, kotlin("test", Versions.KOTLIN_TEST.version))
                        add(name, kotlin("test-junit", Versions.KOTLIN_TEST.version))
                        add(name, "org.mockito.kotlin:mockito-kotlin:${Versions.MOCKITO_KOTLIN}")
                    }
                }
                androidExtension().apply {
                    lintOptions {
                        isWarningsAsErrors = true
                    }
                    testOptions {
                        unitTests.isIncludeAndroidResources = true
                    }
                }
            }
            arrayOf("java", "com.android.library").forEach { id ->
                withId(id) {
                    dependencies.apply {
                        configurations.getByName("compileOnly").apply {
                            add(name, "com.google.code.findbugs:jsr305:[2.0.2, ${Versions.JSR_305}]")
                        }
                    }
                    dependencies.apply {
                        configurations.getByName("testImplementation") {
                            add(name, kotlin("test-junit5", Versions.KOTLIN_TEST.version))
                            add(name, kotlinX("coroutines-core", Versions.KOTLINX_COROUTINES.version))
                            add(name, kotlinX("coroutines-jdk8", Versions.KOTLINX_COROUTINES.version))
                            add(name, "org.mockito:mockito-core:${Versions.MOCKITO}")
                            add(name, "org.mockito.kotlin:mockito-kotlin:${Versions.MOCKITO_KOTLIN}")
                        }
                    }
                    tasks.withType(Test::class.java) {
                        useJUnitPlatform()
                        configureJUnit5()
                    }
                }
            }
        }
    }
}
