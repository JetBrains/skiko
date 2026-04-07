@file:OptIn(ExperimentalSkikoApi::class)

package org.jetbrains.skiko.context

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skia.gpu.graphite.BackendTexture
import org.jetbrains.skia.gpu.graphite.GraphiteContext
import org.jetbrains.skia.runRestoringState
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs

internal sealed class ContextHandler(
    protected val layer: SkiaLayer,
    private val drawContent: Canvas.() -> Unit
) {
    protected var surface: Surface? = null
    protected var canvas: Canvas? = null

    protected abstract fun initContext(): Boolean
    protected abstract fun LayerDrawScope.initCanvas()
    abstract fun flush(scope: LayerDrawScope)
    abstract fun dispose()

    protected open fun disposeCanvas() {
        surface?.close()
    }

    open fun rendererInfo(): String {
        return "GraphicsApi: ${layer.renderApi}\n" +
                "OS: ${hostOs.id} ${hostArch.id}\n"
    }

    // throws RenderException if initialization of graphic context was not successful
    fun LayerDrawScope.draw() {
        if (!initContext()) {
            throw RenderException("Cannot init graphic context")
        }
        initCanvas()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            drawContent()
        }
        flush(this)
    }
}


internal abstract class GaneshContextHandler(
    layer: SkiaLayer
) : ContextHandler(layer, layer::draw) {
    protected var context: DirectContext? = null
    protected var renderTarget: BackendRenderTarget? = null

    override fun dispose() {
        disposeCanvas()
        context?.close()
    }

    override fun disposeCanvas() {
        super.disposeCanvas()
        renderTarget?.close()
    }

    override fun rendererInfo(): String {
        return "GraphicsApi: Ganesh ${layer.renderApi}\n" +
                "OS: ${hostOs.id} ${hostArch.id}\n"
    }
}

internal abstract class GraphiteContextHandler(
    layer: SkiaLayer
) : ContextHandler(layer, layer::draw) {
    protected var context: GraphiteContext? = null
    protected var backendTexture: BackendTexture? = null

    override fun dispose() {
        disposeCanvas()
        context?.close()
    }

    override fun disposeCanvas() {
        super.disposeCanvas()
        backendTexture?.close()
    }

    override fun rendererInfo(): String {
        return "GraphicsApi: Graphite ${layer.renderApi}\n" +
                "OS: ${hostOs.id} ${hostArch.id}\n"
    }
}
