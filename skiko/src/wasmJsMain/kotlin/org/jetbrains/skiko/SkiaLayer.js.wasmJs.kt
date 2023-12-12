package org.jetbrains.skiko

import kotlinx.browser.window
import org.jetbrains.skiko.w3c.HTMLCanvasElement

import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

internal actual fun SkiaLayer.bindCanvasEventsToSkikoView(
    canvas: HTMLCanvasElement
) {
    val htmlCanvas = canvas as org.w3c.dom.HTMLCanvasElement

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

actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = KeyboardEvent
actual typealias SkikoPlatformKeyboardEvent = KeyboardEvent
//  MouseEvent is base class of PointerEvent
actual typealias SkikoPlatformPointerEvent = MouseEvent

internal actual fun SkiaLayer.setOnChangeScaleNotifier() {
    state?.initCanvas(desiredWidth, desiredHeight, contentScale, this.pixelGeometry)
    window.matchMedia("(resolution: ${contentScale}dppx)")
        .addEventListener("change", { setOnChangeScaleNotifier() }, true)
    onContentScaleChanged?.invoke(contentScale)
}