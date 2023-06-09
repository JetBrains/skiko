package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayerAnalytics
import org.jetbrains.skiko.SkikoView
import java.awt.Graphics2D

/**
 * Provides a way to draw on Skia canvas using software rendering without GPU acceleration and then draw it on [java.awt.Graphics2D].
 *
 * Content to draw is provided by [SkikoView].
 *
 * @see SwingRedrawerBase
 * @see SwingOffscreenDrawer
 */
internal class SoftwareSwingRedrawer(
    private val swingLayerProperties: SwingLayerProperties,
    skikoView: SkikoView,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(
    swingLayerProperties,
    skikoView,
    analytics,
    GraphicsApi.SOFTWARE_FAST
) {
    init {
        onDeviceChosen("Software")
    }

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    private val storage = Bitmap()

    override fun createDirectContext(): DirectContext? {
        // Raster does not need context
        return null
    }

    override fun initCanvas(context: DirectContext?): DrawingSurfaceData {
        val scale = swingLayerProperties.scale
        val w = (swingLayerProperties.width * scale).toInt().coerceAtLeast(0)
        val h = (swingLayerProperties.height * scale).toInt().coerceAtLeast(0)

        if (storage.width != w || storage.height != h) {
            storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
        }

        val canvas = Canvas(storage, SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN))

        return DrawingSurfaceData(renderTarget = null, surface = null, canvas = canvas)
    }

    override fun flush(drawingSurfaceData: DrawingSurfaceData, g: Graphics2D) {
        val width = storage.width
        val height = storage.height
        val bytes = storage.readPixels(storage.imageInfo, (width * 4), 0, 0)
        if (bytes != null) {
            swingOffscreenDrawer.draw(g, bytes, width, height)
        }
    }
}