package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*

internal class SoftwareContextHandler(layer: SkiaLayer) : JvmContextHandler(layer) {
    override fun isTransparentBackground(): Boolean {
        // TODO: why Software rendering has another transparency logic from the begginning
        return hostOs == OS.MacOS && layer.transparency
    }

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
    var isInited = false

    override fun initContext(): Boolean {
        // Raster does not need context
        if (!isInited) {
            isInited = true
            onContextInitialized()
        }
        return isInited
    }

    override fun initCanvas() {
        disposeCanvas()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        if (storage.width != w || storage.height != h) {
            storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
        }

        canvas = Canvas(storage, SurfaceProps(pixelGeometry = layer.pixelGeometry))
    }

    override fun flush() {
        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)


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
