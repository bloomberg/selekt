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
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.kotlin

private fun Test.configureJUnit5() {
    systemProperty("junit.jupiter.execution.parallel.enabled", true)
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
}

class SelektPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        tasks.withType(Test::class.java) {
            systemProperty("com.bloomberg.selekt.lib.can_use_embedded", true)
        }
        pluginManager.apply {
            withPlugin("java") {
                dependencies.apply {
                    configurations.getByName("implementation") {
                        add(name, kotlin("stdlib-jdk8:${Versions.KOTLIN}"))
                    }
                    configurations.getByName("testImplementation") {
                        add(name, "org.junit.jupiter:junit-jupiter:${Versions.JUNIT5}")
                    }
                    configurations.getByName("testRuntimeOnly") {
                        add(name, "org.junit.platform:junit-platform-launcher:${Versions.JUNIT5_PLATFORM}")
                        add(name, "org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT5}")
                        add(name, "org.junit.vintage:junit-vintage-engine:${Versions.JUNIT5}")
                    }
                }
                tasks.withType(Test::class.java) {
                    useJUnitPlatform()
                    configureJUnit5()
                }
                tasks.withType(Jar::class.java).configureEach {
                    metaInf {
                        from("$rootDir/LICENSE")
                    }
                }
            }
            withPlugin("com.android.application") {
                androidExtension().apply {
                    lintOptions {
                        isWarningsAsErrors = true
                        // FIXME Remove when all dependencies are available elsewhere.
                        disable("JcenterRepositoryObsolete")
                    }
                }
            }
            withPlugin("com.android.library") {
                dependencies.apply {
                    configurations.getByName("androidTestImplementation") {
                        add(name, kotlin("test", Versions.KOTLIN.version))
                        add(name, kotlin("test-junit", Versions.KOTLIN.version))
                        add(name, "org.mockito.kotlin:mockito-kotlin:${Versions.MOCKITO_KOTLIN}")
                    }
                }
                androidExtension().apply {
                    lintOptions {
                        isWarningsAsErrors = true
                        // FIXME Remove when all dependencies are available elsewhere.
                        disable("JcenterRepositoryObsolete")
                    }
                    testOptions {
                        unitTests.apply {
                            if (project.hasProperty("robolectricDependencyRepoUrl")) {
                                all {
                                    it.systemProperty("robolectric.dependency.repo.url",
                                        requireNotNull(project.properties["robolectricDependencyRepoUrl"]))
                                }
                            }
                            isIncludeAndroidResources = true
                        }
                    }
                }
            }
            arrayOf("java", "com.android.library").forEach { id ->
                withPlugin(id) {
                    dependencies.apply {
                        configurations.findByName("compileOnly")?.apply {
                            add(name, "com.google.code.findbugs:jsr305:${Versions.JSR_305}")
                        }
                    }
                    dependencies.apply {
                        configurations.getByName("testImplementation") {
                            add(name, kotlin("test", Versions.KOTLIN.version))
                            add(name, kotlin("test-junit", Versions.KOTLIN.version))
                            add(name, kotlinX("coroutines-core", Versions.KOTLIN_COROUTINES.version))
                            add(name, kotlinX("coroutines-jdk8", Versions.KOTLIN_COROUTINES.version))
                            add(name, "org.assertj:assertj-core:${Versions.ASSERT_J}")
                            add(name, "org.mockito:mockito-core:${Versions.MOCKITO}")
                            add(name, "org.mockito.kotlin:mockito-kotlin:${Versions.MOCKITO_KOTLIN}")
                        }
                    }
                }
            }
        }
    }
}
