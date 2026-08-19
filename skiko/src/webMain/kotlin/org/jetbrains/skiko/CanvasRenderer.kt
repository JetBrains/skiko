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
 */
internal abstract class CanvasRenderer(
    private val contextPointer: NativePointer,
    width: Int,
    height: Int,
) {
    var width: Int = width
        private set
    var height: Int = height
        private set

    var isDisposed: Boolean = false
        private set

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
        GL.makeContextCurrent(contextPointer)
        context = DirectContext.makeGL()
        initCanvas()
    }

    private val requestAnimationFrameCallback: (timestamp: Double) -> Unit = callback@{ timestamp ->
        redrawScheduled = false
        if (isDisposed) return@callback

        GL.makeContextCurrent(contextPointer)
        // `clear` and `resetMatrix` make canvas not accumulate previous effects
        canvas?.clear(Color.WHITE)
        canvas?.resetMatrix()
        drawFrame(timestamp)
        surface?.flushAndSubmit()
        context.flush()
    }

    /**
     * (Re)creates the render target, surface and [canvas] at the current [width]/[height].
     */
    fun initCanvas() {
        GL.makeContextCurrent(contextPointer)
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

    /**
     * Frees the surface and render target.
     */
    private fun disposeCanvas() {
        GL.makeContextCurrent(contextPointer)
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
    }

    /**
     * Recreates the render target and surface at the new size, reusing the existing
     * WebGL context and [DirectContext].
     *
     * Must be called after the canvas element's `width`/`height` attributes change:
     * that resets the WebGL drawing buffer, while the Skia surface keeps targeting
     * the default framebuffer with the old dimensions.
     */
    fun resize(width: Int, height: Int) {
        check(!isDisposed) { "CanvasRenderer is disposed" }
        if (width == this.width && height == this.height) return
        this.width = width
        this.height = height
        initCanvas()
    }

    /**
     * Releases the GPU resources. The renderer can't be used afterwards;
     * a frame already scheduled via [needRedraw] becomes a no-op.
     */
    fun dispose() {
        if (isDisposed) return
        isDisposed = true
        GL.makeContextCurrent(contextPointer)
        disposeCanvas()
        canvas = null
        context.close()
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
        if (isDisposed || redrawScheduled) {
            return
        }
        redrawScheduled = true
        windowRequestAnimationFrame(requestAnimationFrameCallback)
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