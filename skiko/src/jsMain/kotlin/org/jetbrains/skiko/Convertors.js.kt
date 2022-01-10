package org.jetbrains.skiko

import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.events.InputEvent

fun toSkikoEvent(
    event: MouseEvent,
    buttons: Boolean,
    kind: SkikoPointerEventKind
): SkikoPointerEvent {
    return SkikoPointerEvent(
        x = event.offsetX,
        y = event.offsetY,
        buttons = toSkikoMouseButtons(event, buttons),
        modifiers = toSkikoModifiers(event),
        kind = kind,
        timestamp = event.timeStamp.toLong(),
        platform = event
    )
}

fun toSkikoDragEvent(
    event: MouseEvent
): SkikoPointerEvent {
    return SkikoPointerEvent(
        x = event.offsetX,
        y = event.offsetY,
        buttons = toSkikoMouseButtons(event, true),
        modifiers = toSkikoModifiers(event),
        kind = SkikoPointerEventKind.DRAG,
        timestamp = event.timeStamp.toLong(),
        platform = event
    )
}

fun toSkikoEvent(
    event: KeyboardEvent,
    kind: SkikoKeyboardEventKind
): SkikoKeyboardEvent {
    return SkikoKeyboardEvent(
        SkikoKey.valueOf(toSkikoKey(event)),
        toSkikoModifiers(event),
        kind,
        event.timeStamp.toLong(),
        event
    )
}

fun toSkikoScrollEvent(
    event: WheelEvent,
    buttons: Boolean
): SkikoPointerEvent {
    return SkikoPointerEvent(
        x = event.offsetX,
        y = event.offsetY,
        deltaX = event.deltaX,
        deltaY = event.deltaY,
        buttons = toSkikoMouseButtons(event, buttons),
        modifiers = toSkikoModifiers(event),
        kind = SkikoPointerEventKind.SCROLL,
        timestamp = event.timeStamp.toLong(),
        platform = event
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

private fun toSkikoKey(event: KeyboardEvent): Int {
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
    return key
}