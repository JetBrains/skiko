package org.jetbrains.skiko.wasm

actual fun onWasmReady(onReady: () -> Unit) {
    onReady()
}

