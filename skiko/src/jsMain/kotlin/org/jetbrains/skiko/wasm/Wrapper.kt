package org.jetbrains.skiko.wasm

import kotlinx.dom.createElement
import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.Promise

external val wasmSetup: Promise<Boolean>
external fun onWasmReady(onReady: () -> Unit)

@JsExport
fun createSkikoCanvas(): HTMLCanvasElement {
    return document.createElement("canvas") {} as HTMLCanvasElement
}
