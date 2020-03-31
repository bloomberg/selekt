package com.bloomberg.selekt.android;

import android.content.Context;

import com.bloomberg.selekt.SQLiteJournalMode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.bloomberg.commons.DatabaseKt.deleteDatabase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLiteDatabaseJavaTest {
    private final Context targetContext = mock(Context.class);
    private SQLiteDatabase database = null;

    private final File file = File.createTempFile("test-java-database", ".db");
    private final byte[] key = new byte[] {
        0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
        0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
        0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
        0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42
    };

    public SQLiteDatabaseJavaTest() throws IOException {}

    @Before
    public void setUp() {
        file.deleteOnExit();
        when(targetContext.getDatabasePath(anyString())).thenReturn(file);
    }

    @After
    public void tearDown() {
        final SQLiteDatabase database = this.database;
        if (database != null) {
            try {
                if (database.isOpen()) {
                    database.close();
                }
                assertFalse(database.isOpen());
            } finally {
                assertTrue(deleteDatabase(file));
            }
        }
    }

    @Test
    public void creation() {
        database = SQLiteDatabase.openOrCreateDatabase(
            targetContext.getDatabasePath("sample"),
            SQLiteJournalMode.WAL.getDatabaseConfiguration(),
            key
        );
        database.exec("PRAGMA journal_mode=WAL");
        assertEquals(SQLiteJournalMode.WAL, database.getJournalMode());
    }
}
