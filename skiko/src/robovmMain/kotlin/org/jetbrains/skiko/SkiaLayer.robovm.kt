package org.jetbrains.skiko

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
        get() = view!!.frame.width.toFloat()

    val height: Float
        get() = view!!.frame.height.toFloat()

    internal var view: SkikoUIView? = null

    actual fun attachTo(container: Any) {
        view = container as SkikoUIView
    }

    actual fun detach() {
        view?.detach()

        view = null
        renderDelegate = null
    }

    actual var renderDelegate: SkikoRenderDelegate? = null

    internal actual fun draw(canvas: Canvas) {
        throw UnsupportedOperationException("Don't call it, artifact of wrong abstraction")
    }

    internal fun draw(surface: Surface) {
        val canvas = surface.canvas
        canvas.clear(Color.WHITE)
        renderDelegate?.onRender(canvas, surface.width, surface.height, currentNanoTime())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}
