package org.jetbrains.skiko

actual enum class SkikoKey(actual val platformKeyCode: Int) {
    KEY_UNKNOWN(-1),

    KEY_0(0x30),
    KEY_1(0x31),
    KEY_2(0x32),
    KEY_3(0x33),
    KEY_4(0x34),
    KEY_5(0x35),
    KEY_6(0x36),
    KEY_7(0x37),
    KEY_8(0x38),
    KEY_9(0x39),

    KEY_A(0x41),
    KEY_B(0x42),
    KEY_C(0x43),
    KEY_D(0x44),
    KEY_E(0x45),
    KEY_F(0x46),
    KEY_G(0x47),
    KEY_H(0x48),
    KEY_I(0x49),
    KEY_J(0x4A),
    KEY_K(0x4B),
    KEY_L(0x4C),
    KEY_M(0x4D),
    KEY_N(0x4E),
    KEY_O(0x4F),
    KEY_P(0x50),
    KEY_Q(0x51),
    KEY_R(0x52),
    KEY_S(0x53),
    KEY_T(0x54),
    KEY_U(0x55),
    KEY_V(0x56),
    KEY_W(0x57),
    KEY_X(0x58),
    KEY_Y(0x59),
    KEY_Z(0x5A),

    KEY_CLOSE_BRACKET(0xDD),
    KEY_OPEN_BRACKET(0xDB),
    KEY_QUOTE(0xDE),
    KEY_SEMICOLON(	0xBA),
    KEY_SLASH(0xBF),
    KEY_COMMA(0xBC),
    KEY_BACKSLASH(	0xDC),
    KEY_PERIOD(0xBE),
    KEY_BACK_QUOTE(0xC0),
    KEY_EQUALS(0xBB),
    KEY_MINUS(0xBD),
    KEY_ENTER(0x0D),
    KEY_ESCAPE(0x1B),
    KEY_TAB(0x09),
    KEY_BACKSPACE(0x08),
    KEY_SPACE(0x20),
    KEY_CAPSLOCK(0x14),

    KEY_LEFT_META(0x5B),
    KEY_LEFT_SHIFT(0xA0),
    KEY_LEFT_ALT(0xA4),
    KEY_LEFT_CONTROL(0xA2),

    KEY_RIGHT_META(0x5C),
    KEY_RIGHT_SHIFT(0xA1),
    KEY_RIGHT_ALT(0xA5),
    KEY_RIGHT_CONTROL(0xA3),

    KEY_MENU(0x5D),

    KEY_UP(0x26),
    KEY_DOWN(0x28),
    KEY_LEFT(0x25),
    KEY_RIGHT(0x27),

    KEY_F1(0x70),
    KEY_F2(0x71),
    KEY_F3(0x72),
    KEY_F4(0x73),
    KEY_F5(0x74),
    KEY_F6(0x75),
    KEY_F7(0x76),
    KEY_F8(0x77),
    KEY_F9(0x78),
    KEY_F10(0x79),
    KEY_F11(0x7A),
    KEY_F12(0x7B),

	KEY_PRINTSCEEN(0x2C),
	KEY_SCROLL_LOCK(0x91),
	KEY_PAUSE(0x13),

    KEY_INSERT(0x2D),
    KEY_HOME(0x24),
	KEY_PGUP(0x21),
    KEY_DELETE(0x2E),
    KEY_END(0x23),
    KEY_PGDOWN(0x22),

    KEY_NUM_LOCK(0x90),

    KEY_NUMPAD_0(0x60),
    KEY_NUMPAD_1(0x61),
    KEY_NUMPAD_2(0x62),
    KEY_NUMPAD_3(0x63),
    KEY_NUMPAD_4(0x64),
    KEY_NUMPAD_5(0x65),
    KEY_NUMPAD_6(0x66),
    KEY_NUMPAD_7(0x67),
    KEY_NUMPAD_8(0x68),
    KEY_NUMPAD_9(0x69),

    KEY_NUMPAD_ENTER(0x0D),
    KEY_NUMPAD_ADD(0x6B),
    KEY_NUMPAD_SUBTRACT(0x6D),
    KEY_NUMPAD_MULTIPLY(0x6A),
    KEY_NUMPAD_DIVIDE(0x6F),
    KEY_NUMPAD_DECIMAL(0x6E);

    companion object {
        fun valueOf(platformKeyCode: Int): SkikoKey {
            val key = SkikoKey.values().firstOrNull { it.platformKeyCode == platformKeyCode }
            return if (key == null) SkikoKey.KEY_UNKNOWN else key
        }
    }
}
