package org.jetbrains.skiko

import kotlinx.browser.window
import org.jetbrains.skia.Canvas
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

actual open class SkiaLayer {
    private var state: CanvasRenderer? = null

    actual var renderApi: GraphicsApi = GraphicsApi.WEBGL
    actual val contentScale: Float
        get() = window.devicePixelRatio.toFloat()
    actual var fullscreen: Boolean
        get() = false
        set(value) {
            if (value) throw Exception("Fullscreen is not supported!")
        }
    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw Exception("Transparency is not supported!")
        }

    actual fun needRedraw() {
        state?.needRedraw()
    }

    actual var skikoView: SkikoView? = null

    actual fun attachTo(container: Any) {
        attachTo(container as HTMLCanvasElement, false)
    }

    actual fun detach() {
        // TODO: when switch to the frame dispatcher - stop it here.
    }

    private var isPointerPressed = false

    private var desiredWidth = 0
    private var desiredHeight = 0

    fun attachTo(htmlCanvas: HTMLCanvasElement, autoDetach: Boolean = true) {
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
        htmlCanvas.addEventListener("pointerdown", { event ->
            event as MouseEvent
            isPointerPressed = true
            skikoView?.onPointerEvent(toSkikoEvent(event, true, SkikoPointerEventKind.DOWN))
        })
        htmlCanvas.addEventListener("pointerup", { event ->
            event as MouseEvent
            isPointerPressed = false
            skikoView?.onPointerEvent(toSkikoEvent(event, true, SkikoPointerEventKind.UP))
        })
        htmlCanvas.addEventListener("pointermove", { event ->
            event as MouseEvent
            if (isPointerPressed) {
                skikoView?.onPointerEvent(toSkikoDragEvent(event))
            } else {
                skikoView?.onPointerEvent(toSkikoEvent(event, false, SkikoPointerEventKind.MOVE))
            }
        })
        htmlCanvas.addEventListener("wheel", { event ->
            event as WheelEvent
            skikoView?.onPointerEvent(toSkikoScrollEvent(event, isPointerPressed))
        })
        htmlCanvas.addEventListener("contextmenu", { event ->
            event.preventDefault()
        })
        htmlCanvas.addEventListener("keydown", { event ->
            event as KeyboardEvent
            skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.DOWN))
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
actual typealias SkikoPlatformInputEvent = InputEvent
actual typealias SkikoPlatformKeyboardEvent = KeyboardEvent
//  MouseEvent is base class of PointerEvent
actual typealias SkikoPlatformPointerEvent = MouseEvent


