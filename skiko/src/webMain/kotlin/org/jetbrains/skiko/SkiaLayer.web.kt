package org.jetbrains.skiko

import kotlinx.browser.window
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.wasm.EmscriptenWebGLContextAttributes
import org.jetbrains.skiko.wasm.createWebGLContext
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.ExperimentalWasmJsInterop

/**
 * Provides a way to render the content and to receive the input events.
 * Rendering and events processing should be implemented in [renderDelegate].
 *
 * SkikoLayer needs to be initialized with [HTMLCanvasElement] instance
 * using [attachTo] method.
 *
 * Internally this drives an internal [WebGLRenderContext] (which owns the WebGL Skia surface) from a
 * `requestAnimationFrame` loop: each scheduled frame re-reads the `<canvas>` size, acquires a surface of that
 * size, hands its canvas to [renderDelegate], then presents.
 */
actual open class SkiaLayer {

    companion object {
        /**
         * @param emscriptenWebGLContextAttributes - the preferred attributes. It's not guaranteed that the browser will respect them.
         * https://github.com/emscripten-core/emscripten/blob/main/system/include/emscripten/html5_webgl.h#L31
         */
        @InternalSkikoApi
        fun makeWithContextAttributes(emscriptenWebGLContextAttributes: EmscriptenWebGLContextAttributes): SkiaLayer {
            return SkiaLayer().apply {
                requestEmscriptenWebGLContextAttributes = emscriptenWebGLContextAttributes
            }
        }
    }

    internal var renderContext: WebGLRenderContext? = null

    internal var requestEmscriptenWebGLContextAttributes: EmscriptenWebGLContextAttributes? = null

    /**
     * [GraphicsApi.WEBGL] is the only supported renderApi for k/js (browser).
     */
    actual var renderApi: GraphicsApi = GraphicsApi.WEBGL

    /**
     * See https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio
     */
    actual val contentScale: Float
        get() = window.devicePixelRatio.toFloat()

    /**
     * Fullscreen is not supported
     */
    actual var fullscreen: Boolean
        get() = false
        set(value) {
            if (value) throw Exception("Fullscreen is not supported!")
        }

    /**
     * Schedules a drawFrame to the appropriate moment.
     */
    actual fun needRender(throttledToVsync: Boolean) {
        scheduleFrame()
    }

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()")
    )
    actual fun needRedraw() = needRender()

    /**
     * An implementation of [SkikoRenderDelegate] with content rendering and
     * event processing logic.
     */
    actual var renderDelegate: SkikoRenderDelegate? = null

    /**
     * @param container - should be an instance of [HTMLCanvasElement]
     */
    actual fun attachTo(container: Any) {
        attachTo(container as HTMLCanvasElement)
    }

    actual fun detach() {
        renderContext?.close()
        renderContext = null
        htmlCanvas = null
    }

    /**
     * Recreates the drawing surface to match the current size of the attached canvas
     * element. Call this after changing the canvas element's `width`/`height` attributes.
     */
    fun resize(width: Int, height: Int) {
        checkNotNull(renderContext) { "SkiaLayer is not attached to a canvas" }.resize(width, height)
    }

    actual val component: Any?
        get() = this.htmlCanvas

    private var htmlCanvas: HTMLCanvasElement? = null

    private var redrawScheduled = false

    /**
     * Initializes the internal [WebGLRenderContext].
     * Delegates rendering and events processing to [renderDelegate].
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    private fun attachTo(htmlCanvas: HTMLCanvasElement) {
        if (this.htmlCanvas === htmlCanvas && renderContext != null) {
            // Re-attaching to the same canvas: reuse the WebGL context and DirectContext.
            // Creating a second DirectContext over the same WebGLRenderingContext corrupts
            // both contexts' GL state caches once either of them is destroyed (CMP-8615).
            renderContext!!.resize(htmlCanvas.width, htmlCanvas.height)
            return
        }
        detach()
        this.htmlCanvas = htmlCanvas
        renderContext = WebGLRenderContext(
            createWebGLContext(htmlCanvas, requestEmscriptenWebGLContextAttributes),
            htmlCanvas.width,
            htmlCanvas.height,
        )
    }

    /**
     * Coalescing `requestAnimationFrame` scheduler: multiple [scheduleFrame] calls before the next animation
     * frame collapse to a single [renderFrame].
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    private fun scheduleFrame() {
        if (redrawScheduled || renderContext == null) {
            return
        }
        redrawScheduled = true
        windowRequestAnimationFrame { timestamp ->
            redrawScheduled = false
            renderFrame(timestamp)
        }
    }

    /**
     * Renders a single frame: re-reads the `<canvas>` size, acquires a surface of that size from the
     * [renderContext], clears it, lets [renderDelegate] draw, then presents.
     *
     * @param timestampMillis the `requestAnimationFrame` timestamp, in milliseconds.
     */
    private fun renderFrame(timestampMillis: Double) {
        val context = renderContext ?: return
        if (context.isDisposed) return
        val canvasElement = htmlCanvas ?: return
        val width = canvasElement.width
        val height = canvasElement.height
        if (width <= 0 || height <= 0) return

        val nanoTime = (timestampMillis * 1_000_000).toLong()
        val surface = context.acquireSurface(width, height)
        // `clear` and `resetMatrix` make the canvas not accumulate previous effects.
        surface.canvas.clear(Color.WHITE)
        surface.canvas.resetMatrix()
        renderDelegate?.onRender(surface.canvas, width, height, nanoTime)
        context.present()
    }

    internal actual fun draw(canvas: Canvas) {
        val canvasElement = htmlCanvas
        val width = canvasElement?.width ?: 0
        val height = canvasElement?.height ?: 0
        canvas.clear(Color.WHITE)
        renderDelegate?.onRender(canvas, width, height, currentNanoTime())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun windowRequestAnimationFrame(callback: (Double) -> Unit): Int =
    //language=JavaScript
    js("window.requestAnimationFrame(callback)")
