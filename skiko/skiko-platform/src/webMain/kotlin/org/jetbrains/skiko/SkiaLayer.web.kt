@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.jetbrains.skiko

import kotlinx.browser.window
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.wasm.EmscriptenWebGLContextAttributes
import org.jetbrains.skiko.wasm.createWebGLContextForCanvas
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js
import kotlin.js.JsNumber
import kotlin.js.toInt
import kotlin.js.unsafeCast

/**
 * Provides a way to render the content and to receive the input events.
 * Rendering and events processing should be implemented in [renderDelegate].
 *
 * SkikoLayer needs to be initialized with [HTMLCanvasElement] instance
 * using [attachTo] method.
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

    internal var state: CanvasRenderer? = null

    /**
     * Requests experimental 120 Hz rendering using a transferred OffscreenCanvas.
     * Set before attaching. The render delegate and DOM events stay on the main thread.
     * Falls back to ordinary rAF when transfer or OffscreenCanvas WebGL2 is unavailable.
     * Transfer is irreversible; use [resize] rather than changing HTML canvas dimensions.
     * Actual presentation rate depends on WebKit, the display and power settings.
     */
    @ExperimentalSkikoApi
    var experimentalHighRefreshRateEnabled: Boolean = false
        set(value) {
            check(state == null) { "Configure high refresh rate before attaching" }
            field = value
        }

    private var renderingCanvas: JsAny? = null

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
        state?.needRedraw()
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
        state?.dispose()
        state = null
        htmlCanvas = null
        renderingCanvas = null
    }

    /**
     * Recreates the drawing surface to match the current size of the attached canvas
     * element. Call this after changing the canvas element's `width`/`height` attributes.
     */
    fun resize(width: Int, height: Int) {
        val renderer = checkNotNull(state) { "SkiaLayer is not attached to a canvas" }
        renderingCanvas?.let { resizeRenderingCanvas(it, width, height) }
        renderer.resize(width, height)
    }

    actual val component: Any?
        get() = this.htmlCanvas

    private var htmlCanvas: HTMLCanvasElement? = null

    /**
     * Initializes the [CanvasRenderer] and events listeners.
     * Delegates rendering and events processing to [renderDelegate].
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    private fun attachTo(htmlCanvas: HTMLCanvasElement) {
        if (this.htmlCanvas === htmlCanvas && state != null) {
            // Re-attaching to the same canvas: reuse the WebGL context and DirectContext.
            // Creating a second DirectContext over the same WebGLRenderingContext corrupts
            // both contexts' GL state caches once either of them is destroyed (CMP-8615).
            // A transferred HTML canvas is a placeholder; its dimensions cannot be assigned.
            if (renderingCanvas === htmlCanvas) state!!.resize(htmlCanvas.width, htmlCanvas.height)
            return
        }
        detach()
        this.htmlCanvas = htmlCanvas

        val backing = acquireRenderingCanvas(htmlCanvas, experimentalHighRefreshRateEnabled)
        renderingCanvas = backing
        val contextPointer = createWebGLContextForCanvas(backing, requestEmscriptenWebGLContextAttributes)
        GL.makeContextCurrent(contextPointer)

        // Get the actual sampling in the current webgl context and pass it to Skia.
        // This value affects the Skia's antialiasing strategy choice.
        val sampleCount: Int =
            currentGLContext(GL)?.getParameter(WebGLRenderingContext.SAMPLES)?.unsafeCast<JsNumber>()?.toInt() ?: 0

        state = object : CanvasRenderer(
            contextPointer = contextPointer,
            width = renderingWidth(backing),
            height = renderingHeight(backing),
            requestedSampleCount = sampleCount,
            targetFrameRate = if (experimentalHighRefreshRateEnabled && backing !== htmlCanvas) 120.0 else null
        ) {
            override fun drawFrame(currentTimestamp: Double) {
                // currentTimestamp is in milliseconds.
                val currentNanos = currentTimestamp * 1_000_000
                renderDelegate?.onRender(canvas!!, width, height, currentNanos.toLong())
            }
        }
    }

    internal actual fun draw(canvas: Canvas) {
        canvas.clear(Color.WHITE)
        renderDelegate?.onRender(canvas, state!!.width, state!!.height, currentNanoTime())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}

// Keep the backing canvas across detach/reattach and across SkiaLayer instances.
// HTMLCanvasElement.transferControlToOffscreen() can only be called once.
private val transferredCanvases: JsAny = js("new WeakMap()")

@OptIn(ExperimentalWasmJsInterop::class)
private fun acquireRenderingCanvas(canvas: HTMLCanvasElement, enabled: Boolean, cache: JsAny = transferredCanvases): JsAny = js("""(function() {
    var cached = cache.get(canvas);
    if (cached) return cached;
    if (!enabled || typeof canvas.transferControlToOffscreen !== 'function' || typeof OffscreenCanvas === 'undefined') return canvas;
    var probe = new OffscreenCanvas(1, 1);
    var gl = probe.getContext('webgl2');
    if (!gl) return canvas;
    var lose = gl.getExtension('WEBGL_lose_context');
    if (lose) lose.loseContext();
    try {
        var offscreen = canvas.transferControlToOffscreen();
        cache.set(canvas, offscreen);
        return offscreen;
    } catch (error) {
        if (error.name === 'InvalidStateError' || error.name === 'NotSupportedError') return canvas;
        throw error;
    }
})()""")
private fun renderingWidth(canvas: JsAny): Int = js("canvas.width")
private fun renderingHeight(canvas: JsAny): Int = js("canvas.height")
private fun resizeRenderingCanvas(canvas: JsAny, width: Int, height: Int): Unit = js("""{
    if (canvas.width !== width) canvas.width = width;
    if (canvas.height !== height) canvas.height = height;
}""")
