package org.jetbrains.skiko

import platform.UIKit.*

fun toSkikoGestureDirection(direction: UISwipeGestureRecognizerDirection) : SkikoGestureEventDirection {
    return when(direction) {
        UISwipeGestureRecognizerDirectionUp -> SkikoGestureEventDirection.UP
        UISwipeGestureRecognizerDirectionDown -> SkikoGestureEventDirection.DOWN
        UISwipeGestureRecognizerDirectionLeft -> SkikoGestureEventDirection.LEFT
        UISwipeGestureRecognizerDirectionRight -> SkikoGestureEventDirection.RIGHT
        else -> SkikoGestureEventDirection.UNKNOWN
    }
}

fun toSkikoGestureState(state: UIGestureRecognizerState ) : SkikoGestureEventState  {
    return when(state) {
        UIGestureRecognizerStatePossible -> SkikoGestureEventState.PRESSED
        UIGestureRecognizerStateBegan -> SkikoGestureEventState.STARTED
        UIGestureRecognizerStateChanged -> SkikoGestureEventState.CHANGED
        UIGestureRecognizerStateEnded -> SkikoGestureEventState.ENDED
        else -> SkikoGestureEventState.UNKNOWN
    }
}

fun toSkikoTypeEvent(character: String): SkikoInputEvent {
    return SkikoInputEvent(
        character,
        SkikoKeyboardEventKind.TYPE,
        UIEvent()
    )
}

fun toSkikoKeyboardEvent(
    event: UIPress,
    kind: SkikoKeyboardEventKind
): SkikoKeyboardEvent {
    return SkikoKeyboardEvent(
        SkikoKey.valueOf(event.key!!.keyCode),
        toSkikoModifiers(event),
        kind,
        event
    )
}

private fun toSkikoModifiers(event: UIPress): SkikoInputModifiers {
    var result = 0
    val modifiers = event.key!!.modifierFlags
    if (modifiers and UIKeyModifierAlternate != 0L) {
        result = result.or(SkikoInputModifiers.ALT.value)
    }
    if (modifiers and UIKeyModifierShift != 0L) {
        result = result.or(SkikoInputModifiers.SHIFT.value)
    }
    if (modifiers and UIKeyModifierControl != 0L) {
        result = result.or(SkikoInputModifiers.CONTROL.value)
    }
    if (modifiers and UIKeyModifierCommand != 0L) {
        result = result.or(SkikoInputModifiers.META.value)
    }
    return SkikoInputModifiers(result)
}
