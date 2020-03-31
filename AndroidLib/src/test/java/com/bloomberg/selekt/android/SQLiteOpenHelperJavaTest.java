package com.bloomberg.selekt.android;

import android.content.Context;

import com.bloomberg.selekt.SQLiteJournalMode;
import com.bloomberg.selekt.SQLiteTraceEventMode;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.bloomberg.commons.DatabaseKt.deleteDatabase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLiteOpenHelperJavaTest {
    @Rule
    public final TestRule timeoutRule = new DisableOnDebug(new Timeout(10L, TimeUnit.SECONDS));

    private final File file = File.createTempFile("test-java-open-helper", ".db");
    private final byte[] key = new byte[] {
        0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
        0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
        0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
        0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42
    };

    private final Context targetContext = mock(Context.class);
    private SQLiteOpenHelper databaseHelper = null;

    public SQLiteOpenHelperJavaTest() throws IOException {}

    @Before
    public void setUp() {
        file.deleteOnExit();
        when(targetContext.getDatabasePath(anyString())).thenReturn(file);
    }

    @After
    public void tearDown() {
        final SQLiteOpenHelper helper = databaseHelper;
        if (helper != null) {
            final SQLiteDatabase database = helper.getWritableDatabase();
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
        final ISQLiteOpenHelper.Callback callback = mock(ISQLiteOpenHelper.Callback.class);
        databaseHelper = new SQLiteOpenHelper(
            targetContext,
            new ISQLiteOpenHelper.Configuration(
                callback,
                key,
                file.getName()
            ),
            3,
            new SQLiteOpenParams()
        );
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        assertEquals(3, database.getVersion());
    }

    @Test
    public void createWithLoggingEnabled() {
        final ISQLiteOpenHelper.Callback callback = mock(ISQLiteOpenHelper.Callback.class);
        databaseHelper = new SQLiteOpenHelper(
            targetContext,
            new ISQLiteOpenHelper.Configuration(
                callback,
                key,
                file.getName()
            ),
            3,
            new SQLiteOpenParams.Builder()
                .setJournalMode(SQLiteJournalMode.WAL)
                .setPageSizeExponent(12)
                .setTraceEventMode(new SQLiteTraceEventMode().enableStatement())
                .build()
        );
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        assertEquals(SQLiteJournalMode.WAL, database.getJournalMode());
    }
}
