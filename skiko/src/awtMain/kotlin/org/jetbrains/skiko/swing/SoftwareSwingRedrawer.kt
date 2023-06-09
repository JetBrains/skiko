package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Graphics2D

internal class SoftwareSwingRedrawer(
    private val skiaSwingLayer: SkiaSwingLayer,
    skikoView: SkikoView,
    analytics: SkiaLayerAnalytics,
    clipComponents: MutableList<ClipRectangle>
) : SwingRedrawerBase(
    skiaSwingLayer,
    skikoView,
    analytics,
    GraphicsApi.SOFTWARE_FAST,
    clipComponents
) {
    init {
        onDeviceChosen("Software")
    }

    private val swingOffscreenDrawer = SwingOffscreenDrawer(skiaSwingLayer)

    private val storage = Bitmap()

    override fun createDirectContext(): DirectContext? {
        // Raster does not need context
        return null
    }

    override fun initCanvas(context: DirectContext?): DrawingSurfaceData {
        val scale = skiaSwingLayer.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val w = (skiaSwingLayer.width * scale).toInt().coerceAtLeast(0)
        val h = (skiaSwingLayer.height * scale).toInt().coerceAtLeast(0)

        if (storage.width != w || storage.height != h) {
            storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
        }

        val canvas = Canvas(storage, SurfaceProps(pixelGeometry = skiaSwingLayer.pixelGeometry))

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