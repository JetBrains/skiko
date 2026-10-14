package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry

actual open class SkiaLayer {
    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL

    actual val contentScale: Float
        get() = 1.0f

    actual var fullscreen: Boolean
        get() = false
        set(value) {}

    actual fun needRender(throttledToVsync: Boolean) {}

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()")
    )
    actual fun needRedraw() = needRender()

    actual var renderDelegate: SkikoRenderDelegate? = null

    actual fun attachTo(container: Any) {}

    actual fun detach() {}

    actual val component: Any?
        get() = null

    internal actual fun draw(canvas: Canvas) {
        renderDelegate?.onRender(canvas, 0, 0, currentNanoTime())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}
