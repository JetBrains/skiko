package org.jetbrains.skiko

import org.jetbrains.skiko.wasm.api.CanvasRenderer
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.pointerevents.PointerEvent

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

    actual var app: SkikoApp? = null

    fun translatePointerEvent(
        event: MouseEvent,
        buttons: Int,
        kind: SkikoPointerEventKind
    ): SkikoPointerEvent {
        return SkikoPointerEvent(
            event.offsetX, event.offsetY,
            buttons, kind,
            if (event is PointerEvent) event else null
        )
    }

    fun setCanvas(htmlCanvas: HTMLCanvasElement) {
        state = object: CanvasRenderer(htmlCanvas) {
            override fun drawFrame(currentTimestamp: Double) {
                // currentTimestamp is in milliseconds.
                val currentNanos = currentTimestamp * 1_000_000
                app?.onRender(canvas, width, height, currentNanos.toLong())
            }
        }
        // See https://www.w3schools.com/jsref/dom_obj_event.asp
        // https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events
        htmlCanvas.addEventListener("pointerdown", { event ->
            event as PointerEvent
            app?.onPointerEvent(translatePointerEvent(
                event, SkikoMouseButtons.LEFT, SkikoPointerEventKind.DOWN))
        })
        htmlCanvas.addEventListener("pointerup", { event ->
            event as PointerEvent
            app?.onPointerEvent(translatePointerEvent(
                event, SkikoMouseButtons.LEFT, SkikoPointerEventKind.UP))
        })
        htmlCanvas.addEventListener("pointermove", { event ->
            event as PointerEvent
            app?.onPointerEvent(translatePointerEvent(
                event, SkikoMouseButtons.NONE, SkikoPointerEventKind.MOVE))
        })
        htmlCanvas.addEventListener("contextmenu", { event ->
            event as MouseEvent
            app?.onPointerEvent(translatePointerEvent(
                event, SkikoMouseButtons.RIGHT, SkikoPointerEventKind.DOWN))
            event.preventDefault()
        })
        htmlCanvas.addEventListener("keydown", { event ->
            event as KeyboardEvent
            app?.onKeyboardEvent(
                    SkikoKeyboardEvent(
                    event.keyCode, SkikoKeyboardEventKind.DOWN, event
                )
            )
        })
        htmlCanvas.addEventListener("keyup", { event ->
            event as KeyboardEvent
            app?.onKeyboardEvent(SkikoKeyboardEvent(
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
actual typealias SkikoPlatformPointerEvent = PointerEvent
