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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal fun Project.isAndroidApp() = plugins.findAndroidAppPlugin() != null

internal fun Project.isAndroidLibrary() = plugins.findAndroidLibraryPlugin() != null

internal fun Project.isAndroid() = isAndroidApp() || isAndroidLibrary()

internal fun Project.androidExtension() = extensions.getByType<TestedExtension>()

internal fun Project.androidAppExtension() = extensions.getByType<AppExtension>()

internal fun Project.androidLibraryExtension() = extensions.getByType<LibraryExtension>()
