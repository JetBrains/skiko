package org.jetbrains.skiko

import kotlinx.browser.window
import org.jetbrains.skia.*
import org.jetbrains.skiko.wasm.createWebGLContext
import org.jetbrains.skiko.wasm.GL
import org.w3c.dom.HTMLCanvasElement

/**
 * CanvasRenderer takes an [HTMLCanvasElement] instance and initializes
 * skiko's [Canvas] used for drawing (see [initCanvas]).
 *
 * After initialization [needRedraw] can be used to schedule a call to [drawFrame].
 * [drawFrame] has to be implemented to perform the actual drawing on [canvas].
 */
abstract class CanvasRenderer constructor(val htmlCanvas: HTMLCanvasElement) {
    private val contextPointer = createWebGLContext(htmlCanvas)
    private val context: DirectContext
    private var surface: Surface? = null
    private var renderTarget: BackendRenderTarget? = null

    /**
     * An instance of skiko [Canvas] used for drawing.
     * Created in [initCanvas].
     */
    protected var canvas: Canvas? = null
        private set

    /**
     * The current width of [htmlCanvas]
     */
    val width: Int
        get() = htmlCanvas.width

    /**
     * The current height of [htmlCanvas]
     */
    val height: Int
        get() = htmlCanvas.height

    init {
        GL.makeContextCurrent(contextPointer)
        context = DirectContext.makeGL()
    }

    /**
     * Initializes the canvas.
     *
     * @param desiredWidth - width in pixels
     * @param desiredHeight - height in pixels
     * @param scale - a value to adjust the canvas' size
     * (https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio)
     */
    fun initCanvas(desiredWidth: Int, desiredHeight: Int, scale: Float, pixelGeometry: PixelGeometry) {
        disposeCanvas()
        htmlCanvas.width = (desiredWidth * scale).toInt()
        htmlCanvas.height = (desiredHeight * scale).toInt()
        renderTarget = BackendRenderTarget.makeGL(width, height, 1, 8, 0, 0x8058)
        surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget!!,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB,
            SurfaceProps(pixelGeometry = pixelGeometry)
        ) ?: throw RenderException("Cannot create surface")
        canvas = surface!!.canvas
    }

    private fun disposeCanvas() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
    }

    /**
     * This function should implement the actual drawing on the canvas.
     *
     * @param currentTimestamp - in milliseconds
     */
    abstract fun drawFrame(currentTimestamp: Double)

    private var redrawScheduled = false

    /**
     * Schedules a call to [drawFrame] to the appropriate moment.
     */
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
