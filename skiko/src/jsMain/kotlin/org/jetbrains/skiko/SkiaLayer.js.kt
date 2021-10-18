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

    actual var app: SkikoApp? = null

    fun translateMouseEvent(
        event: MouseEvent, canvas: HTMLCanvasElement,
        buttons: Int,
        kind: SkikoMouseEventKind
    ): SkikoMouseEvent {
        val rect = canvas.getBoundingClientRect()
        return SkikoMouseEvent(
            (event.x - rect.x).toInt(), (event.y - rect.y).toInt(),
            buttons, kind, event
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
        htmlCanvas.addEventListener("mousedown", { event ->
            event as MouseEvent
            app?.onMouseEvent(translateMouseEvent(
                event, htmlCanvas, SkikoMouseButtons.LEFT, SkikoMouseEventKind.DOWN))
        })
        htmlCanvas.addEventListener("mouseup", { event ->
            event as MouseEvent
            app?.onMouseEvent(translateMouseEvent(
                event, htmlCanvas, SkikoMouseButtons.LEFT, SkikoMouseEventKind.UP))
        })
        htmlCanvas.addEventListener("mousemove", { event ->
            event as MouseEvent
            app?.onMouseEvent(translateMouseEvent(
                event, htmlCanvas, SkikoMouseButtons.NONE, SkikoMouseEventKind.MOVE))
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
actual typealias SkikoPlatformPointerEvent = MouseEvent
