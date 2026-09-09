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

package com.bloomberg.selekt.jdbc

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

internal class JdbcArchitectureTest {
    @Test
    fun lowLevelPackagesDoNotDependOnCoordinatingPackages() {
        noClasses()
            .that().resideInAnyPackage(*LOW_LEVEL_PACKAGES)
            .should().dependOnClassesThat().resideInAnyPackage(*COORDINATING_PACKAGES)
            .because("low-level JDBC utilities and value implementations must remain reusable")
            .check(productionClasses)
    }

    @Test
    fun driverDoesNotBypassConnectionBoundary() {
        noClasses()
            .that().resideInAPackage(DRIVER_PACKAGE)
            .should().dependOnClassesThat().resideInAnyPackage(*CONNECTION_INTERNAL_PACKAGES)
            .because("the driver should coordinate JDBC work through the connection package")
            .check(productionClasses)
    }

    @Test
    fun nativeSQLiteApiIsConfinedToDriver() {
        noClasses()
            .that().resideOutsideOfPackage(DRIVER_PACKAGE)
            .should().dependOnClassesThat().haveFullyQualifiedName("com.bloomberg.selekt.IExternalSQLite")
            .because("native SQLite bootstrapping belongs in the JDBC driver")
            .check(productionClasses)
    }

    private companion object {
        private const val DRIVER_PACKAGE = "..jdbc.driver.."

        private val LOW_LEVEL_PACKAGES = arrayOf(
            "..jdbc.exception..",
            "..jdbc.lob..",
            "..jdbc.util.."
        )
        private val COORDINATING_PACKAGES = arrayOf(
            "..jdbc.connection..",
            DRIVER_PACKAGE,
            "..jdbc.metadata..",
            "..jdbc.result..",
            "..jdbc.statement.."
        )
        private val CONNECTION_INTERNAL_PACKAGES = arrayOf(
            "..jdbc.lob..",
            "..jdbc.metadata..",
            "..jdbc.result..",
            "..jdbc.statement.."
        )

        private val productionClasses = ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.bloomberg.selekt.jdbc")
    }
}
