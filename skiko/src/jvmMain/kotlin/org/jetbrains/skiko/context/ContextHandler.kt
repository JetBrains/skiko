package org.jetbrains.skiko.context

import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.DirectContext
import org.jetbrains.skija.Picture
import org.jetbrains.skija.Surface
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs

internal fun createContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler {
    return when (renderApi) {
        GraphicsApi.SOFTWARE -> SoftwareContextHandler(layer)
        GraphicsApi.OPENGL -> OpenGLContextHandler(layer)
        GraphicsApi.DIRECT3D -> Direct3DContextHandler(layer)
        GraphicsApi.METAL -> MetalContextHandler(layer)
        else -> TODO("Unsupported yet.")
    }
}

internal abstract class ContextHandler(val layer: SkiaLayer) {
    open val bleachConstant = if (hostOs == OS.MacOS) 0 else -1
    var context: DirectContext? = null
    var renderTarget: BackendRenderTarget? = null
    var surface: Surface? = null
    var canvas: Canvas? = null

    abstract fun initContext(): Boolean
    abstract fun initCanvas()

    fun clearCanvas() {
        canvas?.clear(bleachConstant)
    }

    open fun drawOnCanvas(picture: Picture) {
        canvas?.drawPicture(picture)
    }

    open fun flush() {
        context?.flush()
    }

    fun dispose() {
        disposeCanvas()
        destroyContext()
    }

    fun disposeCanvas() {
        surface?.close()
        renderTarget?.close()
    }

    open fun hardwareInfo(): String = ""

    protected open fun destroyContext() = Unit
}
