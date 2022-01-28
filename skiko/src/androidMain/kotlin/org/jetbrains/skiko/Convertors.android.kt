package org.jetbrains.skiko

import android.view.MotionEvent
import android.view.KeyEvent
import kotlin.math.abs

fun toSkikoKeyboardEvent(
    event: KeyEvent,
    keyCode: Int,
    kind: SkikoKeyboardEventKind
): SkikoKeyboardEvent {
    return SkikoKeyboardEvent(
        key = SkikoKey.valueOf(keyCode),
        modifiers = toSkikoModifiers(event),
        kind = kind,
        timestamp = event.getEventTime(),
        platform = event
    )
}

fun toSkikoTypeEvent(
    event: KeyEvent,
    keyCode: Int
): SkikoInputEvent {
    return SkikoInputEvent(
        input = event.unicodeChar.toChar().toString(),
        key = SkikoKey.valueOf(keyCode),
        modifiers = toSkikoModifiers(event),
        SkikoKeyboardEventKind.TYPE,
        platform = event
    )
}

private fun toSkikoModifiers(event: KeyEvent): SkikoInputModifiers {
    var result = 0
    if (event.isAltPressed) {
        result = result.or(SkikoInputModifiers.ALT.value)
    }
    if (event.isShiftPressed) {
        result = result.or(SkikoInputModifiers.SHIFT.value)
    }
    if (event.isCtrlPressed) {
        result = result.or(SkikoInputModifiers.CONTROL.value)
    }
    if (event.isMetaPressed) {
        result = result.or(SkikoInputModifiers.META.value)
    }
    return SkikoInputModifiers(result)
}

fun toSkikoTouchEvent(event: MotionEvent, index: Int, density: Float): SkikoTouchEvent {
    return SkikoTouchEvent(
        x = (event.getX(index) / density).toDouble(),
        y = (event.getY(index) / density).toDouble(),
        timestamp = event.getEventTime(),
        kind = toSkikoTouchEventKind(event),
        platform = event
    )
}

fun toSkikoGestureEvent(event: MotionEvent, density: Float): SkikoGestureEvent {
    return SkikoGestureEvent(
        x = (event.x / density).toDouble(),
        y = (event.y / density).toDouble(),
        kind = toSkikoGestureEventKind(event),
        platform = event
    )
}

fun toSkikoScaleGestureEvent(
    event: MotionEvent,
    scale: Double,
    state: SkikoGestureEventState,
    density: Float
): SkikoGestureEvent {
    return SkikoGestureEvent(
        x = (event.x / density).toDouble(),
        y = (event.y / density).toDouble(),
        scale = scale,
        kind = SkikoGestureEventKind.PINCH,
        state = state,
        platform = event
    )
}

private val swipeThreshold = 100
private val swipeVelocityThreshold = 100

internal fun toSkikoGestureDirection(
    event1: MotionEvent, 
    event2: MotionEvent, 
    velocityX: Float, 
    velocityY: Float
): SkikoGestureEventDirection {
    val dx = event2.x - event1.x
    val dy = event2.y - event1.y
    if (abs(dx) > abs(dy)) {
        if (abs(dx) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
            if (dx > 0) {
                return SkikoGestureEventDirection.RIGHT
            } else {
                return SkikoGestureEventDirection.LEFT
            }
        }
    } else {
        if (abs(dy) > swipeThreshold && abs(velocityY) > swipeVelocityThreshold) {
            if (dy > 0) {
                return SkikoGestureEventDirection.DOWN
            } else {
                return SkikoGestureEventDirection.UP
            }
        }
    }
    return SkikoGestureEventDirection.UNKNOWN
}

private fun toSkikoTouchEventKind(event: MotionEvent): SkikoTouchEventKind {
    return when (event.action) {
        MotionEvent.ACTION_POINTER_DOWN,
        MotionEvent.ACTION_DOWN -> SkikoTouchEventKind.STARTED
        MotionEvent.ACTION_POINTER_UP,
        MotionEvent.ACTION_UP -> SkikoTouchEventKind.ENDED
        MotionEvent.ACTION_MOVE -> SkikoTouchEventKind.MOVED
        else -> SkikoTouchEventKind.UNKNOWN
    }
}

private fun toSkikoGestureEventKind(event: MotionEvent): SkikoGestureEventKind {
    return when (event.action) {
        MotionEvent.ACTION_UP -> SkikoGestureEventKind.TAP
        MotionEvent.ACTION_MOVE -> SkikoGestureEventKind.PAN
        else -> SkikoGestureEventKind.UNKNOWN
    }
}