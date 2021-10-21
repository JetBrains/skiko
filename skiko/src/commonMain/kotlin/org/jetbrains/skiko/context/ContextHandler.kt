package org.jetbrains.skiko.context

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Picture
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.*
import org.jetbrains.skiko.RenderException

internal abstract class ContextHandler(val layer: SkiaLayer) {
    protected open val clearColor = if (layer.transparency || hostOs == OS.MacOS) 0 else -1
    protected var context: DirectContext? = null
    protected var renderTarget: BackendRenderTarget? = null
    protected var surface: Surface? = null
    protected var canvas: Canvas? = null

    protected abstract fun initContext(): Boolean
    protected abstract fun initCanvas()

    private fun clearCanvas() {
        val color = if (layer.fullscreen && hostOs != OS.MacOS) -1 else clearColor
        canvas?.clear(color)
    }

    protected open fun drawOnCanvas(picture: Picture) {
        canvas?.drawPicture(picture)
    }

    protected open fun flush() {
        context?.flush()
    }

    open fun dispose() {
        disposeCanvas()
        destroyContext()
    }

    protected open fun disposeCanvas() {
        surface?.close()
        renderTarget?.close()
    }

    open fun rendererInfo(): String {
        return "GraphicsApi: ${layer.renderApi}\n" +
                "OS: ${hostOs.id} ${hostArch.id}\n"
    }

    protected open fun destroyContext() {
        context?.close()
    }

    // throws RenderException if initialization of graphic context was not successful
    fun draw() {
        if (!initContext()) {
            throw RenderException("Cannot init graphic context")
        }
        initCanvas()
        clearCanvas()
        layer.lockPicture {
            drawOnCanvas(it.instance)
        }
        flush()
    }
}
