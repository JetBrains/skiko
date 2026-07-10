package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.wasm.ContextAttributes
import org.w3c.dom.HTMLCanvasElement

/**
 * CanvasRenderer takes an [HTMLCanvasElement] instance and initializes
 * skiko's [Canvas] used for drawing (see [initCanvas]).
 *
 * After initialization [needRedraw] can be used to schedule a call to [drawFrame].
 * [drawFrame] has to be implemented to perform the actual drawing on [canvas].
 *
 * The backing surface is resized automatically whenever [htmlCanvas]'s width/height differ
 * from the last rendered frame, so changing the `<canvas>` element's size after construction
 * takes effect on the next frame.
 *
 * Call [dispose] to release the GL/Skia resources held by this renderer once it's no longer needed.
 */
internal abstract class CanvasRenderer(
    private val contextPointer: NativePointer,
    private val htmlCanvas: HTMLCanvasElement,
) {
    private val context: DirectContext
    private var surface: Surface? = null
    private var renderTarget: BackendRenderTarget? = null
    private var disposed = false

    /**
     * Current size of the backing surface, kept in sync with [htmlCanvas]'s width/height at
     * the start of every frame (see [resizeIfNeeded]).
     */
    var width: Int = htmlCanvas.width
        private set
    var height: Int = htmlCanvas.height
        private set

    /**
     * An instance of skiko [Canvas] used for drawing.
     * Created in [initCanvas].
     */
    protected var canvas: Canvas? = null
        private set

    init {
        GL.makeContextCurrent(contextPointer)
        context = DirectContext.makeGL()
        initCanvas()
    }

    private val requestAnimationFrameCallback: (timestamp: Double) -> Unit = { timestamp ->
        redrawScheduled = false
        if (!disposed) {
            GL.makeContextCurrent(contextPointer)
            resizeIfNeeded()
            // `clear` and `resetMatrix` make canvas not accumulate previous effects
            canvas?.clear(Color.WHITE)
            canvas?.resetMatrix()
            drawFrame(timestamp)
            surface?.flushAndSubmit()
            context.flush()
        }
    }

    /**
     * Re-reads [htmlCanvas]'s current width/height and recreates the backing surface if they
     * changed since the last frame.
     */
    private fun resizeIfNeeded() {
        val newWidth = htmlCanvas.width
        val newHeight = htmlCanvas.height
        if (newWidth != width || newHeight != height) {
            width = newWidth
            height = newHeight
            initCanvas()
        }
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
    @OptIn(ExperimentalWasmJsInterop::class)
    fun needRedraw() {
        if (redrawScheduled || disposed) {
            return
        }
        redrawScheduled = true
        windowRequestAnimationFrame(requestAnimationFrameCallback)
    }

    /**
     * Releases the resources held by this renderer: the Skia [Surface]/[BackendRenderTarget] and
     * the [DirectContext]. After this call [needRedraw] is a no-op. Mirrors the disposal pattern
     * used by [org.jetbrains.skiko.context.ContextHandler.dispose] on other platforms.
     */
    fun dispose() {
        if (disposed) return
        disposed = true
        canvas = null
        disposeCanvas()
        context.close()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun windowRequestAnimationFrame(callback: (Double) -> Unit) : Int =
    //language=JavaScript
    js("window.requestAnimationFrame(callback)")


internal external interface GLInterface {
    fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): NativePointer
    fun makeContextCurrent(contextPointer: NativePointer): Boolean;
}

internal expect val GL: GLInterface