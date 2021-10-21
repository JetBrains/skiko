package org.jetbrains.skiko

actual open class SkiaLayer(properties: SkiaLayerProperties)  {
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
    actual fun needRedraw() {
        TODO("unimplemented")
    }
    actual fun attachTo(container: Any) {
        TODO("unimplemented")
    }
    actual fun detach() {
        TODO("unimplemented")
    }
    actual var skikoView: SkikoView? = null
}

// TODO: do properly
actual typealias SkikoPlatformInputEvent = Any
actual typealias SkikoPlatformKeyboardEvent = Any
actual typealias SkikoPlatformPointerEvent = Any