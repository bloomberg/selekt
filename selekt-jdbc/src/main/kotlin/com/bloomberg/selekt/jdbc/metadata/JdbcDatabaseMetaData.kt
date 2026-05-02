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

import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.result.JdbcResultSet
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.RowIdLifetime
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.Types
import javax.annotation.concurrent.NotThreadSafe

private fun escapeSql(value: String): String = value.replace("'", "''")

private fun escapeGlob(value: String): String = value.replace("[", "[[]")
    .replace("]", "[]]")
    .replace("?", "[?]")

@JvmSynthetic
internal fun String.toJdbcPatternRegex(): Regex = buildString {
    for (c in this@toJdbcPatternRegex) {
        when (c) {
            '%' -> append(".*")
            '_' -> append('.')
            '.', '\\', '(', ')', '[', ']', '{', '}', '^', '$', '|', '?', '*', '+' -> append('\\').append(c)
            else -> append(c)
        }
    }
}.toRegex()

@Suppress("Detekt.LargeClass")
@NotThreadSafe
internal class JdbcDatabaseMetaData(
    private val connection: JdbcConnection,
    private val database: SQLDatabase,
    private val connectionURL: ConnectionURL
) : DatabaseMetaData {
    override fun getConnection(): Connection = connection

    override fun getDatabaseProductName(): String = "SQLite"

    override fun getDatabaseProductVersion(): String = "3.51.2"

    override fun getDatabaseMajorVersion(): Int = 3

    override fun getDatabaseMinorVersion(): Int = 51

    override fun getDriverName(): String = "Selekt SQLite JDBC"

    override fun getDriverVersion(): String = "4.3"

    override fun getDriverMajorVersion(): Int = 4

    override fun getDriverMinorVersion(): Int = 3

    override fun getJDBCMajorVersion(): Int = 4

    override fun getJDBCMinorVersion(): Int = 3

    override fun getSQLKeywords(): String = "ABORT,AUTOINCREMENT,CONFLICT,FAIL,GLOB,IGNORE,ISNULL,NOTNULL,OFFSET," +
        "PRAGMA,RAISE,REPLACE,VACUUM"

    override fun getSearchStringEscape(): String = "\\"

    override fun getMaxCatalogNameLength(): Int = 0

    override fun getNumericFunctions(): String = "ABS,HEX,LENGTH,LOWER,LTRIM,MAX,MIN,NULLIF,QUOTE,RANDOM,REPLACE,ROUND," +
        "RTRIM,SUBSTR,TRIM,TYPEOF,UPPER"

    override fun getStringFunctions(): String = "LENGTH,LOWER,LTRIM,REPLACE,RTRIM,SUBSTR,TRIM,UPPER"

    override fun getSystemFunctions(): String = "COALESCE,IFNULL,LAST_INSERT_ROWID,NULLIF,SQLITE_VERSION"

    override fun getTimeDateFunctions(): String = "DATE,DATETIME,JULIANDAY,STRFTIME,TIME"

    override fun getIdentifierQuoteString(): String = "\""

    override fun getSQLStateType(): Int = DatabaseMetaData.sqlStateSQL99

    override fun getExtraNameCharacters(): String = ""

    override fun isCatalogAtStart(): Boolean = false

    override fun getCatalogSeparator(): String = "."

    override fun getCatalogTerm(): String = ""

    override fun getSchemaTerm(): String = ""

    override fun getProcedureTerm(): String = "table"

    override fun getMaxBinaryLiteralLength(): Int = 0

    override fun getMaxCharLiteralLength(): Int = 0

    override fun getMaxColumnNameLength(): Int = 0

    override fun getMaxColumnsInGroupBy(): Int = 0

    override fun getMaxColumnsInIndex(): Int = 0

    override fun getMaxColumnsInOrderBy(): Int = 0

    override fun getMaxColumnsInSelect(): Int = 0

    override fun getMaxColumnsInTable(): Int = 0

    override fun getMaxConnections(): Int = 0

    override fun getMaxCursorNameLength(): Int = 0

    override fun getMaxIndexLength(): Int = 0

    override fun getMaxProcedureNameLength(): Int = 0

    override fun getMaxRowSize(): Int = 0

    override fun getMaxSchemaNameLength(): Int = 0

    override fun getMaxStatementLength(): Int = 0

    override fun getMaxStatements(): Int = 0

    override fun getMaxTableNameLength(): Int = 0

    override fun getMaxTablesInSelect(): Int = 0

    override fun getMaxUserNameLength(): Int = 0

    override fun supportsAlterTableWithAddColumn(): Boolean = true

    override fun supportsAlterTableWithDropColumn(): Boolean = true

    override fun supportsANSI92EntryLevelSQL(): Boolean = true

    override fun supportsANSI92FullSQL(): Boolean = false

    override fun supportsANSI92IntermediateSQL(): Boolean = false

    override fun supportsBatchUpdates(): Boolean = true

    override fun supportsCatalogsInDataManipulation(): Boolean = false

    override fun supportsCatalogsInIndexDefinitions(): Boolean = false

    override fun supportsCatalogsInPrivilegeDefinitions(): Boolean = false

    override fun supportsCatalogsInProcedureCalls(): Boolean = false

    override fun supportsCatalogsInTableDefinitions(): Boolean = false

    override fun supportsColumnAliasing(): Boolean = false

    override fun supportsConvert(): Boolean = true

    override fun supportsConvert(fromType: Int, toType: Int): Boolean = true

    override fun supportsCorrelatedSubqueries(): Boolean = true

    @Suppress("FunctionMaxLength")
    override fun supportsDataDefinitionAndDataManipulationTransactions(): Boolean = true

    override fun supportsDataManipulationTransactionsOnly(): Boolean = false
    override fun supportsDifferentTableCorrelationNames(): Boolean = true

    override fun supportsExpressionsInOrderBy(): Boolean = true

    override fun supportsExtendedSQLGrammar(): Boolean = false

    override fun supportsFullOuterJoins(): Boolean = true

    override fun supportsGroupBy(): Boolean = true

    override fun supportsGroupByBeyondSelect(): Boolean = true

    override fun supportsGroupByUnrelated(): Boolean = true

    override fun supportsIntegrityEnhancementFacility(): Boolean = false

    override fun supportsLikeEscapeClause(): Boolean = true

    override fun supportsLimitedOuterJoins(): Boolean = true

    override fun supportsMinimumSQLGrammar(): Boolean = true

    override fun supportsMixedCaseIdentifiers(): Boolean = false

    override fun supportsMixedCaseQuotedIdentifiers(): Boolean = true

    override fun supportsMultipleOpenResults(): Boolean = false

    override fun supportsMultipleResultSets(): Boolean = false

    override fun supportsMultipleTransactions(): Boolean = false

    override fun supportsNonNullableColumns(): Boolean = true

    override fun supportsOpenCursorsAcrossCommit(): Boolean = false

    override fun supportsOpenCursorsAcrossRollback(): Boolean = false

    override fun supportsOpenStatementsAcrossCommit(): Boolean = false

    override fun supportsOpenStatementsAcrossRollback(): Boolean = false

    override fun supportsOrderByUnrelated(): Boolean = true

    override fun supportsOuterJoins(): Boolean = true

    override fun supportsPositionedDelete(): Boolean = false

    override fun supportsPositionedUpdate(): Boolean = false

    override fun supportsSelectForUpdate(): Boolean = false

    override fun supportsStoredProcedures(): Boolean = false

    override fun supportsSubqueriesInComparisons(): Boolean = true

    override fun supportsSubqueriesInExists(): Boolean = true

    override fun supportsSubqueriesInIns(): Boolean = true

    override fun supportsSubqueriesInQuantifieds(): Boolean = true

    override fun supportsTableCorrelationNames(): Boolean = false

    override fun supportsTransactions(): Boolean = true

    override fun supportsUnion(): Boolean = true

    override fun supportsUnionAll(): Boolean = true

    override fun getDefaultTransactionIsolation(): Int = Connection.TRANSACTION_SERIALIZABLE

    override fun supportsTransactionIsolationLevel(level: Int): Boolean = Connection.TRANSACTION_SERIALIZABLE == level

    override fun supportsGetGeneratedKeys(): Boolean = true

    override fun supportsResultSetType(type: Int): Boolean = when (type) {
        ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE -> true
        else -> false
    }

    override fun supportsResultSetConcurrency(
        type: Int,
        concurrency: Int
    ): Boolean = ResultSet.CONCUR_READ_ONLY == concurrency

    override fun supportsResultSetHoldability(holdability: Int): Boolean = ResultSet.CLOSE_CURSORS_AT_COMMIT == holdability

    override fun storesLowerCaseIdentifiers(): Boolean = false

    override fun storesLowerCaseQuotedIdentifiers(): Boolean = false

    override fun storesMixedCaseIdentifiers(): Boolean = true

    override fun storesMixedCaseQuotedIdentifiers(): Boolean = true

    override fun storesUpperCaseIdentifiers(): Boolean = false

    override fun storesUpperCaseQuotedIdentifiers(): Boolean = false

    override fun nullsAreSortedAtEnd(): Boolean = false

    override fun nullsAreSortedAtStart(): Boolean = true

    override fun nullsAreSortedHigh(): Boolean = false

    override fun nullsAreSortedLow(): Boolean = false

    override fun allProceduresAreCallable(): Boolean = false

    override fun allTablesAreSelectable(): Boolean = true

    override fun getURL(): String = connectionURL.toString()

    override fun getUserName(): String = ""

    override fun isReadOnly(): Boolean = connection.isReadOnly

    override fun usesLocalFiles(): Boolean = true

    override fun usesLocalFilePerTable(): Boolean = true

    override fun doesMaxRowSizeIncludeBlobs(): Boolean = true

    override fun locatorsUpdateCopy(): Boolean = false

    private fun executeMetadataQuery(sql: String): ResultSet = JdbcResultSet(
        database.query(sql, emptyArray()),
        null
    )

    @Suppress("Detekt.StringLiteralDuplication")
    private fun mapSQLiteTypeToJDBCType(sqliteType: String): Int = when (sqliteType.uppercase()) {
        "INTEGER", "INT", "SMALLINT", "MEDIUMINT", "BIGINT" -> Types.INTEGER
        "REAL", "DOUBLE", "DOUBLE PRECISION", "FLOAT" -> Types.REAL
        "NUMERIC", "DECIMAL" -> Types.NUMERIC
        "TEXT", "CLOB", "VARCHAR", "CHAR", "CHARACTER" -> Types.VARCHAR
        "BLOB" -> Types.BLOB
        "NULL" -> Types.NULL
        else -> Types.VARCHAR
    }

    @Suppress("Detekt.StringLiteralDuplication")
    private fun mapSQLiteTypeToJDBCTypeName(sqliteType: String): String = when (sqliteType.uppercase()) {
        "INTEGER", "INT", "SMALLINT", "MEDIUMINT", "BIGINT" -> "INTEGER"
        "REAL", "DOUBLE", "DOUBLE PRECISION", "FLOAT" -> "REAL"
        "NUMERIC", "DECIMAL" -> "NUMERIC"
        "TEXT", "CLOB" -> "TEXT"
        "VARCHAR", "CHAR", "CHARACTER" -> sqliteType.uppercase()
        "BLOB" -> "BLOB"
        "NULL" -> "NULL"
        else -> sqliteType.uppercase()
    }

    @Suppress("Detekt.MagicNumber")
    private fun getColumnSizeForType(sqliteType: String): Int = when (sqliteType.uppercase()) {
        "INTEGER", "INT" -> 10
        "SMALLINT" -> 5
        "MEDIUMINT" -> 7
        "BIGINT" -> 19
        "REAL", "FLOAT" -> 15
        "DOUBLE", "DOUBLE PRECISION" -> 15
        "NUMERIC", "DECIMAL" -> 10
        "TEXT", "CLOB", "VARCHAR" -> Int.MAX_VALUE
        "CHAR", "CHARACTER" -> 1
        "BLOB" -> Int.MAX_VALUE
        else -> 255
    }

    override fun getTables(
        catalog: String?,
        schemaPattern: String?,
        tableNamePattern: String?,
        types: Array<out String>?
    ): ResultSet {
        val namePattern = tableNamePattern?.let { escapeGlob(it).replace("%", "*") } ?: "*"
        val whereClause = if (tableNamePattern != null) {
            "AND name GLOB '${escapeSql(namePattern)}'"
        } else {
            ""
        }
        val sql = """
            SELECT
                NULL as TABLE_CAT,
                NULL as TABLE_SCHEM,
                name as TABLE_NAME,
                CASE
                    WHEN type = 'table' THEN 'TABLE'
                    WHEN type = 'view' THEN 'VIEW'
                    ELSE UPPER(type)
                END as TABLE_TYPE,
                '' as REMARKS,
                NULL as TYPE_CAT,
                NULL as TYPE_SCHEM,
                NULL as TYPE_NAME,
                NULL as SELF_REFERENCING_COL_NAME,
                NULL as REF_GENERATION
            FROM sqlite_master
            WHERE type IN ('table', 'view') $whereClause
            AND name NOT LIKE 'sqlite_%'
            ORDER BY TABLE_TYPE, TABLE_NAME
        """.trimIndent()
        return executeMetadataQuery(sql)
    }

    @Suppress("Detekt.CognitiveComplexMethod", "Detekt.LongMethod", "Detekt.NestedBlockDepth")
    override fun getColumns(
        catalog: String?,
        schemaPattern: String?,
        tableNamePattern: String?,
        columnNamePattern: String?
    ): ResultSet {
        val tablesResult = getTables(catalog, schemaPattern, tableNamePattern, arrayOf("TABLE", "VIEW"))
        val columnRows = mutableListOf<String>()
        tablesResult.use { tablesResult ->
            while (tablesResult.next()) {
                val tableName = tablesResult.getString("TABLE_NAME")
                val pragmaSql = "PRAGMA table_info('${escapeSql(tableName)}')"
                val pragmaResult = executeMetadataQuery(pragmaSql)
                pragmaResult.use { pragmaResult ->
                    while (pragmaResult.next()) {
                        val columnName = pragmaResult.getString("name")
                        val dataType = pragmaResult.getString("type")
                        val notNull = pragmaResult.getInt("notnull")
                        val defaultValue = pragmaResult.getString("dflt_value")
                        val primaryKey = pragmaResult.getInt("pk")
                        if (columnNamePattern != null && !columnName.matches(
                            columnNamePattern.toJdbcPatternRegex()
                            )
                        ) {
                            continue
                        }
                        val sqlType = mapSQLiteTypeToJDBCType(dataType)
                        val typeName = mapSQLiteTypeToJDBCTypeName(dataType)
                        val columnSize = getColumnSizeForType(dataType)
                        columnRows.add("""
                            SELECT
                                NULL as TABLE_CAT,
                                NULL as TABLE_SCHEM,
                                '${escapeSql(tableName)}' as TABLE_NAME,
                                '${escapeSql(columnName)}' as COLUMN_NAME,
                                $sqlType as DATA_TYPE,
                                '$typeName' as TYPE_NAME,
                                $columnSize as COLUMN_SIZE,
                                NULL as BUFFER_LENGTH,
                                NULL as DECIMAL_DIGITS,
                                10 as NUM_PREC_RADIX,
                                ${if (notNull == 1) { "0" } else { "1" } } as NULLABLE,
                                '' as REMARKS,
                                ${if (defaultValue != null) {
                                    "'${escapeSql(defaultValue)}'"
                                } else {
                                    "NULL"
                                } } as COLUMN_DEF,
                                NULL as SQL_DATA_TYPE,
                                NULL as SQL_DATETIME_SUB,
                                NULL as CHAR_OCTET_LENGTH,
                                ${pragmaResult.row + 1} as ORDINAL_POSITION,
                                '${if (notNull == 1) { "NO" } else { "YES" } }' as IS_NULLABLE,
                                NULL as SCOPE_CATALOG,
                                NULL as SCOPE_SCHEMA,
                                NULL as SCOPE_TABLE,
                                NULL as SOURCE_DATA_TYPE,
                                '${if (primaryKey > 0) { "YES" } else { "NO" } }' as IS_AUTOINCREMENT,
                                '${if (primaryKey > 0) { "YES" } else { "NO" } }' as IS_GENERATEDCOLUMN
                        """.trimIndent())
                    }
                }
            }
        }
        val unionSql = if (columnRows.isNotEmpty()) {
            columnRows.joinToString("\nUNION ALL\n") + "\nORDER BY TABLE_NAME, ORDINAL_POSITION"
        } else {
            """
                SELECT
                    NULL as TABLE_CAT, NULL as TABLE_SCHEM, NULL as TABLE_NAME,
                    NULL as COLUMN_NAME, NULL as DATA_TYPE, NULL as TYPE_NAME,
                    NULL as COLUMN_SIZE, NULL as BUFFER_LENGTH, NULL as DECIMAL_DIGITS,
                    NULL as NUM_PREC_RADIX, NULL as NULLABLE, NULL as REMARKS,
                    NULL as COLUMN_DEF, NULL as SQL_DATA_TYPE, NULL as SQL_DATETIME_SUB,
                    NULL as CHAR_OCTET_LENGTH, NULL as ORDINAL_POSITION, NULL as IS_NULLABLE,
                    NULL as SCOPE_CATALOG, NULL as SCOPE_SCHEMA, NULL as SCOPE_TABLE,
                    NULL as SOURCE_DATA_TYPE, NULL as IS_AUTOINCREMENT, NULL as IS_GENERATEDCOLUMN
                WHERE 1 = 0
            """.trimIndent()
        }
        return executeMetadataQuery(unionSql)
    }

    override fun getPrimaryKeys(catalog: String?, schema: String?, table: String): ResultSet {
        val sql = "PRAGMA table_info('${escapeSql(table)}')"
        val pragmaResult = executeMetadataQuery(sql)
        val pkRows = mutableListOf<String>()
        pragmaResult.use { pragmaResult ->
            while (pragmaResult.next()) {
                val primaryKey = pragmaResult.getInt("pk")
                if (primaryKey > 0) {
                    pkRows.add("""
                        SELECT
                            NULL as TABLE_CAT,
                            NULL as TABLE_SCHEM,
                            '${escapeSql(table)}' as TABLE_NAME,
                            '${escapeSql(pragmaResult.getString("name"))}' as COLUMN_NAME,
                            $primaryKey as KEY_SEQ,
                            'PRIMARY' as PK_NAME
                    """.trimIndent())
                }
            }
        }
        val unionSql = if (pkRows.isNotEmpty()) {
            pkRows.joinToString("\nUNION ALL\n") + "\nORDER BY KEY_SEQ"
        } else {
            """
                SELECT
                    NULL as TABLE_CAT, NULL as TABLE_SCHEM, NULL as TABLE_NAME,
                    NULL as COLUMN_NAME, NULL as KEY_SEQ, NULL as PK_NAME
                WHERE 1 = 0
            """.trimIndent()
        }
        return executeMetadataQuery(unionSql)
    }

    override fun getIndexInfo(
        catalog: String?,
        schema: String?,
        table: String,
        unique: Boolean,
        approximate: Boolean
    ): ResultSet = executeMetadataQuery("""
        SELECT
            NULL as TABLE_CAT,
            NULL as TABLE_SCHEM,
            '${escapeSql(table)}' as TABLE_NAME,
            CASE WHEN il."unique" = 1 THEN 0 ELSE 1 END as NON_UNIQUE,
            NULL as INDEX_QUALIFIER,
            il.name as INDEX_NAME,
            3 as TYPE,
            0 as ORDINAL_POSITION,
            NULL as COLUMN_NAME,
            NULL as ASC_OR_DESC,
            0 as CARDINALITY,
            0 as PAGES,
            NULL as FILTER_CONDITION
        FROM pragma_index_list('${escapeSql(table)}') as il
        ${if (unique) "WHERE il.\"unique\" = 1" else ""}
    """.trimIndent())

    override fun getProcedures(
        catalog: String?,
        schemaPattern: String?,
        procedureNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("getProcedures not implemented")

    override fun getProcedureColumns(
        catalog: String?,
        schemaPattern: String?,
        procedureNamePattern: String?,
        columnNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("getProcedureColumns not implemented")

    override fun getSchemas(): ResultSet = getSchemas(null, null)
    override fun getSchemas(
        catalog: String?,
        schemaPattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("getSchemas not implemented")

    override fun getCatalogs(): ResultSet = throw SQLFeatureNotSupportedException("getCatalogs not implemented")

    override fun getTableTypes(): ResultSet = executeMetadataQuery("""
        SELECT 'TABLE' as TABLE_TYPE
        UNION ALL
        SELECT 'VIEW' as TABLE_TYPE
        ORDER BY TABLE_TYPE
    """.trimIndent())

    @Suppress("Detekt.LongMethod")
    override fun getTypeInfo(): ResultSet = executeMetadataQuery("""
        SELECT
            'INTEGER' as TYPE_NAME,
            ${Types.INTEGER} as DATA_TYPE,
            10 as PRECISION,
            NULL as LITERAL_PREFIX,
            NULL as LITERAL_SUFFIX,
            NULL as CREATE_PARAMS,
            1 as NULLABLE,
            1 as CASE_SENSITIVE,
            2 as SEARCHABLE,
            0 as UNSIGNED_ATTRIBUTE,
            0 as FIXED_PREC_SCALE,
            0 as AUTO_INCREMENT,
            'INTEGER' as LOCAL_TYPE_NAME,
            0 as MINIMUM_SCALE,
            0 as MAXIMUM_SCALE,
            NULL as SQL_DATA_TYPE,
            NULL as SQL_DATETIME_SUB,
            10 as NUM_PREC_RADIX
        UNION ALL
        SELECT
            'REAL' as TYPE_NAME,
            ${Types.REAL} as DATA_TYPE,
            15 as PRECISION,
            NULL as LITERAL_PREFIX,
            NULL as LITERAL_SUFFIX,
            NULL as CREATE_PARAMS,
            1 as NULLABLE,
            0 as CASE_SENSITIVE,
            2 as SEARCHABLE,
            0 as UNSIGNED_ATTRIBUTE,
            0 as FIXED_PREC_SCALE,
            0 as AUTO_INCREMENT,
            'REAL' as LOCAL_TYPE_NAME,
            0 as MINIMUM_SCALE,
            15 as MAXIMUM_SCALE,
            NULL as SQL_DATA_TYPE,
            NULL as SQL_DATETIME_SUB,
            10 as NUM_PREC_RADIX
        UNION ALL
        SELECT
            'TEXT' as TYPE_NAME,
            ${Types.VARCHAR} as DATA_TYPE,
            ${Int.MAX_VALUE} as PRECISION,
            '''' as LITERAL_PREFIX,
            '''' as LITERAL_SUFFIX,
            NULL as CREATE_PARAMS,
            1 as NULLABLE,
            1 as CASE_SENSITIVE,
            3 as SEARCHABLE,
            0 as UNSIGNED_ATTRIBUTE,
            0 as FIXED_PREC_SCALE,
            0 as AUTO_INCREMENT,
            'TEXT' as LOCAL_TYPE_NAME,
            0 as MINIMUM_SCALE,
            0 as MAXIMUM_SCALE,
            NULL as SQL_DATA_TYPE,
            NULL as SQL_DATETIME_SUB,
            NULL as NUM_PREC_RADIX
        UNION ALL
        SELECT
            'BLOB' as TYPE_NAME,
            ${Types.BLOB} as DATA_TYPE,
            ${Int.MAX_VALUE} as PRECISION,
            NULL as LITERAL_PREFIX,
            NULL as LITERAL_SUFFIX,
            NULL as CREATE_PARAMS,
            1 as NULLABLE,
            0 as CASE_SENSITIVE,
            0 as SEARCHABLE,
            0 as UNSIGNED_ATTRIBUTE,
            0 as FIXED_PREC_SCALE,
            0 as AUTO_INCREMENT,
            'BLOB' as LOCAL_TYPE_NAME,
            0 as MINIMUM_SCALE,
            0 as MAXIMUM_SCALE,
            NULL as SQL_DATA_TYPE,
            NULL as SQL_DATETIME_SUB,
            NULL as NUM_PREC_RADIX
        UNION ALL
        SELECT
            'NUMERIC' as TYPE_NAME,
            ${Types.NUMERIC} as DATA_TYPE,
            10 as PRECISION,
            NULL as LITERAL_PREFIX,
            NULL as LITERAL_SUFFIX,
            'precision,scale' as CREATE_PARAMS,
            1 as NULLABLE,
            0 as CASE_SENSITIVE,
            2 as SEARCHABLE,
            0 as UNSIGNED_ATTRIBUTE,
            0 as FIXED_PREC_SCALE,
            0 as AUTO_INCREMENT,
            'NUMERIC' as LOCAL_TYPE_NAME,
            0 as MINIMUM_SCALE,
            10 as MAXIMUM_SCALE,
            NULL as SQL_DATA_TYPE,
            NULL as SQL_DATETIME_SUB,
            10 as NUM_PREC_RADIX
        ORDER BY DATA_TYPE
    """.trimIndent())

    override fun getUDTs(
        catalog: String?,
        schemaPattern: String?,
        typeNamePattern: String?,
        types: IntArray?
    ): ResultSet = throw SQLFeatureNotSupportedException("getUDTs not implemented")

    override fun getSuperTypes(
        catalog: String?,
        schemaPattern: String?,
        typeNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("getSuperTypes not implemented")

    override fun getSuperTables(
        catalog: String?,
        schemaPattern: String?,
        tableNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("getSuperTables not implemented")

    override fun getAttributes(
        catalog: String?,
        schemaPattern: String?,
        typeNamePattern: String?,
        attributeNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("getAttributes not implemented")

    override fun getFunctions(
        catalog: String?,
        schemaPattern: String?,
        functionNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("getFunctions not implemented")

    override fun getFunctionColumns(
        catalog: String?,
        schemaPattern: String?,
        functionNamePattern: String?,
        columnNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("getFunctionColumns not implemented")

    override fun getPseudoColumns(
        catalog: String?,
        schemaPattern: String?,
        tableNamePattern: String?,
        columnNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("getPseudoColumns not implemented")

    override fun generatedKeyAlwaysReturned(): Boolean = false

    override fun dataDefinitionCausesTransactionCommit(): Boolean = false

    override fun dataDefinitionIgnoredInTransactions(): Boolean = false

    override fun deletesAreDetected(type: Int): Boolean = false

    override fun insertsAreDetected(type: Int): Boolean = false

    override fun updatesAreDetected(type: Int): Boolean = false

    override fun othersDeletesAreVisible(type: Int): Boolean = false

    override fun othersInsertsAreVisible(type: Int): Boolean = false

    override fun othersUpdatesAreVisible(type: Int): Boolean = false

    override fun ownDeletesAreVisible(type: Int): Boolean = false

    override fun ownInsertsAreVisible(type: Int): Boolean = false

    override fun ownUpdatesAreVisible(type: Int): Boolean = false

    override fun supportsNamedParameters(): Boolean = false

    override fun supportsStatementPooling(): Boolean = false

    override fun supportsSavepoints(): Boolean = true

    override fun supportsStoredFunctionsUsingCallSyntax(): Boolean = false

    override fun supportsCoreSQLGrammar(): Boolean = true

    override fun supportsSchemasInDataManipulation(): Boolean = false

    override fun supportsSchemasInProcedureCalls(): Boolean = false

    override fun supportsSchemasInTableDefinitions(): Boolean = false

    override fun supportsSchemasInIndexDefinitions(): Boolean = false

    override fun supportsSchemasInPrivilegeDefinitions(): Boolean = false

    override fun nullPlusNonNullIsNull(): Boolean = true

    override fun autoCommitFailureClosesAllResultSets(): Boolean = false

    override fun getRowIdLifetime(): RowIdLifetime = RowIdLifetime.ROWID_UNSUPPORTED

    override fun getResultSetHoldability(): Int = ResultSet.HOLD_CURSORS_OVER_COMMIT

    override fun getColumnPrivileges(
        catalog: String?,
        schema: String?,
        table: String?,
        columnNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("SQLite does not support column privileges")

    override fun getTablePrivileges(
        catalog: String?,
        schemaPattern: String?,
        tableNamePattern: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("SQLite does not support table privileges")

    override fun getBestRowIdentifier(
        catalog: String?,
        schema: String?,
        table: String?,
        scope: Int,
        nullable: Boolean
    ): ResultSet = throw SQLFeatureNotSupportedException("SQLite does not support best row identifier metadata")

    override fun getVersionColumns(
        catalog: String?,
        schema: String?,
        table: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("SQLite does not support version columns")

    override fun getImportedKeys(
        catalog: String?,
        schema: String?,
        table: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("Use PRAGMA foreign_key_list to get foreign key information")

    override fun getExportedKeys(
        catalog: String?,
        schema: String?,
        table: String?
    ): ResultSet {
        throw SQLFeatureNotSupportedException("SQLite does not support exported key metadata")
    }

    override fun getCrossReference(
        parentCatalog: String?,
        parentSchema: String?,
        parentTable: String?,
        foreignCatalog: String?,
        foreignSchema: String?,
        foreignTable: String?
    ): ResultSet = throw SQLFeatureNotSupportedException("SQLite does not support cross reference metadata")

    override fun getClientInfoProperties(): ResultSet = throw SQLFeatureNotSupportedException(
        "SQLite does not support client info properties"
    )

    override fun <T> unwrap(iface: Class<T>): T = if (iface.isAssignableFrom(this::class.java)) {
        @Suppress("UNCHECKED_CAST")
        this as T
    } else if (iface.isAssignableFrom(SQLDatabase::class.java)) {
        @Suppress("UNCHECKED_CAST")
        database as T
    } else {
        throw SQLException("Cannot unwrap to ${iface.name}")
    }

    override fun isWrapperFor(
        iface: Class<*>
    ): Boolean = iface.isAssignableFrom(this::class.java) || iface.isAssignableFrom(SQLDatabase::class.java)
}
