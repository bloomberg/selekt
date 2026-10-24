/*
 * Copyright 2020 Bloomberg Finance L.P.
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

package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SQLStatementKtTest {
    @Test
    fun alterSqlStatement() {
        assertEquals(SQLStatementType.DDL, "ALTER".resolvedSqlStatementType())
    }

    @Test
    fun alterNotSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "AULTER".resolvedSqlStatementType())
    }

    @Test
    fun analyzeSqlStatement() {
        assertEquals(SQLStatementType.UNPREPARED, "ANALYZE".resolvedSqlStatementType())
    }

    @Test
    fun analyzeOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "AMALYZE".resolvedSqlStatementType())
    }

    @Test
    fun attachSqlStatement() {
        assertEquals(SQLStatementType.ATTACH, "ATTACH DATABASE Foo AS Bar".resolvedSqlStatementType())
    }

    @Test
    fun attachOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "APTACH DATABASE Foo AS Bar".resolvedSqlStatementType())
    }

    @Test
    fun beginSqlStatement() {
        assertEquals(SQLStatementType.BEGIN, "BEGIN TRANSACTION".resolvedSqlStatementType())
    }

    @Test
    fun beginOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "GIN TRANSACTION".resolvedSqlStatementType())
    }

    @Test
    fun commitSqlStatement() {
        assertEquals(SQLStatementType.COMMIT, "COMMIT".resolvedSqlStatementType())
    }

    @Test
    fun commitOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "CAMMIT".resolvedSqlStatementType())
    }

    @Test
    fun createSqlStatement() {
        assertEquals(SQLStatementType.DDL, "CREATE".resolvedSqlStatementType())
    }

    @Test
    fun createOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "CEATE".resolvedSqlStatementType())
    }

    @Test
    fun deleteSqlStatement() {
        assertEquals(SQLStatementType.UPDATE, "DELETE".resolvedSqlStatementType())
    }

    @Test
    fun deleteOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "DEELETE".resolvedSqlStatementType())
    }

    @Test
    fun detachSqlStatement() {
        assertEquals(SQLStatementType.UNPREPARED, "DETACH".resolvedSqlStatementType())
    }

    @Test
    fun detachOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "DEPTACH".resolvedSqlStatementType())
    }

    @Test
    fun dropSqlStatement() {
        assertEquals(SQLStatementType.DDL, "DROP".resolvedSqlStatementType())
    }

    @Test
    fun dropOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "DRAP".resolvedSqlStatementType())
    }

    @Test
    fun endSqlStatement() {
        assertEquals(SQLStatementType.COMMIT, "END".resolvedSqlStatementType())
    }

    @Test
    fun endOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "NND".resolvedSqlStatementType())
    }

    @Test
    fun explainSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "EXP".resolvedSqlStatementType())
    }

    @Test
    fun insertSqlStatement() {
        assertEquals(SQLStatementType.UPDATE, "INSERT INTO Foo VALUES (42)".resolvedSqlStatementType())
    }

    @Test
    fun insertOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "NSERT INTO Foo VALUES (42)".resolvedSqlStatementType())
    }

    @Test
    fun otherSqlStatementType() {
        assertEquals(SQLStatementType.OTHER, "XYZ".resolvedSqlStatementType())
    }

    @Test
    fun otherShortSqlStatementType() {
        assertEquals(SQLStatementType.OTHER, "X".resolvedSqlStatementType())
    }

    @Test
    fun pragmaSqlStatementType() {
        assertEquals(SQLStatementType.PRAGMA, "PRAGMA".resolvedSqlStatementType())
    }

    @Test
    fun pragmaOtherSqlStatementType() {
        assertEquals(SQLStatementType.PRAGMA, "PRGMA".resolvedSqlStatementType())
    }

    @Test
    fun reindexSqlStatementType() {
        assertEquals(SQLStatementType.OTHER, "REINDEX ".resolvedSqlStatementType())
    }

    @Test
    fun reindexOtherSqlStatementType() {
        assertEquals(SQLStatementType.OTHER, "RINDEX ".resolvedSqlStatementType())
    }

    @Test
    fun replaceSqlStatement() {
        assertEquals(SQLStatementType.UPDATE, "REPLACE".resolvedSqlStatementType())
    }

    @Test
    fun replaceOtherSqlStatement() {
        assertEquals(SQLStatementType.UPDATE, "REPACE".resolvedSqlStatementType())
    }

    @Test
    fun rollbackSqlStatement() {
        assertEquals(SQLStatementType.ABORT, "ROLLBACK".resolvedSqlStatementType())
    }

    @Test
    fun rollbackOtherSqlStatement() {
        assertEquals(SQLStatementType.ABORT, "RULLBACK".resolvedSqlStatementType())
    }

    @Test
    fun rollbackTransactionSqlStatement() {
        assertEquals(SQLStatementType.ABORT, "ROLLBACK TRANSACTION".resolvedSqlStatementType())
    }

    @Test
    fun rollbackTransactionLowercaseSqlStatement() {
        assertEquals(SQLStatementType.ABORT, "rollback transaction".resolvedSqlStatementType())
    }

    @Test
    fun rollbackToSavepointSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "ROLLBACK TO savepoint1".resolvedSqlStatementType())
    }

    @Test
    fun rollbackTransactionToSavepointSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "ROLLBACK TRANSACTION TO savepoint1".resolvedSqlStatementType())
    }

    @Test
    fun rollbackToSavepointLowercaseSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "rollback to savepoint1".resolvedSqlStatementType())
    }

    @Test
    fun rollbackToSavepointMixedCaseSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "RoLLbAcK tO savepoint1".resolvedSqlStatementType())
    }

    @Test
    fun rollbackToSavepointMinimalSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "ROLLBACK TO sp".resolvedSqlStatementType())
    }

    @Test
    fun rollbackToSavepointLongNameSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "ROLLBACK TO very_long_savepoint_name_123".resolvedSqlStatementType())
    }

    @Test
    fun rollbackToSavepointKeywordSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "ROLLBACK TO SAVEPOINT sp1".resolvedSqlStatementType())
    }

    @Test
    fun rollbackWithoutToSqlStatement() {
        assertEquals(SQLStatementType.ABORT, "ROLLBACK    ".resolvedSqlStatementType())
    }

    @Test
    fun rollbackTruncatedSqlStatement() {
        assertEquals(SQLStatementType.ABORT, "ROLLBACK X".resolvedSqlStatementType())
    }

    @Test
    fun rollbackIntoSqlStatement() {
        assertEquals(SQLStatementType.ABORT, "ROLLBACK INTO something".resolvedSqlStatementType())
    }

    @Test
    fun rollbackToSavepointMultipleWhitespaceSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "ROLLBACK   TO savepoint".resolvedSqlStatementType())
    }

    @Test
    fun releaseSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "RELEASE".resolvedSqlStatementType())
    }

    @Test
    fun releaseSavepointSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "RELEASE sp1".resolvedSqlStatementType())
    }

    @Test
    fun releaseSavepointKeywordSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "RELEASE SAVEPOINT sp1".resolvedSqlStatementType())
    }

    @Test
    fun savePointSqlStatement() {
        assertEquals(SQLStatementType.SAVEPOINT, "SAVEPOINT".resolvedSqlStatementType())
    }

    @Test
    fun selectSqlStatement() {
        assertEquals(SQLStatementType.SELECT, "SELECT * FROM Foo".resolvedSqlStatementType())
    }

    @Test
    fun selectOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "LECT * FROM Foo".resolvedSqlStatementType())
    }

    @Test
    fun updateSqlStatement() {
        assertEquals(SQLStatementType.UPDATE, "UPDATE Foo SET bar=42".resolvedSqlStatementType())
    }

    @Test
    fun updateOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "OPDATE Foo SET bar=42".resolvedSqlStatementType())
    }

    @Test
    fun leadingCommentsAreSkippedBeforeClassification() {
        mapOf(
            "-- audit\r\nSELECT 1" to SQLStatementType.SELECT,
            "/* SELECT */ UPDATE Foo SET bar=42" to SQLStatementType.UPDATE,
            " ; /* audit */ -- more\nBEGIN TRANSACTION" to SQLStatementType.BEGIN,
            "/* audit */ COMMIT" to SQLStatementType.COMMIT,
            "-- audit\nCREATE TABLE Foo(bar)" to SQLStatementType.DDL,
            "/* audit */ PRAGMA user_version" to SQLStatementType.PRAGMA
        ).forEach { (sql, expected) -> assertEquals(expected, sql.resolvedSqlStatementType(), sql) }
    }

    @Test
    fun keywordsInsideLeadingCommentsAreIgnored() {
        assertEquals(SQLStatementType.SELECT, "-- BEGIN\nSELECT 1".resolvedSqlStatementType())
        assertEquals(SQLStatementType.UPDATE, "/* SELECT */ UPDATE Foo SET bar=42".resolvedSqlStatementType())
        assertEquals(SQLStatementType.OTHER, "-- ROLLBACK".resolvedSqlStatementType())
        assertEquals(SQLStatementType.OTHER, "/* SELECT".resolvedSqlStatementType())
    }

    @Test
    fun commentCursorHandlesEndOfInputAndAdjacentTokens() {
        mapOf(
            "-- comment" to SQLStatementType.OTHER,
            "/* comment" to SQLStatementType.OTHER,
            "SELECT/*c*/1" to SQLStatementType.SELECT,
            "--c\nSELECT 1" to SQLStatementType.SELECT
        ).forEach { (sql, expected) -> assertEquals(expected, sql.resolvedSqlStatementType(), sql) }
    }

    @Test
    fun rollbackClauseSkipsCommentsBetweenTokens() {
        assertEquals(SQLStatementType.ABORT, "ROLLBACK /* TO */".resolvedSqlStatementType())
        assertEquals(SQLStatementType.SAVEPOINT, "ROLLBACK /* audit */ TO savepoint1".resolvedSqlStatementType())
        assertEquals(
            SQLStatementType.SAVEPOINT,
            "ROLLBACK -- audit\r\n TRANSACTION /* target */ TO SAVEPOINT savepoint1".resolvedSqlStatementType()
        )
    }

    @Test
    fun transactionControlsAfterLeadingComments() {
        assertEquals(SQLStatementType.BEGIN, "  /* audit */ -- begin\n BEGIN IMMEDIATE TRANSACTION".resolvedSqlStatementType())
        assertEquals(SQLStatementType.COMMIT, "-- audit\nEND TRANSACTION".resolvedSqlStatementType())
        assertEquals(SQLStatementType.ABORT, "/* audit */ ROLLBACK TRANSACTION".resolvedSqlStatementType())
    }

    @Test
    fun savepointControlsAfterLeadingComments() {
        assertEquals(SQLStatementType.SAVEPOINT, "/* audit */ SAVEPOINT sp".resolvedSqlStatementType())
        assertEquals(SQLStatementType.SAVEPOINT, "-- audit\nRELEASE SAVEPOINT sp".resolvedSqlStatementType())
        assertEquals(SQLStatementType.SAVEPOINT, "ROLLBACK /* audit */ TRANSACTION TO SAVEPOINT sp".resolvedSqlStatementType())
    }

    @Test
    fun transactionKeywordsInsideLeadingCommentsAreNotControls() {
        assertEquals(SQLStatementType.SELECT, "-- BEGIN\nSELECT 1".resolvedSqlStatementType())
        assertEquals(SQLStatementType.SELECT, "/* ROLLBACK */ SELECT 1".resolvedSqlStatementType())
    }

    @Test
    fun transactionControlVariants() {
        val controls = listOf(
            "BEGIN TRANSACTION" to SQLStatementType.BEGIN,
            "COMMIT" to SQLStatementType.COMMIT,
            "END" to SQLStatementType.COMMIT,
            "ROLLBACK" to SQLStatementType.ABORT,
            "SAVEPOINT sp" to SQLStatementType.SAVEPOINT,
            "RELEASE SAVEPOINT sp" to SQLStatementType.SAVEPOINT,
            "ROLLBACK TO SAVEPOINT sp" to SQLStatementType.SAVEPOINT,
            "ROLLBACK TRANSACTION TO SAVEPOINT sp" to SQLStatementType.SAVEPOINT
        )

        controls.forEach { (sql, expected) -> assertControlVariants(sql, expected) }
    }

    @Test
    fun transactionKeywordsOnlyInsideCommentsAreNotControls() {
        listOf("BEGIN", "COMMIT", "END", "ROLLBACK", "SAVEPOINT", "RELEASE").forEach { keyword ->
            assertEquals(SQLStatementType.SELECT, "-- $keyword\r\nSELECT 1".resolvedSqlStatementType())
            assertEquals(SQLStatementType.SELECT, "/* $keyword */ ;\r\nSELECT 1".resolvedSqlStatementType())
        }
    }

    private fun String.caseVariants() = listOf(
        this,
        lowercase(),
        mapIndexed { index, character ->
            if (index % 2 == 0) { character.uppercaseChar() } else { character.lowercaseChar() }
        }.joinToString("")
    )

    private fun assertControlVariants(sql: String, expected: SQLStatementType) = sql.controlVariants().forEach { variant ->
        assertEquals(expected, variant.resolvedSqlStatementType(), variant)
    }

    private fun String.controlVariants(): List<String> {
        val interTokenVariants = caseVariants().flatMap { caseVariant ->
            TOKEN_SEPARATORS.map { separator -> caseVariant.replace(" ", separator) }
        }
        return interTokenVariants.flatMap { variant ->
            LEADING_SEPARATORS.map { prefix -> prefix + variant + ";\r\n" }
        }
    }

    private companion object {
        val LEADING_SEPARATORS = listOf("", " \t;\r\n", "-- leading\r\n", "/* leading */ ; ")
        val TOKEN_SEPARATORS = listOf(" ", "\t", "\r\n", " /* between */ ", "-- between\r\n")
    }
}
