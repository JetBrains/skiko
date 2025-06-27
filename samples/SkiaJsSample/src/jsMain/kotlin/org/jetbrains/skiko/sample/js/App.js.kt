package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    print(::onWasmReady)
    onWasmReady {
    }
}

actual fun findElementById(id: String): Any? = document.getElementById(id)