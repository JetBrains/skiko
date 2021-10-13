package org.jetbrains.skiko

actual open class SkiaLayer actual constructor(
    properties: SkiaLayerProperties
) {
    actual var renderApi: GraphicsApi = GraphicsApi.WEBGL
    actual val contentScale: Float
        get() = 1.0f
    actual var fullscreen: Boolean
        get() = false
        set(value) = throw Exception("Fullscreen is not supported!")
    actual var transparency: Boolean
        get() = false
        set(value) = throw Exception("Transparency is not supported!")
    actual fun needRedraw() {
        TODO("unimplemented")
    }
}
