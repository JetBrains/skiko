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
    return SkikoPointerEvent(
        xpos,
        ypos,
        toSkikoMouseButtons(event),
        toSkikoModifiers(event),
        kind,
        event
    )
}

fun toSkikoEvent(
    event: NSEvent,
    kind: SkikoKeyboardEventKind
): SkikoKeyboardEvent {
    return SkikoKeyboardEvent(
        event.keyCode.toInt(),
        toSkikoModifiers(event),
        kind,
        event
    )
}

private fun toSkikoMouseButtons(event: NSEvent): SkikoMouseButtons {
    var result = 0
    val mask = event.buttonMask.toInt()
    if ((mask and 1) != 0 || event.buttonNumber == 0L) {
        result = result.or(SkikoMouseButtons.LEFT.value)
    }
    if ((mask and 2) != 0 || event.buttonNumber == 1L) {
        result = result.or(SkikoMouseButtons.RIGHT.value)
    }
    return SkikoMouseButtons(result)
}

private fun toSkikoModifiers(event: NSEvent): SkikoInputModifiers {
    return SkikoInputModifiers.EMPTY
}