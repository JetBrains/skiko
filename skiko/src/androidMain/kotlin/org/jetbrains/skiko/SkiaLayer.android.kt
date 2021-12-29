package org.jetbrains.skiko

import android.view.KeyEvent
import android.view.MotionEvent
import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.redrawer.Redrawer

actual typealias SkikoGesturePlatformEvent = MotionEvent
actual typealias SkikoPlatformPointerEvent = MotionEvent
// TODO: most likely wrong.
actual typealias SkikoPlatformInputEvent = Any
actual typealias SkikoPlatformKeyboardEvent = KeyEvent

actual open class SkiaLayer {
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
        TODO("Implement attachTo()")
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