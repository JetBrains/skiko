package org.jetbrains.skiko

import android.content.Context
import android.view.*
import android.view.inputmethod.InputMethodManager
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry

actual typealias SkikoGesturePlatformEvent = MotionEvent
actual typealias SkikoPlatformPointerEvent = MotionEvent
actual typealias SkikoPlatformInputEvent = KeyEvent
actual typealias SkikoPlatformKeyboardEvent = KeyEvent

actual open class SkiaLayer {

    var gesturesToListen: Array<SkikoGestureEventKind>? = null
        set(value) {
            field = value
            initGestures()
        }

    private fun initGestures() {
        glView?.let { view ->
            view.gesturesDetector.setGesturesToListen(gesturesToListen)
            if (gesturesToListen != null) {
                view.setOnTouchListener { _, event ->
                    view.gesturesDetector.onTouchEvent(event)
                }
            }
        }
    }

    private var glView: SkikoSurfaceView? = null
    private var container: ViewGroup? = null

    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL
    actual val contentScale: Float
        get() = container?.context?.resources?.displayMetrics?.density?: 1.0f

    actual var fullscreen: Boolean
        get() = true
        set(value) {
            if (value) throw IllegalArgumentException("changing fullscreen is unsupported")
        }

    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("transparency unsupported")
        }

    actual var renderDelegate: SkikoRenderDelegate? = null

    actual fun attachTo(container: Any) {
        when (container) {
            is ViewGroup -> {
                attachTo(container)
            }
            else -> error("Cannot attach to $container")
        }
    }

    private var _isKeyboardVisible = false

    fun showScreenKeyboard() {
        if (glView != null) {
            val imm = glView!!.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            _isKeyboardVisible = true
        }
    }

    fun hideScreenKeyboard() {
        if (glView != null) {
            val imm = glView!!.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(glView!!.windowToken, 0)
            _isKeyboardVisible = false
        }
    }

    fun isScreenKeyboardOpen() = _isKeyboardVisible


    fun attachTo(container: ViewGroup) {
        initDefaultContext(container.context)

        val view = SkikoSurfaceView(container.context, this)
        container.addView(view)

        this.container = container
        this.glView = view
        initGestures()

        view.setFocusableInTouchMode(true)

        needRedraw()
    }

    actual fun detach() {
        this.container?.let {
            it.removeView(this.glView)
            this.glView = null
        }
    }

    actual fun needRedraw() {
        glView?.apply {
            scheduleFrame()
        }
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN

    actual val component: Any?
        get() = this.container

    internal actual fun draw(canvas: Canvas): Unit = TODO()
}