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

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.plugins.PluginContainer

import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

internal fun PluginContainer.findAndroidAppPlugin() = findPlugin(AppPlugin::class.java)

internal fun PluginContainer.findAndroidLibraryPlugin() = findPlugin(LibraryPlugin::class.java)

internal val PluginContainer.hasKotlinPlugin
    get() = hasPlugin(KotlinPluginWrapper::class.java)

internal val PluginContainer.hasKotlinAndroidPlugin
    get() = hasPlugin(KotlinAndroidPluginWrapper::class.java)
