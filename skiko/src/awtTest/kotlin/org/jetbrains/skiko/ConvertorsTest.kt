package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.junit.Test
import kotlin.test.assertEquals

class ConvertorsTest {

    @Test
    fun canConvertBitmapToBufferedImage() {
        val bytes = intArrayOf(
            0xCA, 0xDA, 0xCA, 0xC9, 0xA3, 0xAC, 0xA8, 0x89,
            0x9B, 0xB5, 0xE5, 0x95, 0x46, 0x90, 0x81, 0xC5
        ).map { it.toByte() }.toByteArray()

        val imageInfo = ImageInfo(
            width = 2, height = 2,
            colorType = ColorType.RGB_888X,
            alphaType = ColorAlphaType.OPAQUE
        )

        val image = Image.makeRaster(imageInfo, bytes, 8)
        val bitmap = Bitmap.makeFromImage(image)
        val bufferedImage = bitmap.toBufferedImage()

        val bitmapConverted = bufferedImage.toBitmap()
        val pixels = bitmapConverted.readPixels(imageInfo)!!

        assertEquals(bytes.size, pixels.size)
    }
}
