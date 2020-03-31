package com.bloomberg.selekt.android

import com.bloomberg.commons.loadEmbeddedLibrary
import com.bloomberg.selekt.externalSQLiteSingleton

private const val CAN_USE_EMBEDDED_PROPERTY_KEY = "com.bloomberg.selekt.lib.can_use_embedded"

internal val sqlite = externalSQLiteSingleton {
    "selekt".let {
        try {
            System.loadLibrary(it)
        } catch (e: UnsatisfiedLinkError) {
            if (System.getProperty(CAN_USE_EMBEDDED_PROPERTY_KEY, null) == "true") {
                loadEmbeddedLibrary(checkNotNull(SQLite::class.java.classLoader), "jni", it)
            }
        }
    }
}
