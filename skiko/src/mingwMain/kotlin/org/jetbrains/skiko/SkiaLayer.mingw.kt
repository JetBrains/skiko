package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.WindowsOpenGLRedrawer
import platform.windows.*
import kotlin.system.getTimeNanos


@SymbolName("GetDpiForWindow")
private external fun GetDpiForWindow(hwnd: HWND): UInt

actual open class SkiaLayer {

    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL
        set(value) {
            if (value != GraphicsApi.OPENGL) {
                throw IllegalArgumentException("Only OpenGL is supported in Windows at the moment")
            }
            field = value
        }

    actual val contentScale: Float
        get() = GetDpiForWindow(window).toFloat() / 96.0f

    actual var fullscreen: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("Fullscreen is not supported in Windows at the moment")
        }

    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("Transparency is not supported in Windows at the moment")
        }

    internal lateinit var window: HWND

    actual val component: Any?
        get() = this.window

    actual var skikoView: SkikoView? = null

    private var redrawer: Redrawer? = null

    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    internal var onWMPaint: (nanoTime: Long) -> Unit = {}

    internal var size: Pair<Int, Int> = 0 to 0

    fun isShowing(): Boolean {
        return true
    }

    actual fun attachTo(container: Any) {
        attachTo(container as HWND)
    }

    fun attachTo(window: HWND) {
        this.window = window
        redrawer = WindowsOpenGLRedrawer(this).apply {
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

    private fun syncSize() {
        redrawer?.syncSize()
        redrawer?.needRedraw()
    }

    internal fun update(nanoTime: Long) {
        val (width, height) = size
        val bounds = Rect.makeWH(width.toFloat(), height.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds)
        skikoView?.onRender(canvas, width, height, nanoTime)

        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, width, height)
    }

    internal actual fun draw(canvas: Canvas) {
        picture?.also {
            canvas.drawPicture(it.instance)
        }
    }

    private fun getInputModifiers(): SkikoInputModifiers {
        var mod = SkikoInputModifiers.EMPTY
        if (GetAsyncKeyState(VK_SHIFT) < 0.toShort()) {
            mod += SkikoInputModifiers.SHIFT
        }
        if (GetAsyncKeyState(VK_CONTROL) < 0.toShort()) {
            mod += SkikoInputModifiers.CONTROL
        }
        if (GetAsyncKeyState(VK_MENU) < 0.toShort()) {
            mod += SkikoInputModifiers.ALT
        }
        return mod
    }

    fun windowProc(hwnd: HWND?, msg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT {
        when (msg.toInt()) {

            WM_SIZE -> syncSize()
            WM_PAINT -> onWMPaint(getTimeNanos())

            WM_KEYDOWN -> skikoView?.onKeyboardEvent(
                SkikoPlatformKeyboardEvent(
                    kind = SkikoKeyboardEventKind.DOWN,
                    virtualKey = wParam,
                    modifiers = lParam,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_KEYUP -> skikoView?.onKeyboardEvent(
                SkikoPlatformKeyboardEvent(
                    kind = SkikoKeyboardEventKind.UP,
                    virtualKey = wParam,
                    modifiers = lParam,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_CHAR -> skikoView?.onInputEvent(
                SkikoPlatformInputEvent(
                    charCode = wParam,
                    modifiers = lParam,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_MOUSEMOVE -> skikoView?.onPointerEvent(
                SkikoPlatformPointerEvent(
                    kind = SkikoPointerEventKind.MOVE,
                    pressedButtons = wParam,
                    position = lParam,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_LBUTTONDOWN -> skikoView?.onPointerEvent(
                SkikoPlatformPointerEvent(
                    kind = SkikoPointerEventKind.DOWN,
                    pressedButtons = wParam,
                    position = lParam,
                    button = SkikoMouseButtons.LEFT,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_LBUTTONUP -> skikoView?.onPointerEvent(
                SkikoPlatformPointerEvent(
                    kind = SkikoPointerEventKind.UP,
                    pressedButtons = wParam,
                    position = lParam,
                    button = SkikoMouseButtons.LEFT,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_RBUTTONDOWN -> skikoView?.onPointerEvent(
                SkikoPlatformPointerEvent(
                    kind = SkikoPointerEventKind.DOWN,
                    pressedButtons = wParam,
                    position = lParam,
                    button = SkikoMouseButtons.RIGHT,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_RBUTTONUP -> skikoView?.onPointerEvent(
                SkikoPlatformPointerEvent(
                    kind = SkikoPointerEventKind.UP,
                    pressedButtons = wParam,
                    position = lParam,
                    button = SkikoMouseButtons.RIGHT,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_MBUTTONDOWN -> skikoView?.onPointerEvent(
                SkikoPlatformPointerEvent(
                    kind = SkikoPointerEventKind.DOWN,
                    pressedButtons = wParam,
                    position = lParam,
                    button = SkikoMouseButtons.MIDDLE,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_MBUTTONUP -> skikoView?.onPointerEvent(
                SkikoPlatformPointerEvent(
                    kind = SkikoPointerEventKind.UP,
                    pressedButtons = wParam,
                    position = lParam,
                    button = SkikoMouseButtons.MIDDLE,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            WM_MOUSEWHEEL -> skikoView?.onPointerEvent(
                SkikoPlatformPointerEvent(
                    kind = SkikoPointerEventKind.SCROLL,
                    pressedButtons = wParam,
                    position = lParam,
                    inputModifiers = getInputModifiers(),
                ).skikoEvent
            )

            else -> return DefWindowProcW(hwnd, msg, wParam, lParam)
        }
        return 0
    }
}