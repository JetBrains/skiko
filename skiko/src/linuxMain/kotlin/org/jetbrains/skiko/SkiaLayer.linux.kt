package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry

actual open class SkiaLayer {
    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL

    actual val contentScale: Float
        get() = 1.0f

    actual var fullscreen: Boolean = false

    actual var transparency: Boolean = false

    actual val component: Any?
        get() = null

    actual fun needRender(throttledToVsync: Boolean) {
        // No scheduling on the minimal Linux native path; the sample drives rendering.
    }

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()")
    )
    actual fun needRedraw() = needRender()

    actual fun attachTo(container: Any) {
        // No-op for minimal Linux implementation
    }

    actual fun detach() {
        // No-op for minimal Linux implementation
    }

    internal actual fun draw(canvas: Canvas) {
        // Not used in the minimal Linux implementation
    }

    actual var renderDelegate: SkikoRenderDelegate? = null

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}

actual val currentSystemTheme: SystemTheme
    get() = SystemTheme.UNKNOWN
