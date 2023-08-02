package org.jetbrains.skiko

import kotlinx.cinterop.useContents
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import platform.UIKit.*
import kotlin.system.getTimeNanos

actual open class SkiaLayer {
    internal var needRedrawCallback: () -> Unit = {}

    actual var renderApi: GraphicsApi
        get() = GraphicsApi.METAL
        set(_) { throw UnsupportedOperationException() }

    actual val contentScale: Float
        get() = view!!.contentScaleFactor.toFloat()

    actual var fullscreen: Boolean
        get() = true
        set(_) { throw UnsupportedOperationException() }

    actual var transparency: Boolean
        get() = false
        set(_) { throw UnsupportedOperationException() }

    actual fun needRedraw() {
        needRedrawCallback.invoke()
    }

    actual val component: Any?
        get() = this.view

    val width: Float
       get() = view!!.frame.useContents {
           return@useContents size.width.toFloat()
       }

    val height: Float
        get() = view!!.frame.useContents {
            return@useContents size.height.toFloat()
        }

    internal var view: SkikoUIView? = null
    // We need to keep reference to gesturesDetector as Objective-C will only keep weak reference here.
    internal var gesturesDetector = SkikoGesturesDetector(this)
    var gesturesToListen: Array<SkikoGestureEventKind>? = null
        set(value) {
            field = value
            initGestures()
        }

    internal fun initGestures() {
        gesturesDetector.setGesturesToListen(gesturesToListen)
    }

    actual fun attachTo(container: Any) {
        attachTo(container as SkikoUIView)
    }

    fun attachTo(view: SkikoUIView) {
        this.view = view
    }

    actual fun detach() {
        view?.detach()
    }

    actual var skikoView: SkikoView? = null

    internal actual fun draw(canvas: Canvas) {
        throw NotImplementedError("Don't call it")
    }

    internal fun draw(surface: Surface) {
        skikoView?.onRender(surface.canvas, surface.width, surface.height, getTimeNanos())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}

// TODO: do properly
actual typealias SkikoGesturePlatformEvent = UIEvent
actual typealias SkikoPlatformInputEvent = UIPress
actual typealias SkikoPlatformKeyboardEvent = UIPress
actual typealias SkikoPlatformPointerEvent = UIEvent
