package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.*

/**
 * TODO: remove once the native macOS backends converge on the render-context shape the AWT ones use.
 */
internal abstract class ContextHandler(
    protected val layer: SkiaLayer,
    private val drawContent: Canvas.() -> Unit
) {
    protected var context: DirectContext? = null
    protected var renderTarget: BackendRenderTarget? = null
    protected var surface: Surface? = null
    protected var canvas: Canvas? = null

    protected abstract fun initContext(): Boolean
    protected abstract fun LayerDrawScope.initCanvas()

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
    fun LayerDrawScope.draw(flush: Boolean = true) {
        if (!initContext()) {
            throw RenderException("Cannot init graphic context")
        }
        initCanvas()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            drawContent()
        }
        if (flush) {
            flush()
        }
    }
}

internal fun LayerDrawScope.draw(contextHandler: ContextHandler, flush: Boolean = true) {
    with(contextHandler) {
        this@draw.draw(flush)
    }
}
