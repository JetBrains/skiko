package org.jetbrains.skiko

import kotlinx.browser.window
import org.jetbrains.skia.Canvas
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

/**
 * Provides a way to render the content and to receive the input events.
 * Rendering and events processing should be implemented in [skikoView].
 *
 * SkikoLayer needs to be initialized with [HTMLCanvasElement] instance
 * using [attachTo] method.
 */
actual open class SkiaLayer {
    private var state: CanvasRenderer? = null

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
    actual var skikoView: SkikoView? = null

    /**
     * @param container - should be an instance of [HTMLCanvasElement]
     */
    actual fun attachTo(container: Any) {
        attachTo(container as HTMLCanvasElement, false)
    }

    actual fun detach() {
        // TODO: when switch to the frame dispatcher - stop it here.
    }

    private var isPointerPressed = false

    private var desiredWidth = 0
    private var desiredHeight = 0

    actual val component: Any?
        get() = this.htmlCanvas

    private var htmlCanvas: HTMLCanvasElement? = null

    /**
     * Initializes the [CanvasRenderer] and events listeners.
     * Delegates rendering and events processing to [skikoView].
     */
    fun attachTo(htmlCanvas: HTMLCanvasElement, autoDetach: Boolean = true) {
        this.htmlCanvas = htmlCanvas

        // Scale canvas to allow high DPI rendering as suggested in
        // https://www.khronos.org/webgl/wiki/HandlingHighDPI.
        desiredWidth = htmlCanvas.width
        desiredHeight = htmlCanvas.height
        htmlCanvas.style.width = "${desiredWidth}px"
        htmlCanvas.style.height = "${desiredHeight}px"
        setOnChangeScaleNotifier()

        state = object: CanvasRenderer(htmlCanvas) {
            override fun drawFrame(currentTimestamp: Double) {
                // currentTimestamp is in milliseconds.
                val currentNanos = currentTimestamp * 1_000_000
                skikoView?.onRender(canvas!!, width, height, currentNanos.toLong())
            }
        }.apply { initCanvas(desiredWidth, desiredHeight, contentScale) }
        // See https://www.w3schools.com/jsref/dom_obj_event.asp
        // https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events
        htmlCanvas.addEventListener("mousedown", { event ->
            event as MouseEvent
            isPointerPressed = true
            skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.DOWN))
        })
        htmlCanvas.addEventListener("mouseup", { event ->
            event as MouseEvent
            isPointerPressed = false
            skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.UP))
        })
        htmlCanvas.addEventListener("mousemove", { event ->
            event as MouseEvent
            if (isPointerPressed) {
                skikoView?.onPointerEvent(toSkikoDragEvent(event))
            } else {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.MOVE))
            }
        })
        htmlCanvas.addEventListener("wheel", { event ->
            event as WheelEvent
            skikoView?.onPointerEvent(toSkikoScrollEvent(event))
        })
        htmlCanvas.addEventListener("contextmenu", { event ->
            event.preventDefault()
        })
        htmlCanvas.addEventListener("keydown", { event ->
            event as KeyboardEvent
            skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.DOWN))

            toSkikoTypeEvent(event.key, event)?.let { inputEvent ->
                skikoView?.input?.onInputEvent(inputEvent)
            }
        })
        htmlCanvas.addEventListener("keyup", { event ->
            event as KeyboardEvent
            skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.UP))
        })
    }

    private fun setOnChangeScaleNotifier() {
        state?.initCanvas(desiredWidth, desiredHeight, contentScale)
        window.matchMedia("(resolution: ${contentScale}dppx)").addEventListener("change", { setOnChangeScaleNotifier() }, true)
        onContentScaleChanged?.invoke(contentScale)
    }

    internal actual fun draw(canvas: Canvas) {
        skikoView?.onRender(canvas, state!!.width, state!!.height, currentNanoTime())
    }
}

var onContentScaleChanged: ((Float) -> Unit)? = null

actual typealias SkikoTouchPlatformEvent = Any
actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = KeyboardEvent
actual typealias SkikoPlatformKeyboardEvent = KeyboardEvent
//  MouseEvent is base class of PointerEvent
actual typealias SkikoPlatformPointerEvent = MouseEvent
