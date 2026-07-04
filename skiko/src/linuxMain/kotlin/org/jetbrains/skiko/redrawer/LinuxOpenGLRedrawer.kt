package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.runRestoringState
import org.jetbrains.skiko.*
import kotlin.time.TimeSource

/**
 * OpenGL [Redrawer] for the native Linux target. Owns the Skia [DirectContext] and the
 * [Surface] wrapping the window's default framebuffer.
 */
internal class LinuxOpenGLRedrawer(
    private val skiaLayer: SkiaLayer
) : Redrawer {
    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null

    private var currentWidth = 0
    private var currentHeight = 0

    override val renderInfo: String
        get() = "GraphicsApi: ${skiaLayer.renderApi}\nOS: ${hostOs.id} ${hostArch.id}\n"

    private val win = skiaLayer.window
    private val gl = GlContext(win).apply {
        makeCurrent()
        setSwapInterval(1) // Vsync enabled
    }

    private val initialTime = TimeSource.Monotonic.markNow()

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        renderImmediately()
    }

    override fun dispose() {
        frameDispatcher.cancel()
        gl.makeCurrent()
        disposeSurface()
        context?.close()
        context = null
        gl.close()
    }

    override fun needRender(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    override fun update(nanoTime: Long) {
        skiaLayer.update(nanoTime)
    }

    override fun renderImmediately() {
        gl.makeCurrent()
        skiaLayer.update(initialTime.elapsedNow().inWholeNanoseconds)
        skiaLayer.inDrawScope {
            performDraw()
        }
        gl.swapBuffers()
    }

    private fun LayerDrawScope.performDraw() {
        if (!initContext()) {
            throw RenderException("Cannot init graphic OpenGL context")
        }
        initSurface()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            skiaLayer.draw(this)
        }
        context?.flush()
    }

    private fun initContext(): Boolean {
        try {
            if (context == null) {
                context = DirectContext.makeGL()
            }
        } catch (_: Exception) {
            println("Failed to create Skia OpenGL context!")
            return false
        }
        return true
    }

    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    private fun LayerDrawScope.initSurface() {
        val w = scaledLayerWidth
        val h = scaledLayerHeight
        if (isSizeChanged(w, h)) {
            disposeSurface()
            renderTarget = BackendRenderTarget.makeGL(
                w,
                h,
                0,
                8,
                0, // fbId = 0 (default framebuffer)
                FramebufferFormat.GR_GL_RGBA8
            )
            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = skiaLayer.pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface?.canvas
                ?: error("Could not obtain Canvas from Surface")
        }
    }

    private fun disposeSurface() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
        canvas = null
    }
}
