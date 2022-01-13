package org.jetbrains.skiko

// Clicked mouse buttons bitmask.
inline class SkikoMouseButtons(val value: Int) {
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

inline class SkikoInputModifiers(val value: Int) {
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

expect enum class SkikoKey {
    KEY_UNKNOWN,
    KEY_A,
    KEY_S,
    KEY_D,
    KEY_F,
    KEY_H,
    KEY_G,
    KEY_Z,
    KEY_X,
    KEY_C,
    KEY_V,
    KEY_B,
    KEY_Q,
    KEY_W,
    KEY_E,
    KEY_R,
    KEY_Y,
    KEY_T,
    KEY_U,
    KEY_I,
    KEY_P,
    KEY_L,
    KEY_J,
    KEY_K,
    KEY_N,
    KEY_M,
    KEY_O,
    KEY_1,
    KEY_2,
    KEY_3,
    KEY_4,
    KEY_5,
    KEY_6,
    KEY_7,
    KEY_8,
    KEY_9,
    KEY_0,
    KEY_CLOSE_BRACKET,
    KEY_OPEN_BRACKET,
    KEY_QUOTE,
    KEY_SEMICOLON,
    KEY_SLASH,
    KEY_COMMA,
    KEY_BACKSLASH,
    KEY_PERIOD,
    KEY_BACK_QUOTE,
    KEY_EQUALS,
    KEY_MINUS,
    KEY_ENTER,
    KEY_ESCAPE,
    KEY_TAB,
    KEY_BACKSPACE,
    KEY_SPACE,
    KEY_CAPSLOCK,
    KEY_LEFT_META,
    KEY_LEFT_SHIFT,
    KEY_LEFT_ALT,
    KEY_LEFT_CONTROL,
    KEY_RIGHT_META,
    KEY_RIGHT_SHIFT,
    KEY_RIGHT_ALT,
    KEY_RIGHT_CONTROL,
    KEY_MENU,
    KEY_UP,
    KEY_DOWN,
    KEY_LEFT,
    KEY_RIGHT,
    KEY_F1,
    KEY_F2,
	KEY_F3,
    KEY_F4,
    KEY_F5,
	KEY_F6,
	KEY_F7,
	KEY_F8,
	KEY_F9,
    KEY_F10,
	KEY_F11,
	KEY_F12,
	KEY_PRINTSCEEN,
	KEY_SCROLL_LOCK,
	KEY_PAUSE,
    KEY_INSERT,
    KEY_HOME,
	KEY_PGUP,
    KEY_DELETE,
    KEY_END,
    KEY_PGDOWN,
    KEY_NUM_LOCK,
    KEY_NUMPAD_0,
    KEY_NUMPAD_1,
    KEY_NUMPAD_2,
    KEY_NUMPAD_3,
    KEY_NUMPAD_4,
    KEY_NUMPAD_5,
    KEY_NUMPAD_6,
    KEY_NUMPAD_7,
    KEY_NUMPAD_8,
    KEY_NUMPAD_9,
    KEY_NUMPAD_ENTER,
    KEY_NUMPAD_ADD,
    KEY_NUMPAD_SUBTRACT,
    KEY_NUMPAD_MULTIPLY,
    KEY_NUMPAD_DIVIDE,
    KEY_NUMPAD_DECIMAL;
}

enum class SkikoGestureEventKind {
    TAP, PAN, PINCH, ROTATION, LONGPRESS, SWIPE, UNKNOWN
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
    val buttons: SkikoMouseButtons = SkikoMouseButtons.NONE,
    val modifiers: SkikoInputModifiers = SkikoInputModifiers.EMPTY,
    val kind: SkikoPointerEventKind,
    val timestamp: Long = 0,
    val platform: SkikoPlatformPointerEvent?
)

val SkikoPointerEvent.isLeftClick: Boolean
    get() = buttons.has(SkikoMouseButtons.LEFT) && (kind == SkikoPointerEventKind.UP)

val SkikoPointerEvent.isRightClick: Boolean
    get() = buttons.has(SkikoMouseButtons.RIGHT) && (kind == SkikoPointerEventKind.UP)

val SkikoPointerEvent.isMiddleClick: Boolean
    get() = buttons.has(SkikoMouseButtons.MIDDLE) && (kind == SkikoPointerEventKind.UP)

