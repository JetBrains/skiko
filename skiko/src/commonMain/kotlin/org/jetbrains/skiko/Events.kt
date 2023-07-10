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
    UNKNOWN, TAP, DOUBLETAP, LONGPRESS, PAN, PINCH, ROTATION, SWIPE
}
enum class SkikoGestureEventDirection {
    UNKNOWN, UP, DOWN, LEFT, RIGHT
}
enum class SkikoGestureEventState {
    UNKNOWN, PRESSED, STARTED, CHANGED, ENDED
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

expect class SkikoPlatformInputEvent
data class SkikoInputEvent(
    val input: String,
    val key: SkikoKey,
    val modifiers: SkikoInputModifiers = SkikoInputModifiers.EMPTY,
    val kind: SkikoKeyboardEventKind,
    val platform: SkikoPlatformInputEvent?
)

enum class SkikoKeyboardEventKind {
    UNKNOWN, UP, DOWN, TYPE
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
    UNKNOWN, UP, DOWN, MOVE, DRAG, SCROLL, ENTER, EXIT
}

expect class SkikoPlatformPointerEvent

// TODO(https://github.com/JetBrains/skiko/issues/680) refactor API
data class SkikoPointerEvent(
    /**
     * X position in points (scaled pixels that depend on the scale factor of the current display).
     *
     * If the event contains multiple pointers, it represents the center of all pointers.
     */
    val x: Double,
    /**
     * Y position in points (scaled pixels that depend on the scale factor of the current display)
     *
     * If the event contains multiple pointers, it represents the center of all pointers.
     */
    val y: Double,
    val kind: SkikoPointerEventKind,
    /**
     * Scroll delta along the X axis
     */
    val deltaX: Double = 0.0,
    /**
     * Scroll delta along the Y axis
     */
    val deltaY: Double = 0.0,
    val pressedButtons: SkikoMouseButtons = SkikoMouseButtons.NONE,
    val button: SkikoMouseButtons = SkikoMouseButtons.NONE,
    val modifiers: SkikoInputModifiers = SkikoInputModifiers.EMPTY,
    /**
     * Timestamp in milliseconds
     */
    val timestamp: Long = 0,
    val pointers: List<SkikoPointer> = listOf(
        SkikoPointer(0, x, y, pressedButtons.has(SkikoMouseButtons.LEFT))
    ),
    val platform: SkikoPlatformPointerEvent? = null
)

val SkikoPointerEvent.isLeftClick: Boolean
    get() = button.has(SkikoMouseButtons.LEFT) && (kind == SkikoPointerEventKind.UP)

val SkikoPointerEvent.isRightClick: Boolean
    get() = button.has(SkikoMouseButtons.RIGHT) && (kind == SkikoPointerEventKind.UP)

val SkikoPointerEvent.isMiddleClick: Boolean
    get() = button.has(SkikoMouseButtons.MIDDLE) && (kind == SkikoPointerEventKind.UP)

/**
 * The device type that produces pointer events, such as a mouse or stylus.
 */
enum class SkikoPointerDevice {
    UNKNOWN, MOUSE, TOUCH
}

/**
 * Represents pointer such as mouse cursor, or touch/stylus press.
 * There can be multiple pointers on the screen at the same time.
 */
data class SkikoPointer(
    /**
     * Unique id associated with the pointer. Used to distinguish between multiple pointers that can exist
     * at the same time (i.e. multiple pressed touches).
     *
     * If there is only on pointer in the system (for example, one mouse), it should always
     * have the same id across multiple events.
     */
    val id: Long,

    /**
     * X position in points (scaled pixels that depend on the scale factor of the current display)
     */
    val x: Double,
    /**
     * Y position in points (scaled pixels that depend on the scale factor of the current display)
     */
    val y: Double,

    /**
     * `true` if the pointer event is considered "pressed." For example, finger
     *  touching the screen or a mouse button is pressed [pressed] would be `true`.
     *  During the up event, pointer is considered not pressed.
     */
    val pressed: Boolean,

    /**
     * The device type associated with the pointer, such as [mouse][SkikoPointerDevice.MOUSE],
     * or [touch][SkikoPointerDevice.TOUCH].
     */
    val device: SkikoPointerDevice = SkikoPointerDevice.MOUSE,

    /**
     * Pressure of the pointer. 0.0 - no pressure, 1.0 - average pressure
     */
    val pressure: Double = 1.0,
)

@InternalSkikoApi
val Iterable<SkikoPointer>.centroidX get() = asSequence().map { it.x }.average()

@InternalSkikoApi
val Iterable<SkikoPointer>.centroidY get() = asSequence().map { it.y }.average()
