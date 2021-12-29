package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.Redrawer
import javax.swing.UIManager

actual fun setSystemLookAndFeel(): Unit = TODO()

internal class AndroidOpenGLRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    override fun dispose() = TODO()
    override fun needRedraw() = TODO()
    override fun redrawImmediately() = TODO()

    override val renderInfo: String get() = "Android renderer"
}

internal actual fun makeDefaultRenderFactory(): RenderFactory {
    return object : RenderFactory {
        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            properties: SkiaLayerProperties
        ): Redrawer = when (hostOs) {
            OS.Android -> AndroidOpenGLRedrawer(layer, properties)
            else -> throw IllegalArgumentException("Must not happen")
        }
    }
}
