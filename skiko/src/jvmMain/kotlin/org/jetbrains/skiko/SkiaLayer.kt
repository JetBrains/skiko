package org.jetbrains.skiko

import java.awt.Component
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.DirectContext
import org.jetbrains.skija.FramebufferFormat
import org.jetbrains.skija.Rect
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skija.ClipMode

private class SkijaState {
    val bleachConstant = if (hostOs == OS.MacOS) 0 else -1
    var context: DirectContext? = null
    var renderTarget: BackendRenderTarget? = null
    var surface: Surface? = null
    var canvas: Canvas? = null

    fun clear() {
        surface?.close()
        renderTarget?.close()
    }
}

interface SkiaRenderer {
    fun onInit()
    fun onRender(canvas: Canvas, width: Int, height: Int)
    fun onReshape(width: Int, height: Int)
    fun onDispose()
}

open class SkiaLayer() : HardwareLayer() {
    open val api: GraphicsApi = GraphicsApi.OPENGL

    var renderer: SkiaRenderer? = null
    val clipComponets = mutableListOf<ClipRectangle>()

    private val skijaState = SkijaState()
    protected var inited: Boolean = false

    fun reinit() {
        inited = false
    }

    override fun disposeLayer() {
        super.disposeLayer()
        renderer?.onDispose()
    }

    private val fpsCounter = FPSCounter(
        count = System.getProperty("skiko.fps.count")?.toInt() ?: 500,
        probability = System.getProperty("skiko.fps.probability")?.toDouble() ?: 0.97
    )

    override fun draw() {
        if (System.getProperty("skiko.fps.enabled") == "true") {
            fpsCounter.tick()
        }

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
            canvas!!.clear(bleachConstant)
            
            // cliping
            for (component in clipComponets) {
                clipRectBy(component)
            }

            renderer?.onRender(canvas!!, width, height)
            context!!.flush()
        }
    }

    private fun clipRectBy(rectangle: ClipRectangle) {
        skijaState.apply {
            canvas!!.clipRect(
                Rect.makeLTRB(
                    rectangle.x,
                    rectangle.y,
                    rectangle.x + rectangle.width,
                    rectangle.y + rectangle.height
                ),
                ClipMode.DIFFERENCE,
                true
            )
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
