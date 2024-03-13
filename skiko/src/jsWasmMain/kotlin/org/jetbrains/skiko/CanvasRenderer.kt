package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.w3c.HTMLCanvasElement
import org.jetbrains.skiko.w3c.window
import org.jetbrains.skiko.wasm.createWebGLContext

/**
 * CanvasRenderer takes an [HTMLCanvasElement] instance and initializes
 * skiko's [Canvas] used for drawing (see [initCanvas]).
 *
 * After initialization [needRedraw] can be used to schedule a call to [drawFrame].
 * [drawFrame] has to be implemented to perform the actual drawing on [canvas].
 */
internal abstract class CanvasRenderer(
    private val contextPointer: NativePointer,
    val width: Int,
    val height: Int,
) {
    private val context: DirectContext
    private var surface: Surface? = null
    private var renderTarget: BackendRenderTarget? = null

    /**
     * An instance of skiko [Canvas] used for drawing.
     * Created in [initCanvas].
     */
    protected var canvas: Canvas? = null
        private set

    init {
        makeGLContextCurrent(contextPointer)
        context = DirectContext.makeGL()
        initCanvas()
    }

    fun initCanvas() {
        disposeCanvas()

        renderTarget = BackendRenderTarget.makeGL(width, height, 1, 8, 0, 0x8058)
        surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget!!,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB,
            SurfaceProps()
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
            makeGLContextCurrent(contextPointer)
            // `clear` and `resetMatrix` make canvas not accumulate previous effects
            canvas?.clear(Color.WHITE)
            canvas?.resetMatrix()
            drawFrame(timestamp)
            surface?.flushAndSubmit()
            context.flush()
        }
    }
}

internal expect fun makeGLContextCurrent(contextPointer: NativePointer)