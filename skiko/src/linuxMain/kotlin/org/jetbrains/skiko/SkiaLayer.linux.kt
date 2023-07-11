package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry

actual open class SkiaLayer : SkiaLayerInterface  {
    actual var renderApi: GraphicsApi
        get() = TODO("Not yet implemented")
        set(value) {}
    actual val contentScale: Float
        get() = TODO("Not yet implemented")
    actual var fullscreen: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var transparency: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    actual val component: Any?
        get() = TODO("Not yet implemented")
    actual override fun needRedraw() {
        TODO("unimplemented")
    }
    actual fun attachTo(container: Any) {
        TODO("unimplemented")
    }
    actual override fun detach() {
        TODO("unimplemented")
    }

    @InternalSkikoApi
    actual fun draw(canvas: Canvas) {
        TODO("unimplemented")
    }

    actual override var skikoView: SkikoView? = null
    actual val pixelGeometry: PixelGeometry
        get() = TODO("Not yet implemented")
}

// TODO: do properly
actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = Any
actual typealias SkikoPlatformKeyboardEvent = Any
actual typealias SkikoPlatformPointerEvent = Any

actual val currentSystemTheme: SystemTheme
    get() = SystemTheme.UNKNOWN