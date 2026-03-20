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

package com.bloomberg.selekt.jdbc.metadata

import com.bloomberg.selekt.ColumnType
import com.bloomberg.selekt.ICursor
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.RowIdLifetime
import java.sql.SQLException
import java.sql.Types
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class JdbcDatabaseMetaDataTest {
    private lateinit var mockDatabase: SQLDatabase
    private lateinit var mockConnection: JdbcConnection
    private lateinit var mockCursor: ICursor
    private lateinit var metaData: JdbcDatabaseMetaData

    @BeforeEach
    fun setUp() {
        mockCursor = mock {
            whenever(it.moveToNext()).doReturn(false)
            whenever(it.columnCount).doReturn(0)
            whenever(it.columnNames()).doReturn(emptyArray())
            whenever(it.isClosed()).doReturn(false)
        }
        mockDatabase = mock {
            whenever(it.query(any<String>(), any<Array<Any?>>())).doReturn(mockCursor)
        }
        val connectionURL = ConnectionURL.parse("jdbc:sqlite:/tmp/test.db")
        mockConnection = JdbcConnection(mockDatabase, connectionURL, Properties())
        metaData = JdbcDatabaseMetaData(mockConnection, mockDatabase, connectionURL)
    }

    @Test
    fun getConnection() {
        assertEquals(mockConnection, metaData.connection)
    }

    @Test
    fun databaseProductInfo(): Unit = metaData.run {
        assertEquals("SQLite", databaseProductName)
        assertNotNull(databaseProductVersion)
        assertTrue(databaseProductVersion.isNotEmpty())
    }

    @Test
    fun driverInfo(): Unit = metaData.run {
        assertEquals("Selekt SQLite JDBC", driverName)
        assertEquals("4.3", driverVersion)
        assertEquals(4, driverMajorVersion)
        assertEquals(3, driverMinorVersion)
    }

    @Test
    fun jdbcVersion(): Unit = metaData.run {
        assertEquals(4, jdbcMajorVersion)
        assertEquals(3, jdbcMinorVersion)
    }

    @Test
    fun sqlKeywords(): Unit = metaData.sqlKeywords.run {
        assertTrue(contains("PRAGMA"))
        assertTrue(contains("AUTOINCREMENT"))
    }

    @Test
    fun identifierInfo(): Unit = metaData.run {
        assertEquals("\"", identifierQuoteString)
        assertFalse(isCatalogAtStart)
        assertEquals(".", catalogSeparator)
        assertEquals("", schemaTerm)
        assertEquals("", catalogTerm)
        assertEquals("table", procedureTerm)
    }

    @Test
    fun transactionSupport(): Unit = metaData.run {
        assertTrue(supportsTransactions())
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, defaultTransactionIsolation)
        assertTrue(supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE))
        assertFalse(supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED))
        assertFalse(supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED))
        assertFalse(supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ))
    }

    @Test
    fun sqlSupport(): Unit = metaData.run {
        assertTrue(supportsAlterTableWithAddColumn())
        assertTrue(supportsAlterTableWithDropColumn())
        assertFalse(supportsColumnAliasing())
        assertTrue(supportsConvert())
        assertFalse(supportsTableCorrelationNames())
        assertTrue(supportsDifferentTableCorrelationNames())
        assertTrue(supportsExpressionsInOrderBy())
        assertTrue(supportsOrderByUnrelated())
        assertTrue(supportsGroupBy())
        assertTrue(supportsGroupByUnrelated())
        assertTrue(supportsGroupByBeyondSelect())
        assertTrue(supportsLikeEscapeClause())
        assertFalse(supportsMultipleResultSets())
        assertFalse(supportsMultipleTransactions())
        assertTrue(supportsNonNullableColumns())
        assertTrue(supportsMinimumSQLGrammar())
        assertTrue(supportsCoreSQLGrammar())
        assertFalse(supportsExtendedSQLGrammar())
        assertTrue(supportsANSI92EntryLevelSQL())
        assertFalse(supportsANSI92IntermediateSQL())
        assertFalse(supportsANSI92FullSQL())
    }

    @Test
    fun resultSetSupport(): Unit = metaData.run {
        assertTrue(supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY))
        assertFalse(supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE))
        assertFalse(supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE))
        assertTrue(supportsResultSetConcurrency(
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY
        ))
        assertFalse(supportsResultSetConcurrency(
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_UPDATABLE
        ))
    }

    @Test
    fun joinSupport(): Unit = metaData.run {
        assertTrue(supportsOuterJoins())
        assertTrue(supportsFullOuterJoins())
        assertTrue(supportsLimitedOuterJoins())
    }

    @Test
    fun subquerySupport(): Unit = metaData.run {
        assertTrue(supportsSubqueriesInComparisons())
        assertTrue(supportsSubqueriesInExists())
        assertTrue(supportsSubqueriesInIns())
        assertTrue(supportsSubqueriesInQuantifieds())
        assertTrue(supportsCorrelatedSubqueries())
    }

    @Test
    fun unionSupport(): Unit = metaData.run {
        assertTrue(supportsUnion())
        assertTrue(supportsUnionAll())
    }

    @Test
    fun cursorSupport(): Unit = metaData.run {
        assertFalse(supportsOpenCursorsAcrossCommit())
        assertFalse(supportsOpenCursorsAcrossRollback())
        assertFalse(supportsOpenStatementsAcrossCommit())
        assertFalse(supportsOpenStatementsAcrossRollback())
    }

    @Test
    fun ddlSupport(): Unit = metaData.run {
        assertFalse(dataDefinitionCausesTransactionCommit())
        assertFalse(dataDefinitionIgnoredInTransactions())
        assertTrue(supportsBatchUpdates())
    }

    @Test
    fun namingLimits(): Unit = metaData.run {
        assertEquals(0, maxBinaryLiteralLength)
        assertEquals(0, maxCharLiteralLength)
        assertEquals(0, maxColumnNameLength)
        assertEquals(0, maxColumnsInGroupBy)
        assertEquals(0, maxColumnsInIndex)
        assertEquals(0, maxColumnsInOrderBy)
        assertEquals(0, maxColumnsInSelect)
        assertEquals(0, maxColumnsInTable)
        assertEquals(0, maxConnections)
        assertEquals(0, maxCursorNameLength)
        assertEquals(0, maxIndexLength)
        assertEquals(0, maxProcedureNameLength)
        assertEquals(0, maxRowSize)
        assertEquals(0, maxSchemaNameLength)
        assertEquals(0, maxStatementLength)
        assertEquals(0, maxStatements)
        assertEquals(0, maxTableNameLength)
        assertEquals(0, maxTablesInSelect)
        assertEquals(0, maxUserNameLength)
    }

    @Test
    fun miscellaneousProperties(): Unit = metaData.run {
        mockConnection.isReadOnly = true
        assertTrue(isReadOnly)
        assertFalse(locatorsUpdateCopy())
        assertTrue(usesLocalFiles())
        assertTrue(usesLocalFilePerTable())
        assertFalse(storesUpperCaseIdentifiers())
        assertFalse(storesLowerCaseIdentifiers())
        assertTrue(storesMixedCaseIdentifiers())
        assertFalse(storesUpperCaseQuotedIdentifiers())
        assertFalse(storesLowerCaseQuotedIdentifiers())
        assertTrue(storesMixedCaseQuotedIdentifiers())
        assertFalse(supportsMixedCaseIdentifiers())
        assertTrue(supportsMixedCaseQuotedIdentifiers())
        assertTrue(doesMaxRowSizeIncludeBlobs())
        assertFalse(nullsAreSortedHigh())
        assertFalse(nullsAreSortedLow())
        assertTrue(nullsAreSortedAtStart())
        assertFalse(nullsAreSortedAtEnd())
        assertFalse(allProceduresAreCallable())
        assertTrue(allTablesAreSelectable())
        assertEquals("jdbc:sqlite:/tmp/test.db", url)
        assertEquals("", userName)
    }

    @Test
    fun unsupportedFeatures(): Unit = metaData.run {
        assertFalse(supportsStoredProcedures())
        assertFalse(supportsMultipleResultSets())
        assertTrue(supportsGetGeneratedKeys())
        assertFalse(supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT))
        assertTrue(supportsResultSetHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT))
        assertTrue(supportsSavepoints())
        assertFalse(supportsNamedParameters())
        assertFalse(supportsMultipleOpenResults())
        assertFalse(supportsStatementPooling())
        assertFalse(supportsStoredFunctionsUsingCallSyntax())
        assertFalse(autoCommitFailureClosesAllResultSets())
    }

    @Test
    fun getTables() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mock<ICursor>())
        assertNotNull(metaData.getTables(null, null, "%", arrayOf("TABLE")))
    }

    @Test
    fun getColumns() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mock<ICursor>())
        assertNotNull(metaData.getColumns(null, null, "users", "%"))
    }

    @Test
    fun getPrimaryKeys() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mock<ICursor>())
        assertNotNull(metaData.getPrimaryKeys(null, null, "users"))
    }

    @Test
    fun getIndexInfo() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mock<ICursor>())
        assertNotNull(metaData.getIndexInfo(null, null, "users", unique = false, approximate = false))
    }

    @Test
    fun unsupportedMetaDataMethods(): Unit = metaData.run {
        assertFailsWith<SQLException> {
            getProcedures(null, null, "%")
        }
        assertFailsWith<SQLException> {
            getProcedureColumns(null, null, "%", "%")
        }
        assertFailsWith<SQLException> {
            getSchemas()
        }
        assertFailsWith<SQLException> {
            catalogs
        }
        assertFailsWith<SQLException> {
            getTablePrivileges(null, null, "%")
        }
        assertFailsWith<SQLException> {
            getColumnPrivileges(null, null, "users", "%")
        }
        assertFailsWith<SQLException> {
            getBestRowIdentifier(null, null, "users", 0, false)
        }
        assertFailsWith<SQLException> {
            getVersionColumns(null, null, "users")
        }
        assertFailsWith<SQLException> {
            getImportedKeys(null, null, "users")
        }
        assertFailsWith<SQLException> {
            getExportedKeys(null, null, "users")
        }
        assertFailsWith<SQLException> {
            getCrossReference(null, null, "users", null, null, "orders")
        }
        assertFailsWith<SQLException> {
            getUDTs(null, null, "%", null)
        }
        assertFailsWith<SQLException> {
            getSuperTypes(null, null, "%")
        }
        assertFailsWith<SQLException> {
            getSuperTables(null, null, "%")
        }
        assertFailsWith<SQLException> {
            getAttributes(null, null, "%", "%")
        }
    }

    @Test
    fun typeInfo() {
        assertNotNull(metaData.typeInfo)
    }

    @Test
    fun tableTypes() {
        assertNotNull(metaData.tableTypes)
    }

    @Test
    fun wrapperInterface(): Unit = metaData.run {
        assertTrue(isWrapperFor(JdbcDatabaseMetaData::class.java))
        assertFalse(isWrapperFor(String::class.java))
        assertSame(metaData, metaData.unwrap(JdbcDatabaseMetaData::class.java))
        assertFailsWith<SQLException> {
            unwrap(String::class.java)
        }
    }

    @Test
    fun numericFunctions(): Unit = metaData.numericFunctions.run {
        listOf(
            "ABS",
            "MAX",
            "MIN",
            "ROUND"
        ).forEach {
            assertTrue(contains(it))
        }
    }

    @Test
    fun stringFunctions(): Unit = metaData.stringFunctions.run {
        listOf(
            "LENGTH",
            "LOWER",
            "UPPER",
            "SUBSTR"
        ).forEach {
            assertTrue(contains(it))
        }
    }

    @Test
    fun systemFunctions(): Unit = metaData.systemFunctions.run {
        listOf(
            "COALESCE",
            "IFNULL",
            "NULLIF"
        ).forEach {
            assertTrue(contains(it))
        }
    }

    @Test
    fun timeDateFunctions(): Unit = metaData.timeDateFunctions.run {
        listOf(
            "DATE",
            "TIME",
            "DATETIME",
            "STRFTIME"
        ).forEach {
            assertTrue(contains(it))
        }
    }

    @Test
    fun searchStringEscape() {
        assertEquals("\\", metaData.searchStringEscape)
    }

    @Test
    fun extraNameCharacters() {
        assertEquals("", metaData.extraNameCharacters)
    }

    @Test
    fun getTablesWithSpecificTypes() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mockCursor)
        metaData.run {
            assertNotNull(getTables(null, null, "%", arrayOf("VIEW")))
            assertNotNull(getTables(null, null, "%", arrayOf("TABLE", "VIEW")))
        }
    }

    @Test
    fun getTablesWithNullTypes() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mockCursor)
        assertNotNull(metaData.getTables(null, null, "%", null))
    }

    @Test
    fun getTablesWithEmptyTypes() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mockCursor)
        assertNotNull(metaData.getTables(null, null, "%", arrayOf()))
    }

    @Test
    fun getTablesWithoutPattern() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mockCursor)
        assertNotNull(metaData.getTables(null, null, null, null))
    }

    @Test
    fun getIndexInfoUnique() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mockCursor)
        assertNotNull(metaData.getIndexInfo(null, null, "users", unique = true, approximate = false))
    }

    @Test
    fun unwrapToSQLDatabase() {
        assertSame(mockDatabase, metaData.unwrap(SQLDatabase::class.java))
    }

    @Test
    fun isWrapperForSQLDatabase() {
        assertTrue(metaData.isWrapperFor(SQLDatabase::class.java))
    }

    @Test
    fun supportsResultSetTypeScrollInsensitive() {
        assertFalse(metaData.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE))
    }

    @Test
    fun supportsResultSetTypeScrollSensitive() {
        assertFalse(metaData.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE))
    }

    @Test
    fun supportsResultSetConcurrencyUpdatable() {
        assertFalse(metaData.supportsResultSetConcurrency(
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_UPDATABLE
        ))
    }

    @Test
    fun transactionIsolationLevels(): Unit = metaData.run {
        assertFalse(supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED))
        assertFalse(supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED))
        assertFalse(supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ))
        assertTrue(supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE))
    }

    @Test
    fun visibilityMethods(): Unit = metaData.run {
        assertFalse(othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY))
        assertFalse(othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY))
        assertFalse(othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY))
        assertFalse(ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY))
        assertFalse(ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY))
        assertFalse(ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY))
    }

    @Test
    fun detectionMethods(): Unit = metaData.run {
        assertFalse(deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY))
        assertFalse(insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY))
        assertFalse(updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY))
    }

    @Test
    fun additionalSupportMethods(): Unit = metaData.run {
        assertFalse(supportsCatalogsInDataManipulation())
        assertFalse(supportsCatalogsInIndexDefinitions())
        assertFalse(supportsCatalogsInPrivilegeDefinitions())
        assertFalse(supportsCatalogsInProcedureCalls())
        assertFalse(supportsCatalogsInTableDefinitions())
        assertFalse(supportsSchemasInDataManipulation())
        assertFalse(supportsSchemasInProcedureCalls())
        assertFalse(supportsSchemasInTableDefinitions())
        assertFalse(supportsSchemasInIndexDefinitions())
        assertFalse(supportsSchemasInPrivilegeDefinitions())
    }

    @Test
    fun additionalProperties(): Unit = metaData.run {
        assertTrue(supportsDataDefinitionAndDataManipulationTransactions())
        assertFalse(supportsDataManipulationTransactionsOnly())
        assertFalse(supportsIntegrityEnhancementFacility())
        assertTrue(nullPlusNonNullIsNull())
        assertFalse(generatedKeyAlwaysReturned())
        assertEquals(RowIdLifetime.ROWID_UNSUPPORTED, metaData.rowIdLifetime)
    }

    @Test
    fun resultSetHoldability() {
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, metaData.resultSetHoldability)
    }

    @Test
    fun sqlStateType() {
        assertEquals(DatabaseMetaData.sqlStateSQL99, metaData.sqlStateType)
    }

    @Test
    fun databaseVersions(): Unit = metaData.run {
        assertEquals(3, databaseMajorVersion)
        assertEquals(51, databaseMinorVersion)
    }

    @Test
    fun unsupportedFeaturesMethods(): Unit = metaData.run {
        assertFailsWith<SQLException> {
            clientInfoProperties
        }
        assertFailsWith<SQLException> {
            getFunctions(null, null, "%")
        }
        assertFailsWith<SQLException> {
            getFunctionColumns(null, null, "%", "%")
        }
        assertFailsWith<SQLException> {
            getPseudoColumns(null, null, "%", "%")
        }
    }

    @Test
    fun getSchemasWithParameters() {
        assertFailsWith<SQLException> {
            metaData.getSchemas("catalog", "schema")
        }
    }

    @Test
    fun supportsSelectForUpdate() {
        assertFalse(metaData.supportsSelectForUpdate())
    }

    @Test
    fun supportsPositionedUpdate() {
        assertFalse(metaData.supportsPositionedUpdate())
    }

    @Test
    fun supportsPositionedDelete() {
        assertFalse(metaData.supportsPositionedDelete())
    }

    @Test
    fun getMaxCatalogNameLength() {
        assertEquals(0, metaData.maxCatalogNameLength)
    }

    @Test
    fun supportsConvertWithParameters() {
        assertTrue(metaData.supportsConvert(Types.INTEGER, Types.VARCHAR))
    }

    private fun makeTablesCursor(): ICursor {
        val n = arrayOf("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS",
            "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SELF_REFERENCING_COL_NAME", "REF_GENERATION")
        return mock {
            var c = 0
            whenever(it.moveToNext()) doAnswer { ++c == 1 }
            whenever(it.columnCount) doReturn n.size
            whenever(it.columnNames()) doReturn n
            whenever(it.columnIndex(any())) doAnswer { inv -> n.indexOf(inv.getArgument<String>(0)) }
            whenever(it.getString(any())) doAnswer { inv ->
                if (n.getOrNull(inv.getArgument<Int>(0)) == "TABLE_NAME") "test_table" else null
            }
            whenever(it.isClosed()) doReturn false
        }
    }

    @Suppress("Detekt.CognitiveComplexMethod")
    private fun makePragmaCursor(cols: List<List<Any?>>): ICursor {
        val n = arrayOf("cid", "name", "type", "notnull", "dflt_value", "pk")
        return mock {
            var c = 0
            whenever(it.moveToNext()) doAnswer { ++c <= cols.size }
            whenever(it.columnCount) doReturn n.size
            whenever(it.columnNames()) doReturn n
            whenever(it.columnIndex(any())) doAnswer { inv -> n.indexOf(inv.getArgument<String>(0)) }
            whenever(it.getString(any())) doAnswer { inv ->
                if (c in 1..cols.size) cols[c - 1].getOrNull(inv.getArgument<Int>(0))?.toString() else null
            }
            whenever(it.getInt(any())) doAnswer { inv ->
                if (c in 1..cols.size) cols[c - 1].getOrNull(inv.getArgument<Int>(0)) as? Int ?: 0 else 0
            }
            whenever(it.position()) doAnswer { c - 1 }
            whenever(it.type(any())) doAnswer { inv ->
                when (inv.getArgument<Int>(0)) {
                    0, 3, 5 -> ColumnType.INTEGER
                    4 -> ColumnType.NULL
                    else -> ColumnType.STRING
                }
            }
            whenever(it.isNull(any())) doAnswer { inv ->
                if (c in 1..cols.size) cols[c - 1].getOrNull(inv.getArgument<Int>(0)) == null else true
            }
            whenever(it.isClosed()) doReturn false
        }
    }

    private fun makeEmptyCursor(count: Int, names: Array<String>): ICursor = mock {
        whenever(it.moveToNext()) doReturn false
        whenever(it.columnCount) doReturn count
        whenever(it.columnNames()) doReturn names
        whenever(it.isClosed()) doReturn false
    }

    private val colsResultNames = arrayOf(
        "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME",
        "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE",
        "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH",
        "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG", "SCOPE_SCHEMA", "SCOPE_TABLE",
        "SOURCE_DATA_TYPE", "IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN")

    private val pkNames = arrayOf(
        "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "KEY_SEQ", "PK_NAME")

    @Suppress("Detekt.CognitiveComplexMethod", "Detekt.LongMethod")
    @Test
    fun getColumnsWithVariousColumnTypes() {
        val tc = makeTablesCursor()
        val pc = makePragmaCursor(listOf(
            listOf(0, "int_col", "INTEGER", 0, null, 0),
            listOf(1, "text_col", "TEXT", 0, null, 0),
            listOf(2, "real_col", "REAL", 0, null, 0),
            listOf(3, "blob_col", "BLOB", 0, null, 0),
            listOf(4, "numeric_col", "NUMERIC", 0, null, 0),
            listOf(5, "varchar_col", "VARCHAR", 0, null, 0),
            listOf(6, "smallint_col", "SMALLINT", 0, null, 0),
            listOf(7, "bigint_col", "BIGINT", 0, null, 0),
            listOf(8, "double_col", "DOUBLE", 0, null, 0),
            listOf(9, "float_col", "FLOAT", 0, null, 0),
            listOf(10, "decimal_col", "DECIMAL", 0, null, 0),
            listOf(11, "char_col", "CHAR", 0, null, 0),
            listOf(12, "clob_col", "CLOB", 0, null, 0),
            listOf(13, "null_col", "NULL", 0, null, 0),
            listOf(14, "unknown_col", "UNKNOWN", 0, null, 0),
            listOf(15, "mediumint_col", "MEDIUMINT", 0, null, 0),
            listOf(16, "doubleprecision_col", "DOUBLE PRECISION", 0, null, 0),
            listOf(17, "character_col", "CHARACTER", 0, null, 0)))
        val uc = makeEmptyCursor(24, colsResultNames)
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            val s = it.getArgument<String>(0)
            when {
                s.contains("sqlite_master") && s.contains("type IN ('table', 'view')") -> tc
                s.startsWith("PRAGMA table_info") -> pc
                s.contains("UNION ALL") -> uc
                else -> mockCursor
            }
        }
        metaData.getColumns(null, null, "%", "%").use { assertNotNull(it) }
    }

    @Suppress("Detekt.CognitiveComplexMethod", "Detekt.LongMethod")
    @Test
    fun getColumnsWithIntType() {
        val tc = makeTablesCursor()
        val pc = makePragmaCursor(listOf(listOf(0, "int_type_col", "INT", 1, "42", 1)))
        val uc = makeEmptyCursor(24, colsResultNames)
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            val s = it.getArgument<String>(0)
            when {
                s.contains("sqlite_master") && s.contains("type IN ('table', 'view')") -> tc
                s.startsWith("PRAGMA table_info") -> pc
                s.contains("UNION ALL") -> uc
                else -> mockCursor
            }
        }
        metaData.getColumns(null, null, "%", "%").use { assertNotNull(it) }
    }

    @Suppress("Detekt.CognitiveComplexMethod", "Detekt.LongMethod")
    @Test
    fun getColumnsWithColumnNamePatternFiltering() {
        val tc = makeTablesCursor()
        val pc = makePragmaCursor(listOf(
            listOf(0, "id", "INTEGER", 1, null, 1),
            listOf(1, "name", "TEXT", 0, null, 0),
            listOf(2, "email", "TEXT", 0, null, 0)))
        val uc = makeEmptyCursor(24, colsResultNames)
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            val s = it.getArgument<String>(0)
            when {
                s.contains("sqlite_master") && s.contains("type IN ('table', 'view')") -> tc
                s.startsWith("PRAGMA table_info") -> pc
                s.contains("UNION ALL") || s.contains("WHERE 1 = 0") -> uc
                else -> mockCursor
            }
        }
        metaData.getColumns(null, null, "%", "na%").use { assertNotNull(it) }
    }

    @Suppress("Detekt.CognitiveComplexMethod", "Detekt.LongMethod")
    @Test
    fun getColumnsWithNullColumnNamePattern() {
        val tc = makeTablesCursor()
        val pc = makePragmaCursor(listOf(
            listOf(0, "id", "INTEGER", 1, "0", 1),
            listOf(1, "name", "TEXT", 0, null, 0)))
        val uc = makeEmptyCursor(24, colsResultNames)
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            val s = it.getArgument<String>(0)
            when {
                s.contains("sqlite_master") && s.contains("type IN ('table', 'view')") -> tc
                s.startsWith("PRAGMA table_info") -> pc
                s.contains("UNION ALL") -> uc
                else -> mockCursor
            }
        }
        metaData.getColumns(null, null, "%", null).use { assertNotNull(it) }
    }

    @Test
    fun getColumnsWithEmptyResult() {
        val tc = makeEmptyCursor(10, arrayOf("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
            "TABLE_TYPE", "REMARKS", "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME",
            "SELF_REFERENCING_COL_NAME", "REF_GENERATION"))
        val ec = makeEmptyCursor(24, colsResultNames)
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            val s = it.getArgument<String>(0)
            when {
                s.contains("sqlite_master") && s.contains("type IN ('table', 'view')") -> tc
                s.contains("WHERE 1 = 0") -> ec
                else -> mockCursor
            }
        }
        metaData.getColumns(null, null, "%", "%").use { assertNotNull(it) }
    }

    @Suppress("Detekt.CognitiveComplexMethod", "Detekt.LongMethod")
    @Test
    fun getPrimaryKeysWithMultipleKeys() {
        val pc = makePragmaCursor(listOf(
            listOf(0, "id", "INTEGER", 1, null, 1),
            listOf(1, "sub_id", "INTEGER", 1, null, 2),
            listOf(2, "name", "TEXT", 0, null, 0)))
        val uc = makeEmptyCursor(6, pkNames)
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            val s = it.getArgument<String>(0)
            when {
                s.startsWith("PRAGMA table_info") -> pc
                s.contains("UNION ALL") -> uc
                else -> mockCursor
            }
        }
        metaData.getPrimaryKeys(null, null, "test_table").use { assertNotNull(it) }
    }

    @Suppress("Detekt.CognitiveComplexMethod", "Detekt.LongMethod")
    @Test
    fun getPrimaryKeysWithEmptyResult() {
        val pc = makePragmaCursor(listOf(
            listOf(0, "id", "INTEGER", 0, null, 0),
            listOf(1, "name", "TEXT", 0, null, 0)))
        val ec = makeEmptyCursor(6, pkNames)
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            val s = it.getArgument<String>(0)
            when {
                s.startsWith("PRAGMA table_info") -> pc
                s.contains("WHERE 1 = 0") -> ec
                else -> mockCursor
            }
        }
        metaData.getPrimaryKeys(null, null, "test_table").use { assertNotNull(it) }
    }

    @Test
    fun getTablesWithNullPattern() {
        val cursor = makeEmptyCursor(10, arrayOf("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
            "TABLE_TYPE", "REMARKS", "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME",
            "SELF_REFERENCING_COL_NAME", "REF_GENERATION"))
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            if (it.getArgument<String>(0).contains("sqlite_master")) { cursor } else { mockCursor }
        }
        metaData.getTables(null, null, null, null).use { assertNotNull(it) }
    }

    @Test
    fun getTablesWithSpecificPattern() {
        val cursor = makeEmptyCursor(10, arrayOf("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
            "TABLE_TYPE", "REMARKS", "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME",
            "SELF_REFERENCING_COL_NAME", "REF_GENERATION"))
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            val s = it.getArgument<String>(0)
            if (s.contains("sqlite_master") && s.contains("AND name GLOB")) { cursor } else { mockCursor }
        }
        metaData.getTables(null, null, "test_%", null).use { assertNotNull(it) }
    }

    @Test
    fun getIndexInfoWithUniqueFlag() {
        val cursor = makeEmptyCursor(13, arrayOf("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
            "NON_UNIQUE", "INDEX_QUALIFIER", "INDEX_NAME", "TYPE", "ORDINAL_POSITION",
            "COLUMN_NAME", "ASC_OR_DESC", "CARDINALITY", "PAGES", "FILTER_CONDITION"))
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doAnswer {
            if (it.getArgument<String>(0).contains("AND \"unique\" = 1")) { cursor } else { mockCursor }
        }
        metaData.getIndexInfo(null, null, "test_table", unique = true, approximate = false).use {
            assertNotNull(it)
        }
    }

    @Test
    fun getIndexInfoNonUnique() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())).doReturn(mockCursor)
        assertNotNull(metaData.getIndexInfo(null, null, "users", unique = false, approximate = false))
    }
}
