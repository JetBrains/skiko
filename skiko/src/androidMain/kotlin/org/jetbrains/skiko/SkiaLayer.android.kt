package org.jetbrains.skiko

import android.app.PendingIntent.getActivity
import android.content.Context
import android.view.*
import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.redrawer.Redrawer


actual typealias SkikoGesturePlatformEvent = MotionEvent
actual typealias SkikoPlatformPointerEvent = MotionEvent
// TODO: most likely wrong.
actual typealias SkikoPlatformInputEvent = Any
actual typealias SkikoTouchPlatformEvent = Any
actual typealias SkikoPlatformKeyboardEvent = KeyEvent

actual open class SkiaLayer {
    private lateinit var view: SkikoSurfaceView

    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL
    actual val contentScale: Float
        get() = 1.0f

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


    actual var skikoView: SkikoView? = null

    actual fun attachTo(container: Any) {
        when (container) {
            is ViewGroup -> {
                attachTo(container, 1000, 1600) //bounds.width() - insets.right - insets.left, bounds.height() - insets.top - insets.bottom)
            }
            else -> error("Cannot attach to $container")
        }
    }

    fun attachTo(container: ViewGroup, width: Int, height: Int) {
        this.view = SkikoSurfaceView(container.context, width, height)
        container.addView(this.view)
    }

    actual fun detach() {
    }

    actual fun needRedraw() {
        TODO("Implement needRedraw()")
    }

    internal var redrawer: Redrawer? = null

    var width: Int = 0
    var height: Int = 0

    internal actual fun draw(canvas: Canvas): Unit = TODO()
}