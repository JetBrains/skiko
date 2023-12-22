package org.jetbrains.skiko

import kotlinx.browser.window
import org.jetbrains.skiko.w3c.HTMLCanvasElement
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.events.UIEvent

internal actual fun SkiaLayer.bindCanvasEventsToSkikoView(
    canvas: HTMLCanvasElement
) {
    val htmlCanvas = canvas.unsafeCast<org.w3c.dom.HTMLCanvasElement>()
    var offsetX = 0.0
    var offsetY = 0.0
    htmlCanvas.addEventListener("touchstart", { event ->
        event.preventDefault()
        event as TouchEvent
        htmlCanvas.getBoundingClientRect().apply {
            offsetX = left
            offsetY = top
        }
        skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.DOWN, offsetX, offsetY))
    })

    htmlCanvas.addEventListener("touchmove", { event ->
        event.preventDefault()
        event as TouchEvent
        skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.MOVE, offsetX, offsetY))
    })

    htmlCanvas.addEventListener("touchend", { event ->
        event.preventDefault()
        event as TouchEvent
        skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.UP, offsetX, offsetY))
    })

    htmlCanvas.addEventListener("touchcancel", { event ->
        event.preventDefault()
        event as TouchEvent
        skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.UP, offsetX, offsetY))
    })
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
actual typealias SkikoPlatformPointerEvent = UIEvent

internal actual fun SkiaLayer.setOnChangeScaleNotifier() {
    state?.initCanvas(desiredWidth, desiredHeight, contentScale, this.pixelGeometry)
    window.matchMedia("(resolution: ${contentScale}dppx)")
        .addEventListener("change", { setOnChangeScaleNotifier() }, true)
    onContentScaleChanged?.invoke(contentScale)
}