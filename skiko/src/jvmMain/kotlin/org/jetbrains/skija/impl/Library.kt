package org.jetbrains.skija.impl

import org.jetbrains.skiko.Library

class Library {
    companion object {
        @JvmStatic
        fun staticLoad() {
            Library.load()
        }

        @JvmStatic
        external fun _nAfterLoad()
    }
}
