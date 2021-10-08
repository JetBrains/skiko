package org.jetbrains.skiko

actual open class SkiaLayer {
    actual var renderApi: GraphicsApi = GraphicsApi.WEBGL
    actual val contentScale: Float
        get() = 1.0f
}
