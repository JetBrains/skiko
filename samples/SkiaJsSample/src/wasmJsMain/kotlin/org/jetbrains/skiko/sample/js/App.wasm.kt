package org.jetbrains.skiko.sample.js

import kotlinx.browser.document

fun main() {
    runApp()
}

actual fun findHTMLCanvasElementById(id: String): Any = document.getElementById(id)!!