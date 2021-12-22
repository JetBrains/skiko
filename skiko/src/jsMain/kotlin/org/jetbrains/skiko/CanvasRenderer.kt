package org.jetbrains.skiko

import kotlinx.browser.window
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.wasm.CreateWebGLContext
import org.jetbrains.skiko.wasm.GL
import org.w3c.dom.HTMLCanvasElement

abstract class CanvasRenderer constructor(val htmlCanvas: HTMLCanvasElement) {
    private val contextPointer = CreateWebGLContext(htmlCanvas)
    private val context: DirectContext
    private var surface: Surface? = null
    private var renderTarget: BackendRenderTarget? = null
    var canvas: Canvas? = null

    val width: Int
        get() = htmlCanvas.width
    val height: Int
        get() = htmlCanvas.height

    init {
        GL.makeContextCurrent(contextPointer)
        context = DirectContext.makeGL()
    }

    fun initCanvas(desiredWidth: Int, desiredHeight: Int, scale: Float) {
        disposeCanvas()
        htmlCanvas.width = (desiredWidth * scale).toInt()
        htmlCanvas.height = (desiredHeight * scale).toInt()
        renderTarget = BackendRenderTarget.makeGL(width, height, 1, 8, 0, 0x8058)
        surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget!!,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB
        )
        canvas = surface!!.canvas
    }

    fun disposeCanvas() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
    }

    abstract fun drawFrame(currentTimestamp: Double)

    private var redrawScheduled = false

    fun needRedraw() {
        if (redrawScheduled) {
            return
        }
        redrawScheduled = true
        window.requestAnimationFrame { timestamp ->
            redrawScheduled = false
            GL.makeContextCurrent(contextPointer)
            // `clear` and `resetMatrix` make canvas not accumulate previous effects
            canvas?.clear(-1)
            canvas?.resetMatrix()
            drawFrame(timestamp)
            surface?.flushAndSubmit()
            context.flush()
        }
    }
}
