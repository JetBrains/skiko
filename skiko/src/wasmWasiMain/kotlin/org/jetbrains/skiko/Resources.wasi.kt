package org.jetbrains.skiko

actual suspend fun loadBytesFromPath(path: String): ByteArray {
    TODO("loadBytesFromPath is not implemented for wasm_non_browser")
}
