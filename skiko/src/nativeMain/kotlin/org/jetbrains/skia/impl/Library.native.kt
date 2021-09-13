package org.jetbrains.skia.impl

actual class Library {
    actual companion object {
        actual fun staticLoad() {
            // Not much here for now.
            // We link statically for native.
        }
    }
}
