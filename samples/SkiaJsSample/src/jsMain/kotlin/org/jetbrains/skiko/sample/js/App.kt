package org.jetbrains.skiko.sample.js

import org.jetbrains.skia.Paint
import org.jetbrains.skiko.wasm.onWasmReady

fun main(args: Array<String>) {
    onWasmReady {
        println(Paint())
    }
}