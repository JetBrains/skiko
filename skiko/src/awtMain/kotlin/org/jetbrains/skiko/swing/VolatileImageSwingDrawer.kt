package org.jetbrains.skiko.swing

import com.jetbrains.JBR
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Surface
import java.awt.Graphics2D
import java.awt.Transparency
import java.awt.image.VolatileImage

/**
 * A specialized implementation of [SwingDrawer] that uses a [VolatileImage] as the intermediate storage.
 * It dumps the [Surface] underlining raster and uploads it to the [VolatileImage] using
 * <a href="https://github.com/JetBrains/JetBrainsRuntimeApi/blob/main/src/com/jetbrains/NativeRasterLoader.java">NativeRasterLoader JBR API</a>
 */
class VolatileImageSwingDrawer : SwingDrawer {
    private var volatileImage: VolatileImage? = null
    private val bitmap: Bitmap = Bitmap()
    private val rasterLoader =
        if (JBR.isNativeRasterLoaderSupported()) JBR.getNativeRasterLoader()
        else throw UnsupportedOperationException("NativeRasterLoader is not supported")

    override fun draw(g: Graphics2D, surface: Surface) {
        if (volatileImage?.width != surface.width || volatileImage?.height != surface.height ||
            volatileImage?.validate(g.deviceConfiguration) == VolatileImage.IMAGE_INCOMPATIBLE
        ) {
            volatileImage = g.deviceConfiguration.createCompatibleVolatileImage(
                surface.width,
                surface.height,
                Transparency.TRANSLUCENT
            )
        }

        if (bitmap.width != surface.width || bitmap.height != surface.height) {
            bitmap.allocPixelsFlags(ImageInfo.makeS32(surface.width, surface.height, ColorAlphaType.PREMUL), false)
        }
        surface.readPixels(bitmap, 0, 0)

        do {
            rasterLoader.loadNativeRaster(volatileImage, bitmap.peekPixels()!!.addr, bitmap.width, bitmap.height, 0, 0)
            g.drawImage(volatileImage, 0, 0, null)
        } while (volatileImage!!.contentsLost())
    }

    override fun dispose() {
        bitmap.close()
    }
}