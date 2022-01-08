package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SQLiteThreadingModeTest {
    @Test
    fun singleThread() {
        assertEquals("0", SQLiteThreadingMode.SINGLETHREAD.toString())
    }

    @Test
    fun serialized() {
        assertEquals("1", SQLiteThreadingMode.SERIALIZED.toString())
    }

    @Test
    fun multiThread() {
        assertEquals("2", SQLiteThreadingMode.MULTITHREAD.toString())
    }
}
