package com.bloomberg.selekt.jupiter

import com.bloomberg.selekt.android.NativeFixtures
import org.junit.jupiter.api.extension.Extension

internal class SelektTestExtension : Extension {
    init {
        NativeFixtures.load()
    }
}
