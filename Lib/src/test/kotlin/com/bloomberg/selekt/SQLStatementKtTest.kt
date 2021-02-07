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
    fun savePointSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "SAVEPOINT".resolvedSqlStatementType())
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
}
