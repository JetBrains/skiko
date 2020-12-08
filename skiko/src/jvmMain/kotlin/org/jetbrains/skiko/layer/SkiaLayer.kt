package org.jetbrains.skiko.layer

import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.DirectContext
import org.jetbrains.skija.FramebufferFormat
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skiko.render.GraphicsApi
import org.jetbrains.skiko.render.OpenGLApi
import org.jetbrains.skiko.render.makeGLContext
import org.jetbrains.skiko.render.makeMetalContext
import org.jetbrains.skiko.render.makeGLRenderTarget
import org.jetbrains.skiko.render.makeMetalRenderTarget
import org.jetbrains.skiko.render.SkiaRenderer

private class SkijaState {
    var context: DirectContext? = null
    var renderTarget: BackendRenderTarget? = null
    var surface: Surface? = null
    var canvas: Canvas? = null

    fun clear() {
        surface?.close()
        renderTarget?.close()
    }
}

open class SkiaLayer() : HardwareLayer() {
    open val api: GraphicsApi = GraphicsApi.OPENGL

    var renderer: SkiaRenderer? = null

    private val skijaState = SkijaState()
    protected var inited: Boolean = false

    fun reinit() {
        inited = false
    }

    override fun disposeLayer() {
        super.disposeLayer()
        renderer?.onDispose()
    }

    override fun draw() {
        if (!inited) {
            if (skijaState.context == null) {
                skijaState.context = when (api) {
                    GraphicsApi.OPENGL -> makeGLContext()
                    GraphicsApi.METAL -> makeMetalContext()
                    else -> TODO("Unsupported yet")
                }
            }
            renderer?.onInit()
            inited = true
            renderer?.onReshape(width, height)
        }
        initSkija()
        skijaState.apply {
            canvas!!.clear(-1)
            renderer?.onRender(canvas!!, width, height)
            context!!.flush()
        }
    }

    private fun initSkija() {
        val dpi = contentScale
        initRenderTarget(dpi)
        initSurface()
        scaleCanvas(dpi)
    }

    private fun initRenderTarget(dpi: Float) {
        skijaState.apply {
            clear()
            renderTarget = when (api) {
                GraphicsApi.OPENGL -> {
                    val gl = OpenGLApi.instance
                    val fbId = gl.glGetIntegerv(gl.GL_DRAW_FRAMEBUFFER_BINDING)
                    makeGLRenderTarget(
                        (width * dpi).toInt(),
                        (height * dpi).toInt(),
                        0,
                        8,
                        fbId,
                        FramebufferFormat.GR_GL_RGBA8
                    )
                }
                GraphicsApi.METAL -> makeMetalRenderTarget(
                    (width * dpi).toInt(),
                    (height * dpi).toInt(),
                    0
                )
                else -> TODO("Unsupported yet")
            }
        }
    }

    private fun initSurface() {
        skijaState.apply {
            surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.getSRGB()
            )
            canvas = surface!!.canvas
        }
    }

    protected open fun scaleCanvas(dpi: Float) {
        skijaState.apply {
            canvas!!.scale(dpi, dpi)
        }
    }
}
