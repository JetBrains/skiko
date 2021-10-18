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
    actual var app: SkikoApp? = null
}

// TODO: do properly
actual typealias SkikoPlatformInputEvent = Any
actual typealias SkikoPlatformKeyboardEvent = Any
actual typealias SkikoPlatformMouseEvent = Any