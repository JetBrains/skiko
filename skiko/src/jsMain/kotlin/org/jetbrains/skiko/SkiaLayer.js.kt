package org.jetbrains.skiko

import kotlinx.browser.window
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

    fun attachTo(htmlCanvas: HTMLCanvasElement, autoDetach: Boolean = true) {
        val scale = contentScale
        if (scale != 1f && htmlCanvas.asDynamic().skikoScaled == undefined) {
            val desiredWidth = htmlCanvas.width
            val desiredHeight = htmlCanvas.height
            // Scale canvas to allow high DPI rendering as suggested in
            // https://www.khronos.org/webgl/wiki/HandlingHighDPI.
            htmlCanvas.asDynamic().skikoScaled = true
            htmlCanvas.style.width = "${desiredWidth}px"
            htmlCanvas.style.height = "${desiredHeight}px"
            htmlCanvas.width = (desiredWidth * scale).toInt()
            htmlCanvas.height = (desiredHeight * scale).toInt()
        }

        state = object: CanvasRenderer(htmlCanvas) {
            override fun drawFrame(currentTimestamp: Double) {
                // currentTimestamp is in milliseconds.
                val currentNanos = currentTimestamp * 1_000_000
                skikoView?.onRender(canvas, width, height, currentNanos.toLong())
            }
        }
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
}

actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = InputEvent
actual typealias SkikoPlatformKeyboardEvent = KeyboardEvent
//  MouseEvent is base class of PointerEvent
actual typealias SkikoPlatformPointerEvent = MouseEvent


