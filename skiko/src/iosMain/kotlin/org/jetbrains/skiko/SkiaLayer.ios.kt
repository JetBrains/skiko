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

    actual fun attachTo(container: Any) {
        view = container as SkikoUIView
    }

    actual fun detach() {
        view?.detach()

        // GC bug? fixes leak on iOS
        view = null
        renderDelegate = null
    }

    actual var renderDelegate: SkikoRenderDelegate? = null

    internal actual fun draw(canvas: Canvas) {
        throw UnsupportedOperationException("Don't call it, artifact of wrong abstraction")
    }

    internal fun draw(surface: Surface) {
        renderDelegate?.onRender(surface.canvas, surface.width, surface.height, getTimeNanos())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}
