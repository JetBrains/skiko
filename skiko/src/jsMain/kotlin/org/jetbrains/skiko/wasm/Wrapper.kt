package org.jetbrains.skiko.wasm

import kotlinx.dom.createElement
import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement


@JsExport
fun createSkikoCanvas(): HTMLCanvasElement {
    return document.createElement("canvas") {} as HTMLCanvasElement
}
