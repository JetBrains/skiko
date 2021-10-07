package org.jetbrains.skiko

actual open class SkiaLayer {
    internal actual val backedLayer: HardwareLayer = HardwareLayer()
    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL
}
