package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.w3c.HTMLCanvasElement
import org.jetbrains.skiko.w3c.window
import org.jetbrains.skiko.wasm.createWebGLContext

/**
 * Provides a way to render the content and to receive the input events.
 * Rendering and events processing should be implemented in [skikoView].
 *
 * SkikoLayer needs to be initialized with [HTMLCanvasElement] instance
 * using [attachTo] method.
 */
actual open class SkiaLayer {
    internal var state: CanvasRenderer? = null

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
     * Transparency is not supported
     */
    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw Exception("Transparency is not supported!")
        }

    /**
     * Schedules a drawFrame to the appropriate moment.
     */
    actual fun needRedraw() {
        state?.needRedraw()
    }

    /**
     * An implementation of SkikoView with content rendering and
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
        // TODO: when switch to the frame dispatcher - stop it here.
    }

    actual val component: Any?
        get() = this.htmlCanvas

    private var htmlCanvas: HTMLCanvasElement? = null

    /**
     * Initializes the [CanvasRenderer] and events listeners.
     * Delegates rendering and events processing to [skikoView].
     */
    private fun attachTo(htmlCanvas: HTMLCanvasElement) {
        this.htmlCanvas = htmlCanvas

        state = object: CanvasRenderer(createWebGLContext(htmlCanvas), htmlCanvas.width, htmlCanvas.height) {
            override fun drawFrame(currentTimestamp: Double) {
                // currentTimestamp is in milliseconds.
                val currentNanos = currentTimestamp * 1_000_000
                renderDelegate?.onRender(canvas!!, width, height, currentNanos.toLong())
            }
        }
    }

    internal actual fun draw(canvas: Canvas) {
        renderDelegate?.onRender(canvas, state!!.width, state!!.height, currentNanoTime())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}