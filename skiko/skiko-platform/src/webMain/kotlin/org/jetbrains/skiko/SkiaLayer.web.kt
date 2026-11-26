@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.impl.Native
import org.jetbrains.skiko.wasm.EmscriptenWebGLContextAttributes
import org.jetbrains.skiko.wasm.createWebGLContextForCanvas
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.JsAny
import kotlin.js.JsNumber
import kotlin.js.toInt
import kotlin.js.unsafeCast
import kotlin.js.js

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

    internal var requestEmscriptenWebGLContextAttributes: EmscriptenWebGLContextAttributes? = null

    /**
     * [GraphicsApi.WEBGL] is the only supported renderApi for k/js (browser).
     */
    actual var renderApi: GraphicsApi = GraphicsApi.WEBGL

    /**
     * See https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio
     */
    actual val contentScale: Float
        get() = offscreenContentScale ?: devicePixelRatio().toFloat()

    private var offscreenContentScale: Float? = null
    private var offscreenFrameRate: Double? = null

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
        attachedCanvas = null
        offscreenContentScale = null
        offscreenFrameRate = null
    }

    /**
     * Recreates the drawing surface to match the current size of the attached canvas
     * element. Call this after changing the canvas element's `width`/`height` attributes.
     */
    fun resize(width: Int, height: Int) {
        val renderer = checkNotNull(state) { "SkiaLayer is not attached to a canvas" }
        if (offscreenContentScale != null) resizeOffscreenCanvas(attachedCanvas!!, width, height)
        renderer.resize(width, height)
    }

    actual val component: Any?
        get() = this.attachedCanvas

    private var attachedCanvas: JsAny? = null

    /**
     * Attaches a transferred `OffscreenCanvas` in a dedicated worker. All rendering,
     * including [renderDelegate], runs in that worker; DOM access is unavailable.
     *
     * [targetFrameRate] uses a timer (120 Hz by default), since worker rAF may also
     * be limited to 60 Hz. This is a best-effort rate, not a vsync or presentation
     * guarantee. Pass null to use worker rAF. Safari's display power policy may
     * still reduce the physical refresh rate.
     *
     * Pass the main window's devicePixelRatio as [contentScale]. Forward resize
     * and visibility changes from the window: call [resize] in physical pixels,
     * and [detach] while hidden to stop pending work. Reattach to resume.
     *
     * The canvas must be transferred before a context is created on its HTML
     * element. Transfer is irreversible; feature-detect on the main thread first.
     */
    @ExperimentalSkikoApi
    fun attachToOffscreenCanvas(
        canvas: JsAny,
        contentScale: Float = 1f,
        targetFrameRate: Double? = 120.0
    ) {
        require(isWorkerOffscreenCanvas(canvas)) { "Expected an OffscreenCanvas in a dedicated worker" }
        require(contentScale.isFinite() && contentScale > 0) { "contentScale must be finite and positive" }
        require(targetFrameRate == null || targetFrameRate.isFinite() && targetFrameRate > 0) {
            "targetFrameRate must be finite and positive"
        }
        if (attachedCanvas === canvas && state != null) {
            require(offscreenFrameRate == targetFrameRate) { "Detach before changing targetFrameRate" }
            offscreenContentScale = contentScale
            state!!.resize(canvasWidth(canvas), canvasHeight(canvas))
            return
        }
        detach()
        offscreenContentScale = contentScale
        offscreenFrameRate = targetFrameRate
        attachRenderer(canvas, canvasWidth(canvas), canvasHeight(canvas), targetFrameRate)
    }

    /**
     * Initializes the [CanvasRenderer] and events listeners.
     * Delegates rendering and events processing to [renderDelegate].
     */
    private fun attachTo(htmlCanvas: HTMLCanvasElement) {
        if (attachedCanvas === htmlCanvas && state != null) {
            // Re-attaching to the same canvas: reuse the WebGL context and DirectContext.
            // Creating a second DirectContext over the same WebGLRenderingContext corrupts
            // both contexts' GL state caches once either of them is destroyed (CMP-8615).
            state!!.resize(htmlCanvas.width, htmlCanvas.height)
            return
        }
        detach()
        attachRenderer(htmlCanvas, htmlCanvas.width, htmlCanvas.height)
    }

    private fun attachRenderer(canvasElement: JsAny, width: Int, height: Int, targetFrameRate: Double? = null) {
        val contextPointer = createWebGLContextForCanvas(canvasElement, requestEmscriptenWebGLContextAttributes)
        if (contextPointer == Native.NullPointer) throw RenderException("Cannot create WebGL2 context")
        GL.makeContextCurrent(contextPointer)

        // Get the actual sampling in the current webgl context and pass it to Skia.
        // This value affects the Skia's antialiasing strategy choice.
        val sampleCount: Int =
            currentGLContext(GL)?.getParameter(WebGLRenderingContext.SAMPLES)?.unsafeCast<JsNumber>()?.toInt() ?: 0

        state = object : CanvasRenderer(
            contextPointer = contextPointer,
            width = width,
            height = height,
            requestedSampleCount = sampleCount,
            targetFrameRate = targetFrameRate
        ) {
            override fun drawFrame(currentTimestamp: Double) {
                // currentTimestamp is in milliseconds.
                val currentNanos = currentTimestamp * 1_000_000
                renderDelegate?.onRender(canvas!!, this.width, this.height, currentNanos.toLong())
            }
        }
        attachedCanvas = canvasElement
    }

    internal actual fun draw(canvas: Canvas) {
        canvas.clear(Color.WHITE)
        renderDelegate?.onRender(canvas, state!!.width, state!!.height, currentNanoTime())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}

private fun devicePixelRatio(): Double = js("globalThis.devicePixelRatio || 1")
private fun isWorkerOffscreenCanvas(canvas: JsAny): Boolean = js("""
    typeof DedicatedWorkerGlobalScope !== 'undefined' && globalThis instanceof DedicatedWorkerGlobalScope &&
    typeof OffscreenCanvas !== 'undefined' && canvas instanceof OffscreenCanvas
""")
private fun canvasWidth(canvas: JsAny): Int = js("canvas.width")
private fun canvasHeight(canvas: JsAny): Int = js("canvas.height")
private fun resizeOffscreenCanvas(canvas: JsAny, width: Int, height: Int): Unit = js("""{
    if (canvas.width !== width) canvas.width = width;
    if (canvas.height !== height) canvas.height = height;
}""")
