package org.jetbrains.skia.impl

import org.jetbrains.skiko.Library

actual class Library {
    actual companion object {
        @JvmStatic
        actual fun staticLoad() {
            Library.load()
        }

        @JvmStatic
        external fun _nAfterLoad()
    }
}
