package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.*

internal abstract class ContextHandler(
    protected val layer: SkiaLayer,
    private val drawContent: Canvas.() -> Unit
) {
    protected var context: DirectContext? = null
    protected var renderTarget: BackendRenderTarget? = null
    protected var surface: Surface? = null
    protected var canvas: Canvas? = null

    protected abstract fun initContext(): Boolean
    protected abstract fun initCanvas()

    protected open fun flush() {
        context?.flush()
    }

    open fun dispose() {
        disposeCanvas()
        context?.close()
    }

    protected open fun disposeCanvas() {
        surface?.close()
        renderTarget?.close()
    }

    open fun rendererInfo(): String {
        return "GraphicsApi: ${layer.renderApi}\n" +
                "OS: ${hostOs.id} ${hostArch.id}\n"
    }

    // throws RenderException if initialization of graphic context was not successful
    fun draw() {
        if (!initContext()) {
            throw RenderException("Cannot init graphic context")
        }
        initCanvas()
        canvas?.apply {
            clear(if (isTransparentBackground()) Color.TRANSPARENT else Color.WHITE)
            drawContent()
        }
        flush()
    }

    protected open fun isTransparentBackground(): Boolean {
        if (hostOs == OS.MacOS) {
            // MacOS transparency is always supported
            return true
        }
        if (layer.fullscreen) {
            // for non-MacOS in fullscreen transparency is not supported
            return false
        }
        // for non-MacOS in non-fullscreen transparency provided by [layer]
        return layer.transparency
    }
}
