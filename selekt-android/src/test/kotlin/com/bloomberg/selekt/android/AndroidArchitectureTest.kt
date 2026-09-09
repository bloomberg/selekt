/*
 * Copyright 2026 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

internal class AndroidArchitectureTest {
    @Test
    fun coreDoesNotDependOnAdapters() {
        noClasses()
            .that().resideOutsideOfPackages(ROOM_PACKAGE, SUPPORT_PACKAGE)
            .should().dependOnClassesThat().resideInAnyPackage(ROOM_PACKAGE, SUPPORT_PACKAGE)
            .because("Room and Support SQLite are optional adapters around the Android core")
            .check(productionClasses)
    }

    @Test
    fun adaptersDoNotDependOnEachOther() {
        noClasses()
            .that().resideInAPackage(ROOM_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage(SUPPORT_PACKAGE)
            .because("the Room and Support SQLite adapters must remain independently usable")
            .check(productionClasses)

        noClasses()
            .that().resideInAPackage(SUPPORT_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage(ROOM_PACKAGE)
            .because("the Room and Support SQLite adapters must remain independently usable")
            .check(productionClasses)
    }

    @Test
    fun androidxSQLiteIsConfinedToAdapters() {
        noClasses()
            .that().resideOutsideOfPackages(ROOM_PACKAGE, SUPPORT_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage("androidx.sqlite..")
            .because("AndroidX SQLite is an optional integration API")
            .check(productionClasses)
    }

    private companion object {
        private const val ROOM_PACKAGE = "..android.room.."
        private const val SUPPORT_PACKAGE = "..android.support.."

        private val productionClasses = ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.bloomberg.selekt.android")
    }
}
