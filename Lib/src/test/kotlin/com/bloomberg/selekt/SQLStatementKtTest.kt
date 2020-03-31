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

package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SQLStatementKtTest {
    @Test
    fun alterSqlStatement() {
        assertEquals(SQLStatementType.DDL, "ALTER".sqlStatementType())
    }

    @Test
    fun alterNotSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "AULTER".sqlStatementType())
    }

    @Test
    fun analyzeSqlStatement() {
        assertEquals(SQLStatementType.UNPREPARED, "ANALYZE".sqlStatementType())
    }

    @Test
    fun analyzeOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "AMALYZE".sqlStatementType())
    }

    @Test
    fun attachSqlStatement() {
        assertEquals(SQLStatementType.ATTACH, "ATTACH DATABASE Foo AS Bar".sqlStatementType())
    }

    @Test
    fun attachOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "APTACH DATABASE Foo AS Bar".sqlStatementType())
    }

    @Test
    fun beginSqlStatement() {
        assertEquals(SQLStatementType.BEGIN, "BEGIN TRANSACTION".sqlStatementType())
    }

    @Test
    fun beginOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "GIN TRANSACTION".sqlStatementType())
    }

    @Test
    fun commitSqlStatement() {
        assertEquals(SQLStatementType.COMMIT, "COMMIT".sqlStatementType())
    }

    @Test
    fun commitOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "CAMMIT".sqlStatementType())
    }

    @Test
    fun createSqlStatement() {
        assertEquals(SQLStatementType.DDL, "CREATE".sqlStatementType())
    }

    @Test
    fun createOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "CEATE".sqlStatementType())
    }

    @Test
    fun deleteSqlStatement() {
        assertEquals(SQLStatementType.UPDATE, "DELETE".sqlStatementType())
    }

    @Test
    fun deleteOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "DEELETE".sqlStatementType())
    }

    @Test
    fun detachSqlStatement() {
        assertEquals(SQLStatementType.UNPREPARED, "DETACH".sqlStatementType())
    }

    @Test
    fun detachOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "DEPTACH".sqlStatementType())
    }

    @Test
    fun dropSqlStatement() {
        assertEquals(SQLStatementType.DDL, "DROP".sqlStatementType())
    }

    @Test
    fun dropOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "DRAP".sqlStatementType())
    }

    @Test
    fun endSqlStatement() {
        assertEquals(SQLStatementType.COMMIT, "END".sqlStatementType())
    }

    @Test
    fun endOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "NND".sqlStatementType())
    }

    @Test
    fun explainSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "EXP".sqlStatementType())
    }

    @Test
    fun insertSqlStatement() {
        assertEquals(SQLStatementType.UPDATE, "INSERT INTO Foo VALUES (42)".sqlStatementType())
    }

    @Test
    fun insertOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "NSERT INTO Foo VALUES (42)".sqlStatementType())
    }

    @Test
    fun otherSqlStatementType() {
        assertEquals(SQLStatementType.OTHER, "XYZ".sqlStatementType())
    }

    @Test
    fun otherShortSqlStatementType() {
        assertEquals(SQLStatementType.OTHER, "X".sqlStatementType())
    }

    @Test
    fun replaceSqlStatement() {
        assertEquals(SQLStatementType.UPDATE, "REPLACE".sqlStatementType())
    }

    @Test
    fun replaceOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "RPLACE".sqlStatementType())
    }

    @Test
    fun rollbackSqlStatement() {
        assertEquals(SQLStatementType.ABORT, "ROLLBACK".sqlStatementType())
    }

    @Test
    fun rollbackOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "RULLBACK".sqlStatementType())
    }

    @Test
    fun savePointSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "SAVEPOINT".sqlStatementType())
    }

    @Test
    fun selectSqlStatement() {
        assertEquals(SQLStatementType.SELECT, "SELECT * FROM Foo".sqlStatementType())
    }

    @Test
    fun selectOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "LECT * FROM Foo".sqlStatementType())
    }

    @Test
    fun updateSqlStatement() {
        assertEquals(SQLStatementType.UPDATE, "UPDATE Foo SET bar=42".sqlStatementType())
    }

    @Test
    fun updateOtherSqlStatement() {
        assertEquals(SQLStatementType.OTHER, "OPDATE Foo SET bar=42".sqlStatementType())
    }
}
