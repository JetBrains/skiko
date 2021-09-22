package org.jetbrains.skiko.context

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Picture
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.hostFullName
import org.jetbrains.skiko.javaLocation
import org.jetbrains.skiko.javaVendor

internal fun createContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler {
    return when (renderApi) {
        GraphicsApi.SOFTWARE -> SoftwareContextHandler(layer)
        // GraphicsApi.SOFTWARE -> WindowsSoftwareContextHandler(layer)
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

    open fun disposeCanvas() {
        surface?.close()
        renderTarget?.close()
    }

    open fun rendererInfo(): String {
        return "GraphicsApi: ${layer.renderApi}\n" +
            "OS: $hostFullName\n" +
            "Java: $javaVendor\n" +
            "Java location: $javaLocation\n"
    }

    protected open fun destroyContext() {
        context?.close()
    }
}
