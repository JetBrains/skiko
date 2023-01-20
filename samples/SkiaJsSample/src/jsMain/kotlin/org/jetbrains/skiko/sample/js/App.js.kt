package org.jetbrains.skiko.sample.js

import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady(::runApp)
}