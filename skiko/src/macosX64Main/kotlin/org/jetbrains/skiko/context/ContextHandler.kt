package org.jetbrains.skiko.native.context

import kotlinx.cinterop.ptr
import org.jetbrains.skiko.skia.native.*
import org.jetbrains.skiko.native.*

internal fun createContextHandler(layer: HardwareLayer): ContextHandler {
    return OpenGLContextHandler(layer)
    //return when (SkikoProperties.renderApi) {
    //    GraphicsApi.SOFTWARE -> SoftwareContextHandler(layer)
    //    GraphicsApi.OPENGL -> OpenGLContextHandler(layer)
    //    else -> TODO("Unsupported yet")
    //}
}

internal abstract class ContextHandler(val layer: HardwareLayer) {

    // TODO: hostOs is all written in jdk kotlin.
    // open val bleachConstant = if (hostOs == OS.MacOS) 0 else -1
    open val bleachConstant = 0U
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
        println("Need to free surface")
        println("Need to free renderTarget")
        // surface?.close()
        // renderTarget?.close()
    }
}
