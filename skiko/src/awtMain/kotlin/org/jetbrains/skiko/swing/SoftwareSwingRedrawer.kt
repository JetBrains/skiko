package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*

internal class SoftwareSwingRedrawer(
    private val skiaSwingLayer: SkiaSwingLayer,
    skikoView: SkikoView,
    analytics: SkiaLayerAnalytics,
    clipComponents: MutableList<ClipRectangle>,
    renderExceptionHandler: (e: RenderException) -> Unit,
) : SwingRedrawerBase(
    skiaSwingLayer,
    skikoView,
    analytics,
    GraphicsApi.SOFTWARE_FAST,
    clipComponents,
    renderExceptionHandler
) {
    init {
        onDeviceChosen("Software")
    }

    private val colorModel = ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB),
        true,
        false,
        Transparency.TRANSLUCENT,
        DataBuffer.TYPE_BYTE
    )
    private val storage = Bitmap()
    private var image: BufferedImage? = null

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
        val scale = skiaSwingLayer.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val w = (skiaSwingLayer.width * scale).toInt().coerceAtLeast(0)
        val h = (skiaSwingLayer.height * scale).toInt().coerceAtLeast(0)

        val bytes = storage.readPixels(storage.imageInfo, (w * 4), 0, 0)
        if (bytes != null) {
            val buffer = DataBufferByte(bytes, bytes.size)
            val raster = Raster.createInterleavedRaster(
                buffer,
                w,
                h,
                w * 4, 4,
                intArrayOf(2, 1, 0, 3), // BGRA order
                null
            )
            image = BufferedImage(colorModel, raster, false, null)
            g.color = Color(0, 0, 0, 0)
            g.clearRect(0, 0, w, h)
            g.drawImage(image!!, 0, 0, skiaSwingLayer.width, skiaSwingLayer.height, null)
        }
    }
}