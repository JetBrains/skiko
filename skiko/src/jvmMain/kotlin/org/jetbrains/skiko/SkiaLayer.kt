package org.jetbrains.skiko

import java.awt.Component
import java.awt.GraphicsEnvironment
import java.awt.Transparency
import java.awt.image.ComponentColorModel
import java.awt.image.BufferedImage
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.WritableRaster
import java.awt.image.Raster
import java.awt.image.SampleModel
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.DirectContext
import org.jetbrains.skija.FramebufferFormat
import org.jetbrains.skija.ImageInfo
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
    private val colorModel = ComponentColorModel(java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE)
    val storage = Bitmap()
    var image: BufferedImage? = null
    var imageData: ByteArray? = null
    var raster: WritableRaster? = null

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
        val dpi = contentScale
        val actualWidth = (width * dpi).toInt()
        val actualHeight = (height * dpi).toInt()

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
        
        val frameTime = System.nanoTime()

        initSkija(dpi)
        skijaState.apply {
            canvas!!.clear(bleachConstant)
            
            // cliping
            for (component in clipComponets) {
                clipRectBy(component)
            }

            val renderTime = System.nanoTime()
            renderer?.onRender(canvas!!, width, height)
            print("skia rendering: ${(System.nanoTime() - renderTime) / 1000000}ms : ")

            if (api == GraphicsApi.RASTER) {
                val readingTime = System.nanoTime()
                val bytes = storage.readPixels(storage.getImageInfo(), (actualWidth * 4).toLong(), 0, 0)
                print("reading bytes: ${(System.nanoTime() - readingTime) / 1000000}ms : ")

                if (bytes != null) {
                    val drawTime = System.nanoTime()
                    val buffer = DataBufferByte(bytes, bytes.size)
                    raster = Raster.createInterleavedRaster(
                        buffer,
                        actualWidth,
                        actualHeight,
                        actualWidth * 4, 4,
                        intArrayOf(2, 1, 0, 3), // BGRA order
                        null
                    )
                    image = BufferedImage(colorModel, raster!!, colorModel.isAlphaPremultiplied(), null)
                    getGraphics().drawImage(image!!, 0, 0, width, height, null)
                    print("awt drawing: ${(System.nanoTime() - drawTime) / 1000000}ms : ")
                }
            } else {
                context!!.flush()
            }
        }

        println("total: ${(System.nanoTime() - frameTime) / 1000000}ms")
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

    private fun initSkija(dpi: Float) {
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
            when (api) {
                GraphicsApi.OPENGL -> {
                    surface = Surface.makeFromBackendRenderTarget(
                        context,
                        renderTarget,
                        SurfaceOrigin.BOTTOM_LEFT,
                        SurfaceColorFormat.RGBA_8888,
                        ColorSpace.getSRGB()
                    )
                    canvas = surface!!.canvas
                }
                GraphicsApi.RASTER -> {
                    val actualWidth = (width * dpi).toInt()
                    val actualHeight = (height * dpi).toInt()
                    if (storage.getWidth() != actualWidth || storage.getHeight() != actualHeight) {
                        storage.allocPixelsFlags(ImageInfo.makeS32(actualWidth, actualHeight, ColorAlphaType.PREMUL), false)
                    }
                    canvas = Canvas(storage)
                }
                else -> TODO("Unsupported yet")
            }
        }
    }

    protected open fun scaleCanvas(dpi: Float) {
        skijaState.apply {
            canvas!!.scale(dpi, dpi)
        }
    }
}
