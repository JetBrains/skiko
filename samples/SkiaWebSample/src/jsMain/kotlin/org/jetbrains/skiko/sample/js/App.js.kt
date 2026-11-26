package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    if (runOffscreenWorker()) return
    onWasmReady {
        val description = "Skiko running with ${getPlatform().name}"
        document.title = description
        document.getElementById("description")?.innerHTML = description
        runApp()
    }
}
