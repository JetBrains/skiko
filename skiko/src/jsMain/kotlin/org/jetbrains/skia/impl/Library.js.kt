package org.jetbrains.skia.impl

actual class Library {
    actual companion object {
        actual fun staticLoad() {
            // TODO: load wasm and js modules, maybe just js, as it will load Wasm.
            TODO()
        }
    }
}
