package org.jetbrains.skiko.context

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Picture
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs

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
                "OS: ${hostOs.id} ${hostArch.id}\n"
    }

    protected open fun destroyContext() {
        context?.close()
    }
}
