package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.WindowsOpenGLRedrawer
import platform.windows.HWND


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

    var onWMPaint: (nanoTime: Long) -> Unit = {}

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

    fun syncSize() {
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
}

// TODO: do properly
actual typealias SkikoTouchPlatformEvent = Any
actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = Any
actual typealias SkikoPlatformKeyboardEvent = Any
actual typealias SkikoPlatformPointerEvent = Any

actual val currentSystemTheme: SystemTheme
    get() = SystemTheme.UNKNOWN // TODO Check registry (HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize\AppsUseLightTheme)