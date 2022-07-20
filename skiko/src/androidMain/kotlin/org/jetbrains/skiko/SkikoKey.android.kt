package org.jetbrains.skiko

import android.view.KeyEvent.*

// See https://developer.android.com/reference/android/view/KeyEvent
actual enum class SkikoKey(actual val platformKeyCode: Int) {
    KEY_UNKNOWN(KEYCODE_UNKNOWN),
    KEY_A(KEYCODE_A),
    KEY_S(KEYCODE_S),
    KEY_D(KEYCODE_D),
    KEY_F(KEYCODE_F),
    KEY_H(KEYCODE_H),
    KEY_G(KEYCODE_G),
    KEY_Z(KEYCODE_Z),
    KEY_X(KEYCODE_X),
    KEY_C(KEYCODE_C),
    KEY_V(KEYCODE_V),
    KEY_B(KEYCODE_B),
    KEY_Q(KEYCODE_Q),
    KEY_W(KEYCODE_W),
    KEY_E(KEYCODE_E),
    KEY_R(KEYCODE_R),
    KEY_Y(KEYCODE_Y),
    KEY_T(KEYCODE_T),
    KEY_U(KEYCODE_U),
    KEY_I(KEYCODE_I),
    KEY_P(KEYCODE_P),
    KEY_L(KEYCODE_L),
    KEY_J(KEYCODE_J),
    KEY_K(KEYCODE_K),
    KEY_N(KEYCODE_N),
    KEY_M(KEYCODE_M),
    KEY_O(KEYCODE_O),
    KEY_1(KEYCODE_1),
    KEY_2(KEYCODE_2),
    KEY_3(KEYCODE_3),
    KEY_4(KEYCODE_4),
    KEY_5(KEYCODE_5),
    KEY_6(KEYCODE_6),
    KEY_7(KEYCODE_7),
    KEY_8(KEYCODE_8),
    KEY_9(KEYCODE_9),
    KEY_0(KEYCODE_0),
    KEY_CLOSE_BRACKET(KEYCODE_RIGHT_BRACKET),
    KEY_OPEN_BRACKET(KEYCODE_LEFT_BRACKET),
    KEY_QUOTE(KEYCODE_UNKNOWN),
    KEY_SEMICOLON(KEYCODE_SEMICOLON),
    KEY_SLASH(KEYCODE_SLASH),
    KEY_COMMA(KEYCODE_COMMA),
    KEY_BACKSLASH(KEYCODE_BACKSLASH),
    KEY_PERIOD(KEYCODE_PERIOD),
    KEY_BACK_QUOTE(KEYCODE_UNKNOWN),
    KEY_EQUALS(KEYCODE_EQUALS),
    KEY_MINUS(KEYCODE_MINUS),
    KEY_ENTER(KEYCODE_ENTER),
    KEY_ESCAPE(KEYCODE_ESCAPE),
    KEY_TAB(KEYCODE_TAB),
    KEY_BACKSPACE(KEYCODE_DEL),
    KEY_SPACE(KEYCODE_SPACE),
    KEY_CAPSLOCK(KEYCODE_CAPS_LOCK),
    KEY_LEFT_META(KEYCODE_META_LEFT),
    KEY_LEFT_SHIFT(KEYCODE_SHIFT_LEFT),
    KEY_LEFT_ALT(KEYCODE_ALT_LEFT),
    KEY_LEFT_CONTROL(KEYCODE_CTRL_LEFT),
    KEY_RIGHT_META(KEYCODE_META_RIGHT),
    KEY_RIGHT_SHIFT(KEYCODE_SHIFT_RIGHT),
    KEY_RIGHT_ALT(KEYCODE_ALT_RIGHT),
    KEY_RIGHT_CONTROL(KEYCODE_CTRL_RIGHT),
    KEY_MENU(KEYCODE_UNKNOWN),
    KEY_UP(KEYCODE_DPAD_UP),
    KEY_DOWN(KEYCODE_DPAD_DOWN),
    KEY_LEFT(KEYCODE_DPAD_LEFT),
    KEY_RIGHT(KEYCODE_DPAD_RIGHT),
    KEY_F1(KEYCODE_F1),
    KEY_F2(KEYCODE_F2),
    KEY_F3(KEYCODE_F3),
    KEY_F4(KEYCODE_F4),
    KEY_F5(KEYCODE_F5),
    KEY_F6(KEYCODE_F6),
    KEY_F7(KEYCODE_F7),
    KEY_F8(KEYCODE_F8),
    KEY_F9(KEYCODE_F9),
    KEY_F10(KEYCODE_F10),
    KEY_F11(KEYCODE_F11),
    KEY_F12(KEYCODE_F12),
    KEY_PRINTSCEEN(KEYCODE_UNKNOWN),
    KEY_SCROLL_LOCK(KEYCODE_UNKNOWN),
    KEY_PAUSE(KEYCODE_UNKNOWN),
    KEY_INSERT(KEYCODE_INSERT),
    KEY_HOME(KEYCODE_HOME),
    KEY_PGUP(KEYCODE_PAGE_UP),
    KEY_DELETE(KEYCODE_FORWARD_DEL),
    KEY_END(KEYCODE_MOVE_END),
    KEY_PGDOWN(KEYCODE_PAGE_DOWN),
    KEY_NUM_LOCK(KEYCODE_UNKNOWN),
    KEY_NUMPAD_0(KEYCODE_NUMPAD_0),
    KEY_NUMPAD_1(KEYCODE_NUMPAD_1),
    KEY_NUMPAD_2(KEYCODE_NUMPAD_2),
    KEY_NUMPAD_3(KEYCODE_NUMPAD_3),
    KEY_NUMPAD_4(KEYCODE_NUMPAD_4),
    KEY_NUMPAD_5(KEYCODE_NUMPAD_5),
    KEY_NUMPAD_6(KEYCODE_NUMPAD_6),
    KEY_NUMPAD_7(KEYCODE_NUMPAD_7),
    KEY_NUMPAD_8(KEYCODE_NUMPAD_8),
    KEY_NUMPAD_9(KEYCODE_NUMPAD_9),
    KEY_NUMPAD_ENTER(KEYCODE_NUMPAD_ENTER),
    KEY_NUMPAD_ADD(KEYCODE_NUMPAD_ADD),
    KEY_NUMPAD_SUBTRACT(KEYCODE_NUMPAD_SUBTRACT),
    KEY_NUMPAD_MULTIPLY(KEYCODE_NUMPAD_MULTIPLY),
    KEY_NUMPAD_DIVIDE(KEYCODE_NUMPAD_DIVIDE),
    KEY_NUMPAD_DECIMAL(KEYCODE_UNKNOWN);

    companion object {
        fun valueOf(value: Int): SkikoKey {
            val key = SkikoKey.values().firstOrNull { it.platformKeyCode == value }
            return key ?: SkikoKey.KEY_UNKNOWN
        }
    }
}