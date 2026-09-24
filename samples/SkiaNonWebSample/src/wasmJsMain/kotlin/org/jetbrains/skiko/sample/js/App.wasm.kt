package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.ColorType
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    println("MAIN IS CALLED")

    onWasmReady {
        val description = "Skiko running with ${getPlatform().name}"
        document.title = description
        document.getElementById("description")?.innerHTML = description
        runApp()
    }
}
