package org.jetbrains.skiko

import org.jetbrains.skiko.wasm.api.CanvasRenderer
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent

actual open class SkiaLayer(properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties()
) {
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
        draw()
    }

    actual var skikoView: SkikoView? = null

    fun translatePointerEvent(
        event: MouseEvent,
        buttons: Boolean,
        kind: SkikoPointerEventKind
    ): SkikoPointerEvent {
        var mask = SkikoMouseButtons.NONE
        if (buttons && event.button.toInt() == 0)
            mask = mask or SkikoMouseButtons.LEFT
        // https://www.w3schools.com/jsref/event_button.asp
        if (buttons && event.button.toInt() == 2)
            mask = mask or SkikoMouseButtons.RIGHT
        return SkikoPointerEvent(
            event.offsetX, event.offsetY,
            mask, kind, event
        )
    }

    fun setCanvas(htmlCanvas: HTMLCanvasElement) {
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
            skikoView?.onPointerEvent(translatePointerEvent(
                event, true, SkikoPointerEventKind.DOWN))
        })
        htmlCanvas.addEventListener("pointerup", { event ->
            event as MouseEvent
            var mask = SkikoMouseButtons.NONE
            skikoView?.onPointerEvent(translatePointerEvent(
                event, true, SkikoPointerEventKind.UP))
        })
        htmlCanvas.addEventListener("pointermove", { event ->
            event as MouseEvent
            skikoView?.onPointerEvent(translatePointerEvent(
                event, false, SkikoPointerEventKind.MOVE))
        })
        htmlCanvas.addEventListener("contextmenu", { event ->
            event.preventDefault()
        })
        htmlCanvas.addEventListener("keydown", { event ->
            event as KeyboardEvent
            skikoView?.onKeyboardEvent(
                    SkikoKeyboardEvent(
                    event.keyCode, SkikoKeyboardEventKind.DOWN, event
                )
            )
        })
        htmlCanvas.addEventListener("keyup", { event ->
            event as KeyboardEvent
            skikoView?.onKeyboardEvent(SkikoKeyboardEvent(
                event.keyCode, SkikoKeyboardEventKind.UP, event
            ))
        })
    }

    fun draw() {
        state?.draw()
    }
}

actual typealias SkikoPlatformInputEvent = InputEvent
actual typealias SkikoPlatformKeyboardEvent = KeyboardEvent
//  MouseEvent is base class of PointerEvent
actual typealias SkikoPlatformPointerEvent = MouseEvent
