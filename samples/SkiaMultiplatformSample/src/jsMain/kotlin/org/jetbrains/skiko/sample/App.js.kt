package org.jetbrains.skiko.sample

import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady(:runApp)
}