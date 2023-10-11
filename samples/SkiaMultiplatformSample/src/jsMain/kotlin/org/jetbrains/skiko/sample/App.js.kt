package org.jetbrains.skiko.sample

import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    window.addEventListener("DOMContentLoaded", {
        onWasmReady(::runApp)
    })
}
