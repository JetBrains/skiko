package org.jetbrains.skiko

import kotlinx.browser.window
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
        GL.makeContextCurrent(contextPointer)
        context = DirectContext.makeGL()
        initCanvas()
    }

    private fun onFrame(timestamp: Double) {
        redrawScheduled = false
        GL.makeContextCurrent(contextPointer)
        // `clear` and `resetMatrix` make canvas not accumulate previous effects
        canvas?.clear(Color.WHITE)
        canvas?.resetMatrix()
        drawFrame(timestamp)
        surface?.flushAndSubmit()
        context.flush()
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    private val rAfCallback = rAfCallbackToJs {
        onFrame(it)
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
        if (redrawScheduled) {
            return
        }
        redrawScheduled = true
        windowRequestAnimationFrame(rAfCallback)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun windowRequestAnimationFrame(callback: JsAny) : Int =
    //language=JavaScript
    js("window.requestAnimationFrame(callback)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun rAfCallbackToJs(callback: (Double) -> Unit): JsAny =
    //language=JavaScript
    js("(timestamp) => callback(timestamp)")

internal external interface GLInterface {
    fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): NativePointer
    fun makeContextCurrent(contextPointer: NativePointer): Boolean;
}

internal expect val GL: GLInterface