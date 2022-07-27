package org.jetbrains.skiko

import kotlin.jvm.JvmInline

// Clicked mouse buttons bitmask.
@JvmInline
value class SkikoMouseButtons(val value: Int) {
    companion object {
        val NONE = SkikoMouseButtons(0)
        val LEFT = SkikoMouseButtons(1)
        val RIGHT = SkikoMouseButtons(2)
        val MIDDLE = SkikoMouseButtons(4)
        val BUTTON_1 = SkikoMouseButtons(1)
        val BUTTON_2 = SkikoMouseButtons(2)
        val BUTTON_3 = SkikoMouseButtons(4)
        val BUTTON_4 = SkikoMouseButtons(8)
        val BUTTON_5 = SkikoMouseButtons(16)
        val BUTTON_6 = SkikoMouseButtons(32)
        val BUTTON_7 = SkikoMouseButtons(64)
        val BUTTON_8 = SkikoMouseButtons(128)
    }

    fun has(value: SkikoMouseButtons): Boolean {
        if (value.value and this.value != 0) {
            return true
        }
        return false
    }

    override fun toString(): String {
        val result = mutableListOf<String>().apply {
            if (has(SkikoMouseButtons.LEFT)) {
                add("LEFT")
            }
            if (has(SkikoMouseButtons.RIGHT)) {
                add("RIGHT")
            }
            if (has(SkikoMouseButtons.MIDDLE)) {
                add("MIDDLE")
            }
            if (has(SkikoMouseButtons.BUTTON_4)) {
                add("BUTTON_4")
            }
            if (has(SkikoMouseButtons.BUTTON_5)) {
                add("BUTTON_5")
            }
            if (has(SkikoMouseButtons.BUTTON_6)) {
                add("BUTTON_6")
            }
            if (has(SkikoMouseButtons.BUTTON_7)) {
                add("BUTTON_7")
            }
            if (has(SkikoMouseButtons.BUTTON_8)) {
                add("BUTTON_8")
            }
        }
        return if (!result.isEmpty()) result.toString() else ""
    }
}

@JvmInline
value class SkikoInputModifiers(val value: Int) {
    companion object {
        val EMPTY = SkikoInputModifiers(0)
        val META = SkikoInputModifiers(1)
        val CONTROL = SkikoInputModifiers(2)
        val ALT = SkikoInputModifiers(4)
        val SHIFT = SkikoInputModifiers(8)
    }

    fun has(value: SkikoInputModifiers): Boolean {
        if (value.value and this.value != 0) {
            return true
        }
        return false
    }

    override fun toString(): String {
        val result = mutableListOf<String>().apply {
            if (has(SkikoInputModifiers.META)) {
                add("META")
            }
            if (has(SkikoInputModifiers.CONTROL)) {
                add("CONTROL")
            }
            if (has(SkikoInputModifiers.ALT)) {
                add("ALT")
            }
            if (has(SkikoInputModifiers.SHIFT)) {
                add("SHIFT")
            }
        }
        return if (!result.isEmpty()) result.toString() else ""
    }
}

enum class SkikoGestureEventKind {
    TAP, DOUBLETAP, LONGPRESS, PAN, PINCH, ROTATION, SWIPE, UNKNOWN
}
enum class SkikoGestureEventDirection {
    UP, DOWN, LEFT, RIGHT, UNKNOWN
}
enum class SkikoGestureEventState {
    PRESSED, STARTED, CHANGED, ENDED, UNKNOWN
}
expect class SkikoGesturePlatformEvent
data class SkikoGestureEvent(
    val x: Double,
    val y: Double,
    val velocity: Double = 0.0,
    val direction: SkikoGestureEventDirection = SkikoGestureEventDirection.UNKNOWN,
    val rotation: Double = 0.0,
    val scale: Double = 1.0,
    val kind: SkikoGestureEventKind,
    val state: SkikoGestureEventState = SkikoGestureEventState.UNKNOWN,
    val platform: SkikoGesturePlatformEvent? = null
)

enum class SkikoTouchEventKind {
    STARTED, ENDED, MOVED, CANCELLED, UNKNOWN
}
expect class SkikoTouchPlatformEvent
data class SkikoTouchEvent(
    val x: Double,
    val y: Double,
    val kind: SkikoTouchEventKind = SkikoTouchEventKind.UNKNOWN,
    val timestamp: Long = 0,
    val platform: SkikoTouchPlatformEvent? = null
)

expect class SkikoPlatformInputEvent
data class SkikoInputEvent(
    val input: String,
    val key: SkikoKey,
    val modifiers: SkikoInputModifiers = SkikoInputModifiers.EMPTY,
    val kind: SkikoKeyboardEventKind,
    val platform: SkikoPlatformInputEvent?
)

enum class SkikoKeyboardEventKind {
    UP, DOWN, TYPE, UNKNOWN
}
expect class SkikoPlatformKeyboardEvent
data class SkikoKeyboardEvent(
    val key: SkikoKey,
    val modifiers: SkikoInputModifiers = SkikoInputModifiers.EMPTY,
    val kind: SkikoKeyboardEventKind,
    val timestamp: Long = 0,
    val platform: SkikoPlatformKeyboardEvent?
)

enum class SkikoPointerEventKind {
    UP, DOWN, MOVE, DRAG, SCROLL, ENTER, EXIT, UNKNOWN
}

expect class SkikoPlatformPointerEvent
data class SkikoPointerEvent(
    val x: Double,
    val y: Double,
    val deltaX: Double = 0.0,
    val deltaY: Double = 0.0,
    val pressedButtons: SkikoMouseButtons = SkikoMouseButtons.NONE,
    val button: SkikoMouseButtons = SkikoMouseButtons.NONE,
    val modifiers: SkikoInputModifiers = SkikoInputModifiers.EMPTY,
    val kind: SkikoPointerEventKind,
    val timestamp: Long = 0,
    val platform: SkikoPlatformPointerEvent?
)

val SkikoPointerEvent.isLeftClick: Boolean
    get() = button.has(SkikoMouseButtons.LEFT) && (kind == SkikoPointerEventKind.UP)

val SkikoPointerEvent.isRightClick: Boolean
    get() = button.has(SkikoMouseButtons.RIGHT) && (kind == SkikoPointerEventKind.UP)

val SkikoPointerEvent.isMiddleClick: Boolean
    get() = button.has(SkikoMouseButtons.MIDDLE) && (kind == SkikoPointerEventKind.UP)

