package org.jetbrains.skiko.context

import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.DirectContext
import org.jetbrains.skija.Picture
import org.jetbrains.skija.Surface
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.HardwareLayer
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.OS

internal val renderApi: GraphicsApi by lazy {
    val environment = System.getenv("SKIKO_RENDER_API")
    val property = System.getProperty("skiko.renderApi")
    if (environment != null) {
        parseRenderApi(environment)
    } else {
        parseRenderApi(property)
    }
}

private fun parseRenderApi(text: String?): GraphicsApi {
    when(text) {
        "RASTER" -> return GraphicsApi.RASTER
        "OPENGL" -> return GraphicsApi.OPENGL
        else -> return GraphicsApi.OPENGL
    }
}

internal fun createContextHandler(layer: HardwareLayer): ContextHandler {
    return when (renderApi) {
        GraphicsApi.RASTER -> RasterContextHandler(layer)
        GraphicsApi.OPENGL -> OpenGLContextHandler(layer)
        else -> TODO("Unsupported yet")
    }
}

internal abstract class ContextHandler(val layer: HardwareLayer) {
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
        surface?.close()
        renderTarget?.close()
    }
}