package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.gpu.ganesh.flushAndSubmit
import org.jetbrains.skia.gpu.ganesh.makeFromBackendRenderTarget
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.wasm.EmscriptenWebGLContextAttributes
import org.khronos.webgl.WebGLRenderingContextBase
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js

/**
 * Initializes Skiko's [Canvas] using an existing WebGL context (see [initCanvas]).
 *
 * After initialization [needRedraw] can be used to schedule a call to [drawFrame].
 * [drawFrame] has to be implemented to perform the actual drawing on [canvas].
 */
@InternalSkikoApi
abstract class CanvasRenderer(
    private val contextPointer: NativePointer,
    width: Int,
    height: Int,
    requestedSampleCount: Int = 1, // it must be the actual sampling from the current webgl context.
    highRefreshRate: Boolean = false,
) {
    var width: Int = width
        private set
    var height: Int = height
        private set

    // To remain compatible with the previous implementation, we coerce to 1.
    // Although Skia does it in src/gpu/ganesh/gl/GrGLBackendSurface.cpp#L295 too:
    // std::max(1, sampleCnt)
    val requestedSampleCount: Int = requestedSampleCount.coerceAtLeast(1)

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

    private val frameScheduler = WebFrameScheduler(highRefreshRate) callback@{ timestamp ->
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

        renderTarget = BackendRenderTarget.makeGL(width, height, requestedSampleCount, 8, 0, 0x8058)
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
        frameScheduler.dispose()
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

    /**
     * Schedules a call to [drawFrame] to the appropriate moment.
     */
    fun needRedraw() {
        if (!isDisposed) frameScheduler.requestFrame()
    }
}

@InternalSkikoApi
external interface GLInterface {
    fun createContext(context: JsAny, contextAttributes: EmscriptenWebGLContextAttributes): NativePointer
    fun makeContextCurrent(contextPointer: NativePointer): Boolean
}

@InternalSkikoApi
expect val GL: GLInterface

@InternalSkikoApi
@OptIn(ExperimentalWasmJsInterop::class)
fun currentGLContext(gl: GLInterface): WebGLRenderingContextBase? =
    js("gl.currentContext ? gl.currentContext.GLctx : null")
