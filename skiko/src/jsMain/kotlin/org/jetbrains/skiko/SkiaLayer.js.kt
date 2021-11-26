package org.jetbrains.skiko

import org.jetbrains.skiko.wasm.api.CanvasRenderer
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

actual open class SkiaLayer {
    private var state: CanvasRenderer? = null

    actual var renderApi: GraphicsApi = GraphicsApi.WEBGL
    actual val contentScale: Float
        get() = 1.0f
    actual var fullscreen: Boolean
        get() = false
        set(value) {
            if (value) throw Exception("Fullscreen is not supported!")
        }
    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw Exception("Transparency is not supported!")
        }

    actual fun needRedraw() {
        state?.draw()
    }

    actual var skikoView: SkikoView? = null

    actual fun attachTo(container: Any) {
        attachTo(container as HTMLCanvasElement, false)
    }

    actual fun detach() {
        // TODO: when switch to the frame dispatcher - stop it here.
    }

    private var isPointerPressed = false

    fun attachTo(htmlCanvas: HTMLCanvasElement, autoDetach: Boolean = true) {
        state = object: CanvasRenderer(htmlCanvas) {
            override fun drawFrame(currentTimestamp: Double) {
                // currentTimestamp is in milliseconds.
                val currentNanos = currentTimestamp * 1_000_000
                skikoView?.onRender(canvas, width, height, currentNanos.toLong())
            }
        }
        // See https://www.w3schools.com/jsref/dom_obj_event.asp
        // https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events
        htmlCanvas.addEventListener("pointerdown", { event ->
            event as MouseEvent
            isPointerPressed = true
            skikoView?.onPointerEvent(toSkikoEvent(event, true, SkikoPointerEventKind.DOWN))
        })
        htmlCanvas.addEventListener("pointerup", { event ->
            event as MouseEvent
            isPointerPressed = false
            skikoView?.onPointerEvent(toSkikoEvent(event, true, SkikoPointerEventKind.UP))
        })
        htmlCanvas.addEventListener("pointermove", { event ->
            event as MouseEvent
            if (isPointerPressed) {
                skikoView?.onPointerEvent(toSkikoDragEvent(event))
            } else {
                skikoView?.onPointerEvent(toSkikoEvent(event, false, SkikoPointerEventKind.MOVE))
            }
        })
        htmlCanvas.addEventListener("wheel", { event ->
            event as WheelEvent
            skikoView?.onPointerEvent(toSkikoScrollEvent(event, isPointerPressed))
        })
        htmlCanvas.addEventListener("contextmenu", { event ->
            event.preventDefault()
        })
        htmlCanvas.addEventListener("keydown", { event ->
            event as KeyboardEvent
            skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.DOWN))
        })
        htmlCanvas.addEventListener("keyup", { event ->
            event as KeyboardEvent
            skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.UP))
        })
    }
}
private var ticking = false

actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = InputEvent
actual typealias SkikoPlatformKeyboardEvent = KeyboardEvent
//  MouseEvent is base class of PointerEvent
actual typealias SkikoPlatformPointerEvent = MouseEvent

actual enum class SkikoKey(val value: Int) {
    KEY_UNKNOWN(-1),
    KEY_A(65),
    KEY_S(83),
    KEY_D(68),
    KEY_F(70),
    KEY_H(72),
    KEY_G(71),
    KEY_Z(90),
    KEY_X(88),
    KEY_C(67),
    KEY_V(86),
    KEY_B(66),
    KEY_Q(81),
    KEY_W(87),
    KEY_E(69),
    KEY_R(82),
    KEY_Y(89),
    KEY_T(54),
    KEY_U(85),
    KEY_I(73),
    KEY_P(80),
    KEY_L(76),
    KEY_J(74),
    KEY_K(75),
    KEY_N(78),
    KEY_M(77),
    KEY_O(79),
    KEY_1(49),
    KEY_2(50),
    KEY_3(51),
    KEY_4(52),
    KEY_5(53),
    KEY_6(54),
    KEY_7(55),
    KEY_8(56),
    KEY_9(57),
    KEY_0(48),
    KEY_CLOSE_BRACKET(221),
    KEY_OPEN_BRACKET(219),
    KEY_QUOTE(222),
    KEY_SEMICOLON(59),
    KEY_SLASH(191),
    KEY_COMMA(188),
    KEY_BACKSLASH(220),
    KEY_PERIOD(190),
    KEY_BACK_QUOTE(192),
    KEY_EQUALS(61),
    KEY_MINUS(173),
    KEY_ENTER(13),
    KEY_ESCAPE(27),
    KEY_TAB(9),
    KEY_BACKSPACE(8),
    KEY_SPACE(32),
    KEY_CAPSLOCK(20),
    KEY_LEFT_META(224),
    KEY_LEFT_SHIFT(16),
    KEY_LEFT_ALT(18),
    KEY_LEFT_CONTROL(17),
    KEY_RIGHT_META(0x80000000.toInt() or 224),
    KEY_RIGHT_SHIFT(0x80000000.toInt() or 16),
    KEY_RIGHT_ALT(225),
    KEY_RIGHT_CONTROL(0x80000000.toInt() or 17),
    KEY_UP(38),
    KEY_DOWN(40),
    KEY_LEFT(37),
    KEY_RIGHT(39),
    KEY_F1(112),
    KEY_F2(113),
	KEY_F3(114),
    KEY_F4(115),
    KEY_F5(116),
	KEY_F6(117),
	KEY_F7(118),
	KEY_F8(119),
	KEY_F9(120),
    KEY_F10(121),
	KEY_F11(122),
	KEY_F12(123),
	KEY_PRINTSCEEN(44),
	KEY_SCROLL_LOCK(145),
	KEY_PAUSE(19),
    KEY_INSERT(45),
    KEY_HOME(36),
	KEY_PGUP(33),
    KEY_DELETE(46),
    KEY_END(35),
    KEY_PGDOWN(34),
    KEY_NUM_LOCK(144),
    KEY_NUMPAD_0(96),
    KEY_NUMPAD_1(97),
    KEY_NUMPAD_2(98),
    KEY_NUMPAD_3(99),
    KEY_NUMPAD_4(100),
    KEY_NUMPAD_5(101),
    KEY_NUMPAD_6(102),
    KEY_NUMPAD_7(103),
    KEY_NUMPAD_8(104),
    KEY_NUMPAD_9(105),
    KEY_NUMPAD_ENTER(14),
    KEY_NUMPAD_ADD(107),
    KEY_NUMPAD_SUBTRACT(109),
    KEY_NUMPAD_MULTIPLY(106),
    KEY_NUMPAD_DIVIDE(111),
    KEY_NUMPAD_DECIMAC(110);

    companion object {
        fun valueOf(value: Int): SkikoKey {
            val key = SkikoKey.values().firstOrNull { it.value == value }
            return if (key == null) SkikoKey.KEY_UNKNOWN else key
        }
    }
}
