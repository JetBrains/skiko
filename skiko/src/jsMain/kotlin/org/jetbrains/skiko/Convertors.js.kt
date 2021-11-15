package org.jetbrains.skiko

import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.KeyboardEvent

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

fun toSkikoEvent(
    event: KeyboardEvent,
    kind: SkikoKeyboardEventKind
): SkikoKeyboardEvent {
    return SkikoKeyboardEvent(
        event.keyCode,
        toSkikoModifiers(event),
        kind,
        event
    )
}

fun toSkikoMouseButtons(event: MouseEvent, pressed: Boolean): SkikoMouseButtons {
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

fun toSkikoModifiers(event: MouseEvent): SkikoInputModifiers {
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

fun toSkikoModifiers(event: KeyboardEvent): SkikoInputModifiers {
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