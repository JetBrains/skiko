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

    actual var renderer: SkiaRenderer? = null

    actual var eventProcessor: SkikoEventProcessor? = null

    fun setCanvas(htmlCanvas: HTMLCanvasElement) {
        state = object: CanvasRenderer(htmlCanvas) {
            override fun drawFrame(currentTimestamp: Double) {
                // currentTimestamp is milliseconds.
                val currentNanos = currentTimestamp * 1000000
                renderer?.onRender(canvas, width, height, currentNanos.toLong())
            }
        }
        // See https://www.w3schools.com/jsref/dom_obj_event.asp
        htmlCanvas.addEventListener("mousedown", { event ->
            event as PointerEvent
            eventProcessor?.onMouseEvent(SkikoMouseEvent(
                event.x.toInt(), event.y.toInt(),
                SkikoMouseButtons.LEFT,
                SkikoMouseEventKind.DOWN,
                event
            ))
        })
        htmlCanvas.addEventListener("mouseup", { event ->
            event as PointerEvent
            eventProcessor?.onMouseEvent(SkikoMouseEvent(
                event.x.toInt(), event.y.toInt(),
                SkikoMouseButtons.LEFT,
                SkikoMouseEventKind.UP,
                event
            ))
        })
        htmlCanvas.addEventListener("mousemove", { event ->
            event as MouseEvent
            eventProcessor?.onMouseEvent(SkikoMouseEvent(
                event.x.toInt(), event.y.toInt(),
                SkikoMouseButtons.NONE,
                SkikoMouseEventKind.MOVE,
                null
            ))
        })
        htmlCanvas.addEventListener("keydown", { event ->
            event as KeyboardEvent
            eventProcessor?.onKeyboardEvent(
                    SkikoKeyboardEvent(
                    event.keyCode, SkikoKeyboardEventKind.DOWN, event
                )
            )
        })
        htmlCanvas.addEventListener("keyup", { event ->
            event as KeyboardEvent
            eventProcessor?.onKeyboardEvent(SkikoKeyboardEvent(
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
