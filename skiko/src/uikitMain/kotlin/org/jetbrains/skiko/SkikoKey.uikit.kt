package org.jetbrains.skiko

import platform.UIKit.*

actual enum class SkikoKey(val value: UIKeyboardHIDUsage) {
    KEY_UNKNOWN(-1),
    KEY_A(UIKeyboardHIDUsageKeyboardA),
    KEY_S(UIKeyboardHIDUsageKeyboardS),
    KEY_D(UIKeyboardHIDUsageKeyboardD),
    KEY_F(UIKeyboardHIDUsageKeyboardF),
    KEY_H(UIKeyboardHIDUsageKeyboardH),
    KEY_G(UIKeyboardHIDUsageKeyboardG),
    KEY_Z(UIKeyboardHIDUsageKeyboardZ),
    KEY_X(UIKeyboardHIDUsageKeyboardX),
    KEY_C(UIKeyboardHIDUsageKeyboardC),
    KEY_V(UIKeyboardHIDUsageKeyboardV),
    KEY_B(UIKeyboardHIDUsageKeyboardB),
    KEY_Q(UIKeyboardHIDUsageKeyboardQ),
    KEY_W(UIKeyboardHIDUsageKeyboardW),
    KEY_E(UIKeyboardHIDUsageKeyboardE),
    KEY_R(UIKeyboardHIDUsageKeyboardR),
    KEY_Y(UIKeyboardHIDUsageKeyboardY),
    KEY_T(UIKeyboardHIDUsageKeyboardT),
    KEY_U(UIKeyboardHIDUsageKeyboardU),
    KEY_I(UIKeyboardHIDUsageKeyboardI),
    KEY_P(UIKeyboardHIDUsageKeyboardP),
    KEY_L(UIKeyboardHIDUsageKeyboardL),
    KEY_J(UIKeyboardHIDUsageKeyboardJ),
    KEY_K(UIKeyboardHIDUsageKeyboardK),
    KEY_N(UIKeyboardHIDUsageKeyboardN),
    KEY_M(UIKeyboardHIDUsageKeyboardM),
    KEY_O(UIKeyboardHIDUsageKeyboardO),
    KEY_1(UIKeyboardHIDUsageKeyboard1),
    KEY_2(UIKeyboardHIDUsageKeyboard2),
    KEY_3(UIKeyboardHIDUsageKeyboard3),
    KEY_4(UIKeyboardHIDUsageKeyboard4),
    KEY_5(UIKeyboardHIDUsageKeyboard5),
    KEY_6(UIKeyboardHIDUsageKeyboard6),
    KEY_7(UIKeyboardHIDUsageKeyboard7),
    KEY_8(UIKeyboardHIDUsageKeyboard8),
    KEY_9(UIKeyboardHIDUsageKeyboard9),
    KEY_0(UIKeyboardHIDUsageKeyboard0),
    KEY_CLOSE_BRACKET(UIKeyboardHIDUsageKeyboardCloseBracket),
    KEY_OPEN_BRACKET(UIKeyboardHIDUsageKeyboardOpenBracket),
    KEY_QUOTE(UIKeyboardHIDUsageKeyboardQuote),
    KEY_SEMICOLON(UIKeyboardHIDUsageKeyboardSemicolon),
    KEY_SLASH(UIKeyboardHIDUsageKeyboardSlash),
    KEY_COMMA(UIKeyboardHIDUsageKeyboardComma),
    KEY_BACKSLASH(UIKeyboardHIDUsageKeyboardBackslash),
    KEY_PERIOD(UIKeyboardHIDUsageKeyboardPeriod),
    KEY_BACK_QUOTE(53),
    KEY_EQUALS(UIKeyboardHIDUsageKeyboardEqualSign),
    KEY_MINUS(UIKeyboardHIDUsageKeyboardHyphen),
    KEY_ENTER(40),
    KEY_ESCAPE(UIKeyboardHIDUsageKeyboardEscape),
    KEY_TAB(UIKeyboardHIDUsageKeyboardTab),
    KEY_BACKSPACE(UIKeyboardHIDUsageKeyboardDeleteOrBackspace),
    KEY_SPACE(UIKeyboardHIDUsageKeyboardSpacebar),
    KEY_CAPSLOCK(UIKeyboardHIDUsageKeyboardCapsLock),
    KEY_LEFT_META(227),
    KEY_LEFT_SHIFT(UIKeyboardHIDUsageKeyboardLeftShift),
    KEY_LEFT_ALT(UIKeyboardHIDUsageKeyboardLeftAlt),
    KEY_LEFT_CONTROL(UIKeyboardHIDUsageKeyboardLeftControl),
    KEY_RIGHT_META(231),
    KEY_RIGHT_SHIFT(UIKeyboardHIDUsageKeyboardRightShift),
    KEY_RIGHT_ALT(UIKeyboardHIDUsageKeyboardRightAlt),
    KEY_RIGHT_CONTROL(UIKeyboardHIDUsageKeyboardRightControl),
    KEY_MENU(101),
    KEY_UP(UIKeyboardHIDUsageKeyboardUpArrow),
    KEY_DOWN(UIKeyboardHIDUsageKeyboardDownArrow),
    KEY_LEFT(UIKeyboardHIDUsageKeyboardLeftArrow),
    KEY_RIGHT(UIKeyboardHIDUsageKeyboardRightArrow),
    KEY_F1(UIKeyboardHIDUsageKeyboardF1),
    KEY_F2(UIKeyboardHIDUsageKeyboardF2),
	KEY_F3(UIKeyboardHIDUsageKeyboardF3),
    KEY_F4(UIKeyboardHIDUsageKeyboardF4),
    KEY_F5(UIKeyboardHIDUsageKeyboardF5),
	KEY_F6(UIKeyboardHIDUsageKeyboardF6),
	KEY_F7(UIKeyboardHIDUsageKeyboardF7),
	KEY_F8(UIKeyboardHIDUsageKeyboardF8),
	KEY_F9(UIKeyboardHIDUsageKeyboardF9),
    KEY_F10(UIKeyboardHIDUsageKeyboardF10),
	KEY_F11(UIKeyboardHIDUsageKeyboardF11),
	KEY_F12(UIKeyboardHIDUsageKeyboardF12),
	KEY_PRINTSCEEN(104),
	KEY_SCROLL_LOCK(UIKeyboardHIDUsageKeyboardScrollLock),
	KEY_PAUSE(UIKeyboardHIDUsageKeyboardPause),
    KEY_INSERT(117),
    KEY_HOME(UIKeyboardHIDUsageKeyboardHome),
	KEY_PGUP(UIKeyboardHIDUsageKeyboardPageUp),
    KEY_DELETE(UIKeyboardHIDUsageKeyboardDeleteForward),
    KEY_END(UIKeyboardHIDUsageKeyboardEnd),
    KEY_PGDOWN(UIKeyboardHIDUsageKeyboardPageDown),
    KEY_NUM_LOCK(UIKeyboardHIDUsageKeypadNumLock),
    KEY_NUMPAD_0(UIKeyboardHIDUsageKeypad0),
    KEY_NUMPAD_1(UIKeyboardHIDUsageKeypad1),
    KEY_NUMPAD_2(90),
    KEY_NUMPAD_3(UIKeyboardHIDUsageKeypad3),
    KEY_NUMPAD_4(92),
    KEY_NUMPAD_5(UIKeyboardHIDUsageKeypad5),
    KEY_NUMPAD_6(94),
    KEY_NUMPAD_7(UIKeyboardHIDUsageKeypad7),
    KEY_NUMPAD_8(96),
    KEY_NUMPAD_9(UIKeyboardHIDUsageKeypad9),
    KEY_NUMPAD_ENTER(UIKeyboardHIDUsageKeypadEnter),
    KEY_NUMPAD_ADD(87),
    KEY_NUMPAD_SUBTRACT(86),
    KEY_NUMPAD_MULTIPLY(UIKeyboardHIDUsageKeypadAsterisk),
    KEY_NUMPAD_DIVIDE(UIKeyboardHIDUsageKeypadSlash),
    KEY_NUMPAD_DECIMAL(99);

    init {
        if (value > Int.MAX_VALUE) {
            error("iOS SkikoKey, init value = $value > Int.MAX_VALUE")
        }
    }

    actual val platformKeyCode get() = value.toInt()

    companion object {
        fun valueOf(value: UIKeyboardHIDUsage): SkikoKey {
            if (value > Int.MAX_VALUE) {
                error("iOS SkikoKey, valueOf value = $value > Int.MAX_VALUE")
            }
            val key = SkikoKey.values().firstOrNull { it.value == value }
            return key ?: KEY_UNKNOWN
        }
        
        fun fromPressType(value: UIPressType): SkikoKey = when (value) {
            UIPressTypeSelect -> KEY_ENTER
            UIPressTypeUpArrow -> KEY_UP
            UIPressTypeDownArrow -> KEY_DOWN
            UIPressTypeLeftArrow -> KEY_LEFT
            UIPressTypeRightArrow -> KEY_RIGHT
            UIPressTypeMenu -> KEY_MENU
            UIPressTypePlayPause -> KEY_PAUSE
            UIPressTypePageUp -> KEY_PGUP
            UIPressTypePageDown -> KEY_PGDOWN
            else -> KEY_UNKNOWN
        }
    }
}
