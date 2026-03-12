package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*

internal class SoftwareContextHandler(layer: SkiaLayer) : ContextFreeContextHandler(layer) {
    val colorModel = ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB),
        true,
        false,
        Transparency.TRANSLUCENT,
        DataBuffer.TYPE_BYTE
    )
    val storage = Bitmap()
    var image: BufferedImage? = null
    var imageData: ByteArray? = null
    var raster: WritableRaster? = null

    override fun LayerDrawScope.initCanvas() {
        disposeCanvas()

        val w = scaledLayerWidth
        val h = scaledLayerHeight

        if (storage.width != w || storage.height != h) {
            storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
        }

        canvas = Canvas(storage, SurfaceProps(pixelGeometry = pixelGeometry))
    }

    override fun flush(scope: LayerDrawScope) {
        val w = scope.scaledLayerWidth
        val h = scope.scaledLayerHeight

        val bytes = storage.readPixels(storage.imageInfo, (w * 4), 0, 0)
        if (bytes != null) {
            val buffer = DataBufferByte(bytes, bytes.size)
            raster = Raster.createInterleavedRaster(
                buffer,
                w,
                h,
                w * 4, 4,
                intArrayOf(2, 1, 0, 3), // BGRA order
                null
            )
            image = BufferedImage(colorModel, raster!!, false, null)
            val graphics = layer.backedLayer.getGraphics()
            if (!layer.fullscreen && layer.transparency && hostOs == OS.MacOS) {
                graphics?.setColor(Color(0, 0, 0, 0))
                graphics?.clearRect(0, 0, w, h)
            }
            graphics?.drawImage(image!!, 0, 0, layer.width, layer.height, null)
        }
    }
}
