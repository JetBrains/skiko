package org.jetbrains.skiko.wasm

actual fun onWasmReady(onReady: () -> Unit) {
    awaitSkiko.then {
        onReady()
        null
    }
}

