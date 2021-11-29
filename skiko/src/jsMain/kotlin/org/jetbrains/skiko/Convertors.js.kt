package org.jetbrains.skiko

import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.WheelEvent

fun toSkikoEvent(
    event: MouseEvent,
    buttons: Boolean,
    kind: SkikoPointerEventKind
): SkikoPointerEvent {
    return SkikoPointerEvent(
        event.offsetX,
        event.offsetY,
        toSkikoMouseButtons(event, buttons),
        toSkikoModifiers(event),
        kind,
        event
    )
}

fun toSkikoDragEvent(
    event: MouseEvent
): SkikoPointerEvent {
    return SkikoPointerEvent(
        event.offsetX,
        event.offsetY,
        toSkikoMouseButtons(event, true),
        toSkikoModifiers(event),
        SkikoPointerEventKind.DRAG,
        event
    )
}

fun toSkikoEvent(
    event: KeyboardEvent,
    kind: SkikoKeyboardEventKind
): SkikoKeyboardEvent {
    var key = event.keyCode
    val side = event.location
    if (side == KeyboardEvent.DOM_KEY_LOCATION_RIGHT) {
        if (
            key == SkikoKey.KEY_LEFT_CONTROL.value ||
            key == SkikoKey.KEY_LEFT_SHIFT.value ||
            key == SkikoKey.KEY_LEFT_META.value
        )
        key = key.or(0x80000000.toInt())
    }
    return SkikoKeyboardEvent(
        SkikoKey.valueOf(key),
        toSkikoModifiers(event),
        kind,
        event
    )
}

fun toSkikoScrollEvent(
    event: WheelEvent,
    buttons: Boolean
): SkikoPointerEvent {
    return SkikoPointerEvent(
        event.deltaX,
        event.deltaY,
        toSkikoMouseButtons(event, buttons),
        toSkikoModifiers(event),
        SkikoPointerEventKind.SCROLL,
        event
    )
}

private fun toSkikoMouseButtons(
    event: MouseEvent,
    pressed: Boolean
): SkikoMouseButtons {
    // https://www.w3schools.com/jsref/event_button.asp
    var result = 0
    if (pressed && event.button.toInt() == 0) {
        result = result.or(SkikoMouseButtons.LEFT.value)
    }
    if (pressed && event.button.toInt() == 2) {
        result = result.or(SkikoMouseButtons.RIGHT.value)
    }
    if (pressed && event.button.toInt() == 1) {
        result = result.or(SkikoMouseButtons.MIDDLE.value)
    }
    return SkikoMouseButtons(result)
}

private fun toSkikoModifiers(event: MouseEvent): SkikoInputModifiers {
    var result = 0
    if (event.altKey) {
        result = result.or(SkikoInputModifiers.ALT.value)
    }
    if (event.shiftKey) {
        result = result.or(SkikoInputModifiers.SHIFT.value)
    }
    if (event.ctrlKey) {
        result = result.or(SkikoInputModifiers.CONTROL.value)
    }
    if (event.metaKey) {
        result = result.or(SkikoInputModifiers.META.value)
    }
    return SkikoInputModifiers(result)
}

private fun toSkikoModifiers(event: KeyboardEvent): SkikoInputModifiers {
    var result = 0
    if (event.altKey) {
        result = result.or(SkikoInputModifiers.ALT.value)
    }
    if (event.shiftKey) {
        result = result.or(SkikoInputModifiers.SHIFT.value)
    }
    if (event.ctrlKey) {
        result = result.or(SkikoInputModifiers.CONTROL.value)
    }
    if (event.metaKey) {
        result = result.or(SkikoInputModifiers.META.value)
    }
    return SkikoInputModifiers(result)
}