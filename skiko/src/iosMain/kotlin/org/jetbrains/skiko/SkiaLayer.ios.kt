package org.jetbrains.skiko

import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.context.MetalContextHandler
import org.jetbrains.skiko.redrawer.MetalRedrawer
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.*
import platform.darwin.NSObject
import kotlin.system.getTimeNanos

actual open class SkiaLayer {
    fun isShowing(): Boolean {
        return true
    }

    actual var renderApi: GraphicsApi
        get() = GraphicsApi.METAL
        set(value) { throw UnsupportedOperationException() }

    actual val contentScale: Float
        get() = view!!.contentScaleFactor.toFloat()

    actual var fullscreen: Boolean
        get() = true
        set(value) { throw UnsupportedOperationException() }

    actual var transparency: Boolean
        get() = false
        set(value) { throw UnsupportedOperationException() }

    actual fun needRedraw() {
        redrawer?.needRedraw()
    }

    val width: Float
       get() = view!!.frame.useContents {
           return@useContents size.width.toFloat()
       }

    val height: Float
        get() = view!!.frame.useContents {
            return@useContents size.height.toFloat()
        }

    internal var view: UIView? = null
    // We need to keep reference to controller as Objective-C will only keep weak reference here.
    lateinit private var controller: NSObject

    actual fun attachTo(container: Any) {
        attachTo(container as UIView)
    }

    fun attachTo(view: UIView) {
        this.view = view
        contextHandler = MetalContextHandler(this)
        // See https://developer.apple.com/documentation/uikit/touches_presses_and_gestures/using_responders_and_the_responder_chain_to_handle_events?language=objc
        controller = object : NSObject() {
            @ObjCAction
            fun onTap(sender: UITapGestureRecognizer) {
                val (x, y) = sender.locationInView(view).useContents { x to y }
                skikoView?.onGestureEvent(
                    SkikoGestureEvent(
                        x = x,
                        y = y,
                        kind = SkikoGestureEventKind.TAP,
                        state = toSkikoGestureState(sender.state)
                    )
                )
            }

            @ObjCAction
            fun onLongPress(sender: UILongPressGestureRecognizer) {
                val (x, y) = sender.locationInView(view).useContents { x to y }
                skikoView?.onGestureEvent(
                    SkikoGestureEvent(
                        x = x,
                        y = y,
                        kind = SkikoGestureEventKind.LONGPRESS,
                        state = toSkikoGestureState(sender.state)
                    )
                )
            }

            @ObjCAction
            fun onPinch(sender: UIPinchGestureRecognizer) {
                val (x, y) = sender.locationInView(view).useContents { x to y }
                skikoView?.onGestureEvent(
                    SkikoGestureEvent(
                        x = x,
                        y = y,
                        kind = SkikoGestureEventKind.PINCH,
                        scale = sender.scale,
                        velocity = sender.velocity,
                        state = toSkikoGestureState(sender.state)
                    )
                )
            }

            @ObjCAction
            fun onRotation(sender: UIRotationGestureRecognizer) {
                val (x, y) = sender.locationInView(view).useContents { x to y }
                skikoView?.onGestureEvent(
                    SkikoGestureEvent(
                        x = x,
                        y = y,
                        kind = SkikoGestureEventKind.ROTATION,
                        rotation = sender.rotation,
                        velocity = sender.velocity,
                        state = toSkikoGestureState(sender.state)
                    )
                )
            }

            @ObjCAction
            fun onSwipe(sender: UISwipeGestureRecognizer) {
                val (x, y) = sender.locationInView(view).useContents { x to y }
                skikoView?.onGestureEvent(
                    SkikoGestureEvent(
                        x = x,
                        y = y,
                        kind = SkikoGestureEventKind.SWIPE,
                        direction = toSkikoGestureDirection(sender.direction),
                        state = toSkikoGestureState(sender.state)
                    )
                )
            }

            @ObjCAction
            fun onPan(sender: UIPanGestureRecognizer) {
                val (x, y) = sender.locationInView(view).useContents { x to y }
                skikoView?.onGestureEvent(
                    SkikoGestureEvent(
                        x = x,
                        y = y,
                        kind = SkikoGestureEventKind.PAN,
                        state = toSkikoGestureState(sender.state)
                    )
                )
            }

        }
        // We have ':' in selector to take care of function argument.
        view.addGestureRecognizer(UITapGestureRecognizer(controller, NSSelectorFromString("onTap:")))
        view.addGestureRecognizer(UILongPressGestureRecognizer(controller, NSSelectorFromString("onLongPress:")))
        view.addGestureRecognizer(UIPinchGestureRecognizer(controller, NSSelectorFromString("onPinch:")))
        view.addGestureRecognizer(UIRotationGestureRecognizer(controller, NSSelectorFromString("onRotation:")))
        view.addGestureRecognizer(UISwipeGestureRecognizer(controller, NSSelectorFromString("onSwipe:")))
        view.addGestureRecognizer(UIPanGestureRecognizer(controller, NSSelectorFromString("onPan:")))
        // TODO: maybe add observer for view.viewDidDisappear() to detach us?
        redrawer = MetalRedrawer(this).apply {
            needRedraw()
        }
    }

    private var isDisposed = false
    actual fun detach() {
        if (!isDisposed) {
            redrawer?.dispose()
            redrawer = null
            contextHandler?.dispose()
            contextHandler = null
            isDisposed = true
        }
    }
    actual var skikoView: SkikoView? = null

    internal var redrawer: MetalRedrawer? = null
    private var contextHandler: MetalContextHandler? = null

    internal fun draw(canvas: Canvas) {
        check(!isDisposed) { "SkiaLayer is disposed" }
        val (w, h) = view!!.frame.useContents {
            size.width to size.height
        }
        val pictureWidth = (w.toFloat() * contentScale).coerceAtLeast(0.0F)
        val pictureHeight = (h.toFloat() * contentScale).coerceAtLeast(0.0F)

        skikoView?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), getTimeNanos())
    }
}

// TODO: do properly
actual typealias SkikoGesturePlatformEvent = UIEvent
actual typealias SkikoPlatformInputEvent = UIEvent
actual typealias SkikoPlatformKeyboardEvent = UIEvent
actual typealias SkikoPlatformPointerEvent = UIEvent

actual enum class SkikoKey(val value: Int) {
    KEY_UNKNOWN(-1),
    KEY_A(0),
    KEY_S(1),
    KEY_D(2),
    KEY_F(3),
    KEY_H(4),
    KEY_G(5),
    KEY_Z(6),
    KEY_X(7),
    KEY_C(8),
    KEY_V(9),
    KEY_B(11),
    KEY_Q(12),
    KEY_W(13),
    KEY_E(14),
    KEY_R(15),
    KEY_Y(16),
    KEY_T(17),
    KEY_U(32),
    KEY_I(34),
    KEY_P(35),
    KEY_L(37),
    KEY_J(38),
    KEY_K(40),
    KEY_N(45),
    KEY_M(46),
    KEY_O(31),
    KEY_1(18),
    KEY_2(19),
    KEY_3(20),
    KEY_4(21),
    KEY_5(23),
    KEY_6(22),
    KEY_7(26),
    KEY_8(28),
    KEY_9(25),
    KEY_0(29),
    KEY_CLOSE_BRACKET(30),
    KEY_OPEN_BRACKET(33),
    KEY_QUOTE(39),
    KEY_SEMICOLON(41),
    KEY_SLASH(42),
    KEY_COMMA(43),
    KEY_BACKSLASH(44),
    KEY_PERIOD(47),
    KEY_BACK_QUOTE(50),
    KEY_EQUALS(24),
    KEY_MINUS(27),
    KEY_ENTER(36),
    KEY_ESCAPE(53),
    KEY_TAB(48),
    KEY_BACKSPACE(51),
    KEY_SPACE(49),
    KEY_CAPSLOCK(57),
    KEY_LEFT_META(55),
    KEY_LEFT_SHIFT(56),
    KEY_LEFT_ALT(58),
    KEY_LEFT_CONTROL(59),
    KEY_RIGHT_META(54),
    KEY_RIGHT_SHIFT(60),
    KEY_RIGHT_ALT(61),
    KEY_RIGHT_CONTROL(62),
    KEY_UP(126),
    KEY_DOWN(125),
    KEY_LEFT(123),
    KEY_RIGHT(124),
    KEY_F1(122),
    KEY_F2(120),
	KEY_F3(99),
    KEY_F4(118),
    KEY_F5(96),
	KEY_F6(97),
	KEY_F7(98),
	KEY_F8(100),
	KEY_F9(101),
    KEY_F10(109),
	KEY_F11(103),
	KEY_F12(111),
	KEY_PRINTSCEEN(105),
	KEY_SCROLL_LOCK(107),
	KEY_PAUSE(113),
    KEY_INSERT(114),
    KEY_HOME(115),
	KEY_PGUP(116),
    KEY_DELETE(117),
    KEY_END(119),
    KEY_PGDOWN(121),
    KEY_NUM_LOCK(71),
    KEY_NUMPAD_0(82),
    KEY_NUMPAD_1(83),
    KEY_NUMPAD_2(84),
    KEY_NUMPAD_3(85),
    KEY_NUMPAD_4(86),
    KEY_NUMPAD_5(87),
    KEY_NUMPAD_6(88),
    KEY_NUMPAD_7(88),
    KEY_NUMPAD_8(91),
    KEY_NUMPAD_9(92),
    KEY_NUMPAD_ENTER(76),
    KEY_NUMPAD_ADD(69),
    KEY_NUMPAD_SUBTRACT(78),
    KEY_NUMPAD_MULTIPLY(67),
    KEY_NUMPAD_DIVIDE(75),
    KEY_NUMPAD_DECIMAC(65);

    companion object {
        fun valueOf(value: Int): SkikoKey {
            val key = SkikoKey.values().firstOrNull { it.value == value }
            return if (key == null) SkikoKey.KEY_UNKNOWN else key
        }
    }
}