package org.jetbrains.skiko

import org.w3c.dom.TouchEvent
import org.w3c.dom.asList
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

internal fun toSkikoEvent(
    event: MouseEvent,
    kind: SkikoPointerEventKind
): SkikoPointerEvent {
    return SkikoPointerEvent(
        x = event.offsetX,
        y = event.offsetY,
        pressedButtons = toSkikoPressedMouseButtons(event, kind),
        button = toSkikoMouseButton(event),
        modifiers = toSkikoModifiers(event),
        kind = kind,
        timestamp = getEventTimestamp(event),
        platform = event
    )
}

internal fun toSkikoDragEvent(
    event: MouseEvent
): SkikoPointerEvent {
    return SkikoPointerEvent(
        x = event.offsetX,
        y = event.offsetY,
        pressedButtons = SkikoMouseButtons(buttonsFlags),
        button = toSkikoMouseButton(event),
        modifiers = toSkikoModifiers(event),
        kind = SkikoPointerEventKind.DRAG,
        timestamp = getEventTimestamp(event),
        platform = event
    )
}

internal fun toSkikoEvent(
    event: TouchEvent,
    kind: SkikoPointerEventKind,
    offsetX: Double,
    offsetY: Double
): SkikoPointerEvent {
    val touches = event.changedTouches.asList()
    val pointers = touches.map { touch ->
        val x = touch.clientX.toDouble() - offsetX
        val y = touch.clientY.toDouble() - offsetY
        val force = touch.asDynamic().force as Double

        SkikoPointer(
            x = x,
            y = y,
            pressed = kind in listOf(SkikoPointerEventKind.DOWN, SkikoPointerEventKind.MOVE),
            device = SkikoPointerDevice.TOUCH,
            id = touch.identifier.toLong(),
            pressure = force
        )
    }

    return SkikoPointerEvent(
        x = pointers.centroidX,
        y = pointers.centroidY,
        kind = kind,
        timestamp = (currentNanoTime() / 1E6).toLong(),
        pointers = pointers,
        platform = event
    )
}

internal fun toSkikoTypeEvent(
    character: String,
    event: KeyboardEvent?,
): SkikoInputEvent? {
    return if (SPECIAL_KEYS.contains(character)) {
        null
    } else {
        val input = when (character) {
            "Enter" -> "\n"
            "Tab" -> "\t"
            else -> character
        }
        val key = if (event != null) SkikoKey.valueOf(event.keyCode) else SkikoKey.KEY_UNKNOWN
        val modifiers = if  (event != null) toSkikoModifiers(event) else SkikoInputModifiers.EMPTY
        SkikoInputEvent(
            input,
            key,
            modifiers,
            SkikoKeyboardEventKind.TYPE,
            event
        )
    }
}


internal fun toSkikoEvent(
    event: KeyboardEvent,
    kind: SkikoKeyboardEventKind
): SkikoKeyboardEvent {
    return SkikoKeyboardEvent(
        SkikoKey.valueOf(toSkikoKey(event)),
        toSkikoModifiers(event),
        kind,
        getEventTimestamp(event),
        event
    )
}

internal fun toSkikoScrollEvent(
    event: WheelEvent,
): SkikoPointerEvent {
    return SkikoPointerEvent(
        x = event.offsetX,
        y = event.offsetY,
        deltaX = event.deltaX,
        deltaY = event.deltaY,
        pressedButtons = SkikoMouseButtons(buttonsFlags),
        button = SkikoMouseButtons.NONE,
        modifiers = toSkikoModifiers(event),
        kind = SkikoPointerEventKind.SCROLL,
        timestamp = getEventTimestamp(event),
        platform = event
    )
}

private var buttonsFlags = 0
private fun toSkikoPressedMouseButtons(
    event: MouseEvent,
    kind: SkikoPointerEventKind
): SkikoMouseButtons {
    // https://www.w3schools.com/jsref/event_button.asp
    val button = event.button.toInt()
    if (kind == SkikoPointerEventKind.DOWN) {
        buttonsFlags = buttonsFlags.or(getSkikoButtonValue(button))
        return SkikoMouseButtons(buttonsFlags)
    }
    buttonsFlags = buttonsFlags.xor(getSkikoButtonValue(button))
    return SkikoMouseButtons(buttonsFlags)
}

private fun toSkikoMouseButton(event: MouseEvent): SkikoMouseButtons {
    return SkikoMouseButtons(getSkikoButtonValue(event.button.toInt()))
}

private fun getSkikoButtonValue(button: Int): Int {
    return when (button) {
        0 -> SkikoMouseButtons.LEFT.value
        1 -> SkikoMouseButtons.MIDDLE.value
        2 -> SkikoMouseButtons.RIGHT.value
        3 -> SkikoMouseButtons.BUTTON_4.value
        4 -> SkikoMouseButtons.BUTTON_5.value
        else -> 0
    }
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
            key == SkikoKey.KEY_LEFT_CONTROL.platformKeyCode ||
            key == SkikoKey.KEY_LEFT_SHIFT.platformKeyCode ||
            key == SkikoKey.KEY_LEFT_META.platformKeyCode
        )
            key = key.or(0x80000000.toInt())
    }
    return key
}