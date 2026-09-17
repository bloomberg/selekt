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

package com.bloomberg.selekt.android.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import kotlin.test.Test

internal class TrustedSqlDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = TrustedSqlDetector()

    override fun getIssues(): List<Issue> = listOf(TrustedSqlDetector.ISSUE)

    @Test
    fun acceptsConstantsAndExplicitlyTrustedSources() {
        lint().allowMissingSdk().files(
            trustedSqlAnnotation,
            databaseApi,
            kotlin(
                """
                package test.pkg

                import com.bloomberg.selekt.annotations.TrustedSql

                private const val QUERY = "SELECT * FROM data WHERE id=?"

                class Good {
                    fun constants(database: Database) {
                        database.exec("DELETE FROM data")
                        database.exec(QUERY)
                        database.query(arrayOf("id", "name"))
                        database.where(null)
                        Query("SELECT * FROM data")
                    }

                    fun forwarded(database: Database, @TrustedSql sql: String) {
                        database.exec(sql)
                    }

                    @TrustedSql
                    fun generated(): String = "SELECT * FROM data"

                    fun generated(database: Database) {
                        database.exec(generated())
                    }
                }
                """.trimIndent()
            )
        ).run().expectClean()
    }

    @Test
    fun warnsForDynamicSqlStructure() {
        lint().allowMissingSdk().files(
            trustedSqlAnnotation,
            databaseApi,
            kotlin(
                """
                package test.pkg

                class Bad {
                    fun execute(database: Database, external: String) {
                        database.exec(external)
                        database.exec("SELECT * FROM " + external)
                        database.query(arrayOf("id", external))
                        Query(external)
                    }
                }
                """.trimIndent()
            )
        ).run().expectWarningCount(4)
    }

    private companion object {
        val trustedSqlAnnotation = kotlin(
            """
            package com.bloomberg.selekt.annotations

            @Retention(AnnotationRetention.BINARY)
            @Target(
                AnnotationTarget.FIELD,
                AnnotationTarget.FUNCTION,
                AnnotationTarget.LOCAL_VARIABLE,
                AnnotationTarget.PROPERTY,
                AnnotationTarget.PROPERTY_GETTER,
                AnnotationTarget.VALUE_PARAMETER
            )
            annotation class TrustedSql
            """.trimIndent()
        )

        val databaseApi = kotlin(
            """
            package test.pkg

            import com.bloomberg.selekt.annotations.TrustedSql

            class Database {
                fun exec(@TrustedSql sql: String) = Unit
                fun query(@TrustedSql columns: Array<out String>) = Unit
                fun where(@TrustedSql clause: String?) = Unit
            }

            class Query(@TrustedSql sql: String)
            """.trimIndent()
        )
    }
}
