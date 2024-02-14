package org.jetbrains.skia

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageTestDarwin {

    @Test
    fun canCreateFromEncodedNsData() {

        val bytes = intArrayOf(
            0xCA, 0xDA, 0xCA, 0xC9, 0xA3,
            0xAC, 0xA8, 0x89, 0xA7, 0x87,
            0x9B, 0xB5, 0xE5, 0x95, 0x46,
            0x90, 0x81, 0xC5, 0x71, 0x33,
            0x75, 0x55, 0x44, 0x40, 0x30
        ).map { it.toByte() }.toByteArray()

        val imageInfo = ImageInfo(
            width = 5, height = 5,
            colorType = ColorType.GRAY_8,
            alphaType = ColorAlphaType.OPAQUE
        )

        val image = Image.makeRaster(imageInfo, bytes, 5)
        val data = image.encodeToData()!!
        val originalBitmap = Bitmap.makeFromImage(image)

        val nsData = createNSDataFromByteArray(data.bytes)
        val imageFromNsData = Image.makeFromEncoded(nsData)
        val bitmapFromNsData = Bitmap.makeFromImage(imageFromNsData)

        assertEquals(imageInfo.height, imageFromNsData.imageInfo.height)
        assertEquals(imageInfo.width, imageFromNsData.imageInfo.width)
        assertEquals(imageInfo.colorAlphaType, imageFromNsData.imageInfo.colorAlphaType)
        assertEquals(imageInfo.colorType, imageFromNsData.imageInfo.colorType)

        repeat(5) { x ->
            repeat(5) { y ->
                assertEquals(originalBitmap.getColor(x, y), bitmapFromNsData.getColor(x, y))
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun createNSDataFromByteArray(bytes: ByteArray): NSData {
    return NSData.dataWithBytes(bytes.pin().addressOf(0), bytes.size.toULong())
}