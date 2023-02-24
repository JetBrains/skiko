package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.RenderException

internal abstract class ContextHandler(
    protected val layer: SkiaLayer,
    private val drawContent: Canvas.() -> Unit
) {
    // TODO can we simplify clearColor logic? is there a reason why SoftwareContextHandler has opposite logic?
    protected open val clearColor = if (layer.transparency || hostOs == OS.MacOS) 0 else -1
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
            clear(if (layer.fullscreen && hostOs != OS.MacOS) -1 else clearColor)
            drawContent()
        }
        flush()
    }

    fun backendTextureToImage(texture: GrBackendTexture): Image? {
        return context?.let {
            Image.makeFromBackendTexture(it, texture)
        }
    }
}
