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
    protected abstract fun DrawScope.initCanvas()

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

    /**
     * This function will be called only in a thread where it is valid to access layer properties.
     */
    protected abstract fun createDrawScope(): DrawScope

    /**
     * Reads layer properties, creating a [DrawScope] in which [DrawScope.contextHandlerDraw] can later be called on a
     * background thread.
     *
     * This function should be called only in a thread where it is valid to access layer properties.
     */
    inline fun inDrawScope(block: DrawScope.() -> Unit) {
        createDrawScope().block()
    }

    /**
     * This function should be called only in a thread where it is valid to access layer properties.
     */
    fun draw() {
        createDrawScope().contextHandlerDraw()
    }

    // throws RenderException if initialization of graphic context was not successful
    private fun DrawScope.drawImpl() {
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

    inner class DrawScope(
        val scaledLayerWidth: Int,
        val scaledLayerHeight: Int
    ) {
        constructor(layerWidth: Int, layerHeight: Int, scale: Float): this(
            scaledLayerWidth = (layerWidth * scale).toInt().coerceAtLeast(0),
            scaledLayerHeight = (layerHeight * scale).toInt().coerceAtLeast(0)
        )
        constructor(layerWidth: Double, layerHeight: Double, scale: Float): this(
            scaledLayerWidth = (layerWidth * scale).toInt().coerceAtLeast(0),
            scaledLayerHeight = (layerHeight * scale).toInt().coerceAtLeast(0)
        )

        fun contextHandlerDraw() {
            drawImpl()
        }
    }
}
