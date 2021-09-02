package org.jetbrains.skia.impl

import org.jetbrains.skiko.Library
import java.util.concurrent.atomic.AtomicBoolean

class Library {
    companion object {
        var loaded = AtomicBoolean(false)
        @JvmStatic
        fun staticLoad() {
            if (!loaded.compareAndExchange(false, true)) {
              Library.load()
            }
        }

        @JvmStatic external fun _nAfterLoad()
    }
}
