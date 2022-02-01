package org.jetbrains.skiko

import kotlinx.cinterop.useContents
import platform.AppKit.*

fun toSkikoEvent(
    event: NSEvent,
    kind: SkikoPointerEventKind,
    view: NSView
): SkikoPointerEvent {
    var (xpos, ypos) = event.locationInWindow.useContents {
        x to y
    }
    // Translate.
    view.frame.useContents {
       ypos = size.height - ypos
    }
    val timestamp = (event.timestamp * 1_000).toLong()
    return SkikoPointerEvent(
        x = xpos,
        y = ypos,
        pressedButtons = toSkikoPressedMouseButtons(event, kind),
        button = toSkikoMouseButton(event),
        modifiers = toSkikoModifiers(event),
        kind = kind,
        timestamp = timestamp,
        platform = event
    )
}

fun toSkikoEvent(
    event: NSEvent,
    button: SkikoMouseButtons,
    kind: SkikoPointerEventKind,
    view: NSView
): SkikoPointerEvent {
    var (xpos, ypos) = event.locationInWindow.useContents {
        x to y
    }
    // Translate.
    view.frame.useContents {
       ypos = size.height - ypos
    }
    val timestamp = (event.timestamp * 1_000).toLong()
    var buttons: SkikoMouseButtons
    if (kind == SkikoPointerEventKind.DOWN) {
        buttonsFlags = buttonsFlags.or(button.value)
    } else {
        buttonsFlags = buttonsFlags.xor(button.value)
    }
    buttons = SkikoMouseButtons(buttonsFlags)
    return SkikoPointerEvent(
        x = xpos,
        y = ypos,
        pressedButtons = buttons,
        button = button,
        modifiers = toSkikoModifiers(event),
        kind = kind,
        timestamp = timestamp,
        platform = event
    )
}

fun toSkikoScrollEvent(
    event: NSEvent,
    view: NSView
): SkikoPointerEvent {
    var (xpos, ypos) = event.locationInWindow.useContents {
        x to y
    }
    // Translate.
    view.frame.useContents {
       ypos = size.height - ypos
    }
    val timestamp = (event.timestamp * 1_000).toLong()
    return SkikoPointerEvent(
        x = xpos,
        y = ypos,
        deltaX = event.deltaX,
        deltaY = event.deltaY,
        pressedButtons = SkikoMouseButtons(buttonsFlags),
        button = SkikoMouseButtons.NONE,
        modifiers = toSkikoModifiers(event),
        kind = SkikoPointerEventKind.SCROLL,
        timestamp = timestamp,
        platform = event
    )
}

fun toSkikoEvent(
    event: NSEvent,
    kind: SkikoKeyboardEventKind
): SkikoKeyboardEvent {
    val timestamp = (event.timestamp * 1_000).toLong()
    return SkikoKeyboardEvent(
        SkikoKey.valueOf(event.keyCode.toInt()),
        toSkikoModifiers(event),
        kind,
        timestamp,
        event
    )
}

fun toSkikoTypeEvent(
    character: String,
    event: NSEvent?,
): SkikoInputEvent {
    val key = if (event != null) SkikoKey.valueOf(event.keyCode.toInt()) else SkikoKey.KEY_UNKNOWN
    val modifiers = if (event != null) toSkikoModifiers(event) else SkikoInputModifiers.EMPTY
    return SkikoInputEvent(
        character,
        key,
        modifiers,
        SkikoKeyboardEventKind.TYPE,
        event
    )
}

private val actionAllKeyUpFlag = 256.toULong() // MacOS specific
private var modifierKeyUpFlag = actionAllKeyUpFlag
fun toSkikoEvent(
    event: NSEvent
): SkikoKeyboardEvent {
    // NSView.keyDown(NSEvent)/keyUp(NSEvent) does not work with modifier keys,
    // so we need to use NSView.flagsChanged(NSEvent)
    // and convert modifier key event to regular key event.
    // Unfortunately, NSEvent does not give us any information about the type of action,
    // so we need to check it manually.
    val action = event.modifierFlags and NSEventModifierFlagDeviceIndependentFlagsMask.inv()
    var kind = SkikoKeyboardEventKind.DOWN
    val key = SkikoKey.valueOf(event.keyCode.toInt())
    if (action >= modifierKeyUpFlag) {
        kind = SkikoKeyboardEventKind.DOWN
    } else {
        kind = SkikoKeyboardEventKind.UP
    }
    modifierKeyUpFlag = action
    if (modifierKeyUpFlag == actionAllKeyUpFlag) {
        kind = SkikoKeyboardEventKind.UP
    }
    val timestamp = (event.timestamp * 1_000).toLong()
    return SkikoKeyboardEvent(
        SkikoKey.valueOf(event.keyCode.toInt()),
        toSkikoModifiers(event),
        kind,
        timestamp,
        event
    )
}

private var buttonsFlags = 0
private fun toSkikoPressedMouseButtons(
    event: NSEvent,
    kind: SkikoPointerEventKind
): SkikoMouseButtons {
    val button = event.buttonNumber.toInt()
    if (kind == SkikoPointerEventKind.DOWN) {
        buttonsFlags = buttonsFlags.or(getSkikoButtonValue(button))
        return SkikoMouseButtons(buttonsFlags)
    }
    buttonsFlags = buttonsFlags.xor(getSkikoButtonValue(button))
    return SkikoMouseButtons(buttonsFlags)
}

private fun toSkikoMouseButton(event: NSEvent): SkikoMouseButtons {
    return SkikoMouseButtons(getSkikoButtonValue(event.buttonNumber.toInt()))
}

private fun getSkikoButtonValue(button: Int): Int {
    return when (button) {
        2 -> SkikoMouseButtons.MIDDLE.value
        3 -> SkikoMouseButtons.BUTTON_4.value
        4 -> SkikoMouseButtons.BUTTON_5.value
        5 -> SkikoMouseButtons.BUTTON_6.value
        6 -> SkikoMouseButtons.BUTTON_7.value
        7 -> SkikoMouseButtons.BUTTON_8.value
        else -> 0
    }
}

private fun toSkikoModifiers(event: NSEvent): SkikoInputModifiers {
    var result = 0
    val modifiers = event.modifierFlags
    if (modifiers and NSEventModifierFlagOption != 0.toULong()) {
        result = result.or(SkikoInputModifiers.ALT.value)
    }
    if (modifiers and NSEventModifierFlagShift != 0.toULong()) {
        result = result.or(SkikoInputModifiers.SHIFT.value)
    }
    if (modifiers and NSEventModifierFlagControl != 0.toULong()) {
        result = result.or(SkikoInputModifiers.CONTROL.value)
    }
    if (modifiers and NSEventModifierFlagCommand != 0.toULong()) {
        result = result.or(SkikoInputModifiers.META.value)
    }
    return SkikoInputModifiers(result)
}

private fun keyToModifier(key: SkikoKey): ULong {
    return when (key) {
        SkikoKey.KEY_LEFT_ALT,
        SkikoKey.KEY_RIGHT_ALT -> {
            NSEventModifierFlagOption
        }
        SkikoKey.KEY_LEFT_SHIFT,
        SkikoKey.KEY_RIGHT_SHIFT -> {
            NSEventModifierFlagShift
        }
        SkikoKey.KEY_LEFT_CONTROL,
        SkikoKey.KEY_RIGHT_CONTROL -> {
            NSEventModifierFlagControl
        }
        SkikoKey.KEY_LEFT_META,
        SkikoKey.KEY_RIGHT_META -> {
            NSEventModifierFlagCommand
        }
        else -> 0.toULong()
    }
} 