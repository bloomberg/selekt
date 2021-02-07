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

@file:Suppress("UnstableApiUsage")

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.apply

import javax.inject.Inject

class AndroidMavenPublishPlugin @Inject constructor(
    private val objectFactory: ObjectFactory,
    private val softwareComponentFactory: SoftwareComponentFactory
) : Plugin<Project> {
    override fun apply(target: Project): Unit = target.run {
        plugins.apply(MavenPublishPlugin::class)
        pluginManager.withPlugin("com.android.library") {
            arrayOf("aar", "jar").forEach {
                val component = softwareComponentFactory.adhoc(it).apply {
                    addVariantsFromConfiguration(configurations.getByName("api").attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage::class.java, Usage.JAVA_API))
                    }) {
                        mapToMavenScope("compile")
                    }
                    addVariantsFromConfiguration(configurations.getByName("implementation").attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage::class.java, Usage.JAVA_RUNTIME))
                    }) {
                        mapToMavenScope("runtime")
                    }
                }
                check(project.components.add(component)) { "Failed to register component." }
            }
        }
    }
}
