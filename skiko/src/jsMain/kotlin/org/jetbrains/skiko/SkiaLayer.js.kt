package org.jetbrains.skiko

import org.jetbrains.skiko.wasm.api.CanvasRenderer
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent

actual open class SkiaLayer {
    private var state: CanvasRenderer? = null

    actual var renderApi: GraphicsApi = GraphicsApi.WEBGL
    actual val contentScale: Float
        get() = 1.0f
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
        state?.draw()
    }

    actual var skikoView: SkikoView? = null

    actual fun attachTo(container: Any) {
        attachTo(container as HTMLCanvasElement, false)
    }

    actual fun detach() {
        // TODO: when switch to the frame dispatcher - stop it here.
    }

    fun attachTo(htmlCanvas: HTMLCanvasElement, autoDetach: Boolean = true) {
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
            skikoView?.onPointerEvent(toSkikoEvent(event, true, SkikoPointerEventKind.DOWN))
        })
        htmlCanvas.addEventListener("pointerup", { event ->
            event as MouseEvent
            skikoView?.onPointerEvent(toSkikoEvent(event, true, SkikoPointerEventKind.UP))
        })
        htmlCanvas.addEventListener("pointermove", { event ->
            event as MouseEvent
            skikoView?.onPointerEvent(toSkikoEvent(event, false, SkikoPointerEventKind.MOVE))
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
