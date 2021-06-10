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

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.factory.AndroidUnitTest
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.get
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Task.jacocoTaskExtension() = extensions["jacoco"] as JacocoTaskExtension

internal fun TaskContainer.jacocoTestReportTasks() = withType(JacocoReport::class.java)

internal val TaskContainer.jacocoTestReportTask: JacocoReport?
    get() = jacocoTestReportTasks().firstOrNull { it.name == "jacocoTestReport" }

internal fun TaskContainer.unitTestTask(variant: BaseVariant) =
    getByName("test${variant.name.capitalize()}UnitTest") as AndroidUnitTest
