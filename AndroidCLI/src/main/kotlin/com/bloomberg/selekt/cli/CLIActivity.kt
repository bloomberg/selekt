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

package com.bloomberg.selekt.cli

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bloomberg.selekt.annotations.Experimental
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SQLiteTraceEventMode
import com.bloomberg.selekt.android.SQLiteDatabase
import com.bloomberg.selekt.android.Selekt
import com.bloomberg.selekt.cli.databinding.ActivityMainBinding
import java.lang.StringBuilder

class CLIActivity : AppCompatActivity() {
    private lateinit var database: SQLiteDatabase
    private lateinit var binding: ActivityMainBinding

    private val keyListener = View.OnKeyListener { _, keyCode, keyEvent ->
        if (KeyEvent.KEYCODE_ENTER == keyCode) {
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                binding.input.text.apply {
                    takeUnless { it.isBlank() }?.toString()?.let {
                        runCatching { executeSQL(it) }.exceptionOrNull()?.let { e -> log(e) }
                    }
                    clear()
                }
            }
            true
        } else {
            false
        }
    }

    @Suppress("Detekt.MagicNumber")
    @OptIn(Experimental::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Selekt.registerComponentCallbackWith(application)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = SQLiteDatabase.openOrCreateDatabase(
            getDatabasePath("foo.db"),
            SQLiteJournalMode.WAL.databaseConfiguration.copy(
                trace = SQLiteTraceEventMode().enableClose()
                    .enableStatement()
            ),
            ByteArray(32) { 0x42 }
        ).apply {
            exec("PRAGMA journal_mode=${SQLiteJournalMode.WAL}")
        }
        checkNotNull(supportActionBar).hide()
        binding.console.movementMethod = ScrollingMovementMethod()
        binding.input.setOnKeyListener(this@CLIActivity.keyListener)
    }

    override fun onResume() {
        super.onResume()
        binding.input.requestFocus()
    }

    @OptIn(Experimental::class)
    override fun onDestroy() {
        super.onDestroy()
        Selekt.unregisterComponentCallbackFrom(application)
        database.close()
    }

    private fun log(any: Any?) = binding.console.append("\n$any")

    private fun executeSQL(sql: String) {
        log("> $sql")
        if (sql.startsWith("SELECT", ignoreCase = true) || sql.startsWith("PRAGMA", ignoreCase = true)) {
            query(sql)
        } else {
            database.exec(sql)
        }
    }

    private fun query(sql: String): Unit = database.query(sql, emptyArray()).use {
        if (it.columnCount < 1) {
            return
        }
        val builder = StringBuilder()
        while (it.moveToNext()) {
            builder.append(it.getString(0))
            for (i in 1 until it.columnCount) {
                builder.append('|')
                    .append(it.getString(i))
            }
            log(builder)
            builder.clear()
        }
    }
}
