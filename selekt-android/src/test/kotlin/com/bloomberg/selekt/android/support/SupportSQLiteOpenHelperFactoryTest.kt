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

package com.bloomberg.selekt.android.support

import android.content.Context
import android.database.sqlite.SQLiteDatabaseCorruptException
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.jupiter.SelektTestExtension
import com.bloomberg.selekt.android.SQLiteDatabase
import com.bloomberg.selekt.commons.deleteDatabase
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private fun <T : RoomDatabase> buildRoomDatabase(
    context: Context,
    klass: Class<T>
) = Room.databaseBuilder(context, klass, "app")
    .openHelperFactory(createSupportSQLiteOpenHelperFactory(SQLiteJournalMode.WAL, ByteArray(32) { 0x42 }))
    .allowMainThreadQueries()
    .build()

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: User)
}

@ExtendWith(SelektTestExtension::class)
internal class SupportSQLiteOpenHelperFactoryTest {
    private val file = createTempFile("test-room", ".db").toFile()
    private val context = mock<Context>().apply {
        whenever(getDatabasePath(any())) doReturn file
    }
    private val database = buildRoomDatabase(
        context,
        AppDatabase::class.java
    )

    @AfterEach
    fun tearDown() {
        try {
            database.close()
        } finally {
            deleteDatabase(file)
        }
    }

    @Test
    fun insert() {
        val user = User(42, "Michael", "Bloomberg")
        database.userDao().run {
            insertAll(user)
            assertEquals(user, findByName("Michael", "Bloomberg"))
        }
    }

    @Test
    fun encryption() {
        database.userDao().insertAll(User(42, "Michael", "Bloomberg"))
        SQLiteDatabase.openOrCreateDatabase(file, SQLiteJournalMode.WAL.databaseConfiguration, null).use {
            assertFailsWith<SQLiteDatabaseCorruptException> {
                it.journalMode
            }
        }
    }

    @Test
    fun journalMode() {
        database.userDao().insertAll(User(42, "Michael", "Bloomberg"))
        SQLiteDatabase.openOrCreateDatabase(file, SQLiteJournalMode.WAL.databaseConfiguration, ByteArray(32) { 0x42 }).use {
            assertEquals(SQLiteJournalMode.WAL, it.journalMode)
        }
    }
}
