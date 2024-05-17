package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry

/**
 * Generic layer for Skiko rendering.
 */
actual open class SkiaLayer {
    /**
     * Current graphics API used for rendering.
     */
    actual var renderApi: GraphicsApi
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * Current content scale.
     */
    actual val contentScale: Float
        get() = TODO("Not yet implemented")

    /**
     * Pixel geometry corresponding to graphics device which renders this layer
     */
    actual val pixelGeometry: PixelGeometry
        get() = TODO("Not yet implemented")

    /**
     * If rendering is full screen.
     */
    actual var fullscreen: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * If transparency is enabled.
     */
    actual var transparency: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * Underlying platform component.
     */
    actual val component: Any?
        get() = TODO("Not yet implemented")

    /**
     * Current view used for rendering.
     */
    actual var renderDelegate: SkikoRenderDelegate?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * Attach this [SkikoRenderDelegate] to platform container.
     * Actual type of attach container is platform-specific.
     */
    actual fun attachTo(container: Any) {
    }

    /**
     * Detach this [SkikoRenderDelegate] from platform container.
     */
    actual fun detach() {
    }

    /**
     * Force redraw.
     */
    actual fun needRedraw() {
    }

    /**
     * Drawing function.
     */
    internal actual fun draw(canvas: Canvas) {
    }

}

actual val currentSystemTheme: SystemTheme = SystemTheme.UNKNOWN