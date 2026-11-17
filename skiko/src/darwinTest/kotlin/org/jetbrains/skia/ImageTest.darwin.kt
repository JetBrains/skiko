package org.jetbrains.skia

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.pin
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Metal.MTLCreateSystemDefaultDevice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ImageTestDarwin {

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun cannotAdoptGlTextureWithMetalContext() {
        if (hostOs == OS.Tvos) return

        val device = assertNotNull(MTLCreateSystemDefaultDevice())
        val queue = assertNotNull(device.newCommandQueue())
        DirectContext.makeMetal(device.objcPtr(), queue.objcPtr()).use { context ->
            BackendTexture.makeGL(
                width = 1,
                height = 1,
                isMipmapped = false,
                textureId = 1,
                textureTarget = 0x0DE1,
                textureFormat = 0x8058
            ).use { texture ->
                val opaqueFailure = assertFailsWith<RuntimeException> {
                    Image.adoptTextureFrom(context, texture, SurfaceOrigin.TOP_LEFT, ColorType.RGBA_8888)
                }
                assertEquals("Failed to Image::makeFromTexture", opaqueFailure.message)
                val premulFailure = assertFailsWith<RuntimeException> {
                    Image.adoptTextureFrom(
                        context, texture, SurfaceOrigin.TOP_LEFT, ColorType.RGBA_8888, ColorAlphaType.PREMUL
                    )
                }
                assertEquals("Failed to Image::makeFromTexture", premulFailure.message)
            }
        }
    }

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
