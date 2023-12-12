package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady(::runApp)
}

actual fun findHTMLCanvasElementById(id: String): Any = document.getElementById(id)!!