package com.bloomberg.selekt.android

import com.bloomberg.selekt.commons.loadEmbeddedLibrary

internal object NativeFixtures {
    init {
        loadEmbeddedLibrary(NativeFixtures::class.java.classLoader!!, "jni", "AndroidNativeFixturesLib")
        check(nativeInit() == 0)
    }

    fun load() = Unit

    private external fun nativeInit(): Int
}
