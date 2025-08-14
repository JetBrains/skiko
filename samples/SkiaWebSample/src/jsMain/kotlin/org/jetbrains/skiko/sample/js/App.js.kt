package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        val description = "Skiko running with ${getPlatform().name}"
        document.title = description
        findElementById("description")?.innerHTML = description
        runApp()
    }
}