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

fun toSkikoPointerEvent(event: MotionEvent, density: Float): SkikoPointerEvent {
    val upIndex = when (event.action) {
        MotionEvent.ACTION_UP -> 0
        MotionEvent.ACTION_POINTER_UP -> event.actionIndex
        else -> -1
    }

    val pointers = (0 until event.pointerCount).map {
        SkikoPointer(
            x = event.getX(it).toDouble() / density,
            y = event.getY(it).toDouble() / density,
            // Same as in Jetpack Compose for Android
            // https://github.com/androidx/androidx/blob/58597f0eba31b89f57b6605b7ed4977cd48ed38d/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/input/pointer/MotionEventAdapter.android.kt#L126
            // (we don't support Mouse for Android yet, so we don't check hover)
            pressed = it != upIndex,
            device = SkikoPointerDevice.TOUCH,
            id = event.getPointerId(it).toLong(),
            pressure = event.getPressure(it).toDouble()
        )
    }

    val x = pointers.asSequence().map { it.x }.average()
    val y = pointers.asSequence().map { it.y }.average()
    return SkikoPointerEvent(
        x = x,
        y = y,
        kind = toSkikoPointerEventKind(event),
        deltaX = 0.0,
        deltaY = 0.0,
        timestamp = event.eventTime,
        pointers = pointers,
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

internal fun toSkikoPointerEventKind(event: MotionEvent): SkikoPointerEventKind {
    return when (event.action) {
        MotionEvent.ACTION_POINTER_DOWN,
        MotionEvent.ACTION_DOWN -> SkikoPointerEventKind.DOWN
        MotionEvent.ACTION_POINTER_UP,
        MotionEvent.ACTION_UP -> SkikoPointerEventKind.UP
        MotionEvent.ACTION_MOVE -> SkikoPointerEventKind.MOVE
        else -> SkikoPointerEventKind.UNKNOWN
    }
}

private fun toSkikoGestureEventKind(event: MotionEvent): SkikoGestureEventKind {
    return when (event.action) {
        MotionEvent.ACTION_UP -> SkikoGestureEventKind.TAP
        MotionEvent.ACTION_MOVE -> SkikoGestureEventKind.PAN
        else -> SkikoGestureEventKind.UNKNOWN
    }
}