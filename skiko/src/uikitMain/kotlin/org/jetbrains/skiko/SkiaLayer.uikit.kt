package org.jetbrains.skiko

import kotlinx.cinterop.useContents
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface

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

    /**
     * The background color of the layer, as transparency is not supported.
     */
    actual var backgroundColor: Int = Color.WHITE
        set(value) {
            field = value
            needRender()
        }

    actual fun needRender(throttledToVsync: Boolean) {
        needRedrawCallback.invoke()
    }

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()")
    )
    actual fun needRedraw() = needRender()

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
        renderDelegate?.onRender(surface.canvas, surface.width, surface.height, currentNanoTime())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}
