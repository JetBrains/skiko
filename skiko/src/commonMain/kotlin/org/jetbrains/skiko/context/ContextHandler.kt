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
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            drawContent()
        }
        flush()
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Canvas.cutoutFromClip(rectangle: ClipRectangle, scale: Float) {
    clipRect(
        left = rectangle.x * scale,
        top = rectangle.y * scale,
        right = (rectangle.x + rectangle.width) * scale,
        bottom = (rectangle.y + rectangle.height) * scale,
        mode = ClipMode.DIFFERENCE,
        antiAlias = true
    )
}

internal inline fun Canvas.runRestoringState(block: Canvas.() -> Unit) {
    val restoreCount = save()
    try {
        block()
    } finally {
        restoreToCount(restoreCount)
    }
}