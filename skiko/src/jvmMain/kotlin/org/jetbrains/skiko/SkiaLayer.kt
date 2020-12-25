package org.jetbrains.skiko

import java.awt.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
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
    val bleachConstant = if (hostOs == OS.MacOS) -1 else -1
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

    override fun display() {
        if (api == GraphicsApi.RASTER) {
            updateLayer()
            draw()
        } else {
            super.display()
        }
    }

    override fun draw() {
        if (!inited) {
            if (skijaState.context == null) {
                skijaState.context = when (api) {
                    GraphicsApi.OPENGL -> makeGLContext()
                    GraphicsApi.METAL -> makeMetalContext()
                    GraphicsApi.RASTER -> null
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

            if (api == GraphicsApi.RASTER) {
                var img: BufferedImage?
                val bais = ByteArrayInputStream(surface!!.makeImageSnapshot().encodeToData()!!.bytes)
                try {
                    img = ImageIO.read(bais)
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
                getGraphics().drawImage(img!!, 0, 0, width, height, null)
            } else {
                context!!.flush()
            }
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
        initSurface(dpi)
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
                GraphicsApi.RASTER -> null
                else -> TODO("Unsupported yet")
            }
        }
    }

    private fun initSurface(dpi: Float) {
        skijaState.apply {
            surface = when (api) {
                GraphicsApi.OPENGL -> Surface.makeFromBackendRenderTarget(
                    context,
                    renderTarget,
                    SurfaceOrigin.BOTTOM_LEFT,
                    SurfaceColorFormat.RGBA_8888,
                    ColorSpace.getSRGB()
                )
                GraphicsApi.RASTER -> Surface.makeRasterN32Premul(
                    (width * dpi).toInt(),
                    (height * dpi).toInt()
                )
                else -> TODO("Unsupported yet")
            }
            
            canvas = surface!!.canvas
        }
    }

    protected open fun scaleCanvas(dpi: Float) {
        skijaState.apply {
            canvas!!.scale(dpi, dpi)
        }
    }
}
