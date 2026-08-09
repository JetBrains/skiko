package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas

internal interface LinuxLayerRenderer {
    val renderApi: GraphicsApi
    val description: String
    val deviceName: String?

    fun render(width: Int, height: Int, waitForVsync: Boolean, block: (Canvas) -> Unit)

    fun snapshot(width: Int, height: Int): Bitmap

    fun isContextLost(): Boolean = false

    fun <T> withExternalOpenGl(block: () -> T): T =
        error("External OpenGL access requires the OpenGL renderer")

    fun drawTexture(textureId: Int, width: Int, height: Int, canvas: Canvas) {
        error("OpenGL textures require the OpenGL renderer")
    }

    fun close(contextLost: Boolean = false)
}

internal var linuxLayerRendererFactoryOverride:
    ((GraphicsApi, LinuxSkiaLayerComponent, SkiaLayerProperties) -> LinuxLayerRenderer)? = null
