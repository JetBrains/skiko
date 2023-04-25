package org.jetbrains.skiko

import org.w3c.dom.events.*

private val SPECIAL_KEYS = setOf(
    "Unidentified",
    "Alt",
    "AltGraph",
    "Backspace",
    "CapsLock",
    "Control",
    "Fn",
    "FnLock",
    "Hyper",
    "Meta",
    "NumLock",
    "ScrollLock",
    "Shift",
    "Super",
    "Symbol",
    "SymbolLock",
    "F1",
    "F2",
    "F3",
    "F4",
    "F5",
    "F6",
    "F7",
    "F8",
    "F9",
    "F10",
    "F11",
    "F12",
    "F13",
    "F14",
    "F15",
    "F16",
    "F17",
    "F18",
    "F19",
    "F20",
    "F21",
    "F22",
    "ArrowLeft",
    "ArrowUp",
    "ArrowRight",
    "ArrowDown",
    "Help",
    "Home",
    "Delete",
    "End",
    "PageUp",
    "PageDown",
    "Escape",
    "Clear",
    "Clear"
)

internal expect fun getEventTimestamp(e: UIEvent): Long

fun toSkikoEvent(
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

fun toSkikoDragEvent(
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

fun toSkikoTypeEvent(
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


fun toSkikoEvent(
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

fun toSkikoScrollEvent(
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