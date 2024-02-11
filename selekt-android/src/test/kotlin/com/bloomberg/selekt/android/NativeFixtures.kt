package com.bloomberg.selekt.android

import com.bloomberg.selekt.commons.loadLibrary

internal object NativeFixtures {
    init {
        loadLibrary(checkNotNull(NativeFixtures::class.java.classLoader), "jni", "selektric")
        check(nativeInit() == 0)
    }

    fun load() = Unit

    private external fun nativeInit(): Int
}
