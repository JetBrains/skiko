package org.jetbrains.skiko

import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.MacOsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import platform.AppKit.*
import platform.Foundation.NSMakeRect
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.addObserver
import platform.darwin.NSObject
import platform.CoreGraphics.CGRectMake

actual open class SkiaLayer() {
    fun isShowing(): Boolean {
        return true
    }

    actual var renderApi: GraphicsApi = GraphicsApi.METAL
    actual val contentScale: Float
        get() = if (this::nsView.isInitialized) nsView.window!!.backingScaleFactor.toFloat() else 1.0f

    actual var fullscreen: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("fullscreen unsupported")
        }

    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("transparency unsupported")
        }

    lateinit var nsView: NSView

    actual var skikoView: SkikoView? = null

    internal var redrawer: Redrawer? = null

    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    actual fun attachTo(container: Any) {
        attachTo(container as NSWindow)
    }
    fun attachTo(window: NSWindow) {
        val (width, height) = window.contentLayoutRect.useContents {
            this.size.width to this.size.height
        }
        nsView = object : NSView(NSMakeRect(0.0, 0.0, width, height)) {
            private var trackingArea : NSTrackingArea? = null
            override fun wantsUpdateLayer(): Boolean {
                return true
            }
            override fun acceptsFirstResponder(): Boolean {
                return true
            }
            override fun viewWillMoveToWindow(newWindow: NSWindow?) {
                updateTrackingAreas()
            }
            override fun updateTrackingAreas() {
                trackingArea?.let { removeTrackingArea(it) }
                trackingArea = NSTrackingArea(
                    rect = bounds,
                    options = NSTrackingActiveAlways or
                        NSTrackingMouseEnteredAndExited or
                        NSTrackingMouseMoved or
                        NSTrackingActiveInKeyWindow or
                        NSTrackingAssumeInside or
                        NSTrackingInVisibleRect,
                    owner = nsView, userInfo = null)
                nsView.addTrackingArea(trackingArea!!)
            }

            override fun mouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoMouseButtons.LEFT, SkikoPointerEventKind.DOWN, nsView))
            }
            override fun mouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoMouseButtons.LEFT, SkikoPointerEventKind.UP, nsView))
            }
            override fun rightMouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoMouseButtons.RIGHT, SkikoPointerEventKind.DOWN, nsView))
            }
            override fun rightMouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoMouseButtons.RIGHT, SkikoPointerEventKind.UP, nsView))
            }
            override fun otherMouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.DOWN, nsView))
            }
            override fun otherMouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.UP, nsView))
            }
            override fun mouseMoved(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.MOVE, nsView))
            }
            override fun mouseDragged(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.DRAG, nsView))
            }
            override fun scrollWheel(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoScrollEvent(event))
            }
            override fun keyDown(event: NSEvent) {
                skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.DOWN))
            }
            override fun flagsChanged(event: NSEvent) {
                skikoView?.onKeyboardEvent(toSkikoEvent(event))
            }
            override fun keyUp(event: NSEvent) {
                skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.UP))
            }

            @ObjCAction
            open fun onWindowClose(arg: NSObject?) {
                detach()
                val center = NSNotificationCenter.defaultCenter()
                center.removeObserver(nsView)
            }
        }
        val center = NSNotificationCenter.defaultCenter()
        center.addObserver(nsView, NSSelectorFromString("onWindowClose:"),
            NSWindowWillCloseNotification!!, window)
        window.delegate = object : NSObject(), NSWindowDelegateProtocol {
            override fun windowDidResize(notification: NSNotification) {
                val (w, h) = window.contentView!!.frame.useContents {
                    size.width to size.height
                }
                nsView.frame = CGRectMake(0.0, 0.0, w, h)
                redrawer?.syncSize()
                redrawer?.redrawImmediately()
            }

            override fun windowDidChangeBackingProperties(notification: NSNotification) {
                redrawer?.syncSize()
                redrawer?.redrawImmediately()
            }
        }
        window.contentView!!.addSubview(nsView)
        window.makeFirstResponder(nsView)
        redrawer = createNativeRedrawer(this, renderApi).apply {
            syncSize()
            needRedraw()
        }
    }

    actual fun detach() {
        redrawer?.dispose()
        redrawer = null
    }

    actual fun needRedraw() {
        redrawer?.needRedraw()
    }

    internal fun update(nanoTime: Long) {
        val width = nsView.frame.useContents { size.width }
        val height = nsView.frame.useContents { size.height }

        val pictureWidth = (width * contentScale).coerceAtLeast(0.0)
        val pictureHeight = (height * contentScale).coerceAtLeast(0.0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds)
        skikoView?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)

        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, pictureWidth.toInt(), pictureHeight.toInt())
    }

    internal fun draw(canvas: Canvas) {
        picture?.also {
            canvas.drawPicture(it.instance)
        }
    }
}

// TODO: do properly
actual typealias SkikoGesturePlatformEvent = NSEvent
actual typealias SkikoPlatformInputEvent = NSEvent
actual typealias SkikoPlatformKeyboardEvent = NSEvent
actual typealias SkikoPlatformPointerEvent = NSEvent

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