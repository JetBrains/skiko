package org.jetbrains.skia.impl

import org.jetbrains.skiko.Library
import java.util.concurrent.atomic.AtomicBoolean

actual class Library {
    actual companion object {
        var loaded = AtomicBoolean(false)
        @JvmStatic
        actual fun staticLoad() {
            if (loaded.compareAndSet(false, true)) {
              Library.load()
            }
        }

        @JvmStatic external fun _nAfterLoad()
    }
}
