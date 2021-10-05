package org.jetbrains.skiko.native.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.native.*

internal expect fun createContextHandler(layer: HardwareLayer): ContextHandler

internal abstract class ContextHandler(val layer: HardwareLayer) {

    // TODO: hostOs is all written in jdk kotlin.
    // open val bleachConstant = if (hostOs == OS.MacOS) 0 else -1
    open val bleachConstant = 0
    var context: DirectContext? = null
    var renderTarget: BackendRenderTarget? = null
    var surface: Surface? = null
    var canvas: Canvas? = null

    abstract fun initContext(): Boolean

    abstract fun initCanvas()

    fun clearCanvas() {
        println("ContextHandler::clearCanvas")
        canvas?.clear(bleachConstant)
    }

    open fun drawOnCanvas(picture: Picture) {
        println("ContextHandler::drawOnCanvas")
        canvas?.drawPicture(picture)
    }

    open fun flush() {
        println("ContextHandler::flush")
        context?.flush()
    }

    fun dispose() {
        surface?.close()
        renderTarget?.close()
    }
}
