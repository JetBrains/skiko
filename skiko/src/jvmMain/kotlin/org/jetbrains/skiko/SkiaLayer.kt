package org.jetbrains.skiko

import org.jetbrains.skija.*
import org.jetbrains.skiko.redrawer.Redrawer
import java.awt.Graphics
import javax.swing.SwingUtilities.isEventDispatchThread

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
    suspend fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

private class PictureHolder(val instance: Picture, val width: Int, val height: Int)

open class SkiaLayer : HardwareLayer() {
    open val api: GraphicsApi = GraphicsApi.OPENGL

    var renderer: SkiaRenderer? = null
    val clipComponents = mutableListOf<ClipRectangle>()

    private val skijaState = SkijaState()

    @Volatile
    private var isDisposed = false
    private var redrawer: Redrawer? = null

    @Volatile
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()
    private val pictureLock = Any()

    override fun init() {
        super.init()
        redrawer = platformOperations.createHardwareRedrawer(this)
        redrawer?.syncSize()
        needRedraw()
    }

    override fun dispose() {
        check(!isDisposed)
        check(isEventDispatchThread())
        redrawer?.dispose()
        picture?.instance?.close()
        pictureRecorder.close()
        isDisposed = true
        super.dispose()
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        // we don't have to call it in setBounds method, because paint will always be called after setBounds
        redrawer?.syncSize()
        needRedraw()
    }

    fun needRedraw() {
        check(!isDisposed)
        check(isEventDispatchThread())
        redrawer?.needRedraw()
    }

    private val fpsCounter = FPSCounter(
        count = System.getProperty("skiko.fps.count")?.toInt() ?: 500,
        probability = System.getProperty("skiko.fps.probability")?.toDouble() ?: 0.97
    )

    override suspend fun update(nanoTime: Long) {
        check(!isDisposed)
        check(isEventDispatchThread())

        if (System.getProperty("skiko.fps.enabled") == "true") {
            fpsCounter.tick()
        }

        val pictureWidth = (width * contentScale).toInt().coerceAtLeast(0)
        val pictureHeight = (height * contentScale).toInt().coerceAtLeast(0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())!!
        val canvas = pictureRecorder.beginRecording(bounds)!!

        // clipping
        for (component in clipComponents) {
            canvas.clipRectBy(component)
        }

        renderer?.onRender(canvas, pictureWidth, pictureHeight, nanoTime)

        check(!isDisposed)

        synchronized(pictureLock) {
            picture?.instance?.close()
            val picture = pictureRecorder.finishRecordingAsPicture()
            this.picture = PictureHolder(picture, pictureWidth, pictureHeight)
        }
    }

    override fun draw() {
        check(!isDisposed)

        if (skijaState.context == null) {
            skijaState.context = when (api) {
                GraphicsApi.OPENGL -> makeGLContext()
                GraphicsApi.METAL -> makeMetalContext()
                else -> TODO("Unsupported yet")
            }
        }

        initSkija()

        skijaState.apply {
            canvas!!.clear(bleachConstant)
            synchronized(pictureLock) {
                val picture = picture
                if (picture != null) {
                    canvas!!.drawPicture(picture.instance)
                }
            }
            context!!.flush()
        }
    }

    private fun Canvas.clipRectBy(rectangle: ClipRectangle) {
        clipRect(
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

    private fun initSkija() {
        initRenderTarget()
        initSurface()
    }

    private fun initRenderTarget() {
        skijaState.apply {
            clear()
            val dpi = contentScale
            val width = (width * dpi).toInt().coerceAtLeast(0)
            val height = (height * dpi).toInt().coerceAtLeast(0)
            renderTarget = when (api) {
                GraphicsApi.OPENGL -> {
                    val gl = OpenGLApi.instance
                    val fbId = gl.glGetIntegerv(gl.GL_DRAW_FRAMEBUFFER_BINDING)
                    makeGLRenderTarget(
                        width,
                        height,
                        0,
                        8,
                        fbId,
                        FramebufferFormat.GR_GL_RGBA8
                    )
                }
                GraphicsApi.METAL -> makeMetalRenderTarget(
                    width,
                    height,
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
}
