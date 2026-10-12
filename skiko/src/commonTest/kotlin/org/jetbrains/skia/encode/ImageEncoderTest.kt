package org.jetbrains.skia.encode

import org.jetbrains.skia.Codec
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImageEncoderTest {
    @Test
    fun encodersProduceDecodableFormats() = withImage { image ->
        val cases = listOf(
            PngEncoder() to EncodedImageFormat.PNG,
            JpegEncoder() to EncodedImageFormat.JPEG,
            WebpEncoder() to EncodedImageFormat.WEBP,
        )
        for ((encoder, format) in cases) {
            assertNotNull(image.encodeToData(encoder)).use { data ->
                assertTrue(data.bytes.isNotEmpty())
                Codec.makeFromData(data).use { codec ->
                    assertEquals(format, codec.encodedImageFormat)
                    assertEquals(16, codec.imageInfo.width)
                    assertEquals(16, codec.imageInfo.height)
                    codec.readPixels().use { bitmap ->
                        assertEquals(16, bitmap.width)
                    }
                }
            }
        }
    }

    @Test
    fun customEncoderDelegatesToImage() = withImage { image ->
        var calledWith: Image? = null
        val encoder = object : ImageEncoder {
            override fun encode(input: Image) = PngEncoder().encode(input).also {
                calledWith = input
            }
        }
        assertNotNull(image.encodeToData(encoder)).use { data ->
            assertEquals(image, calledWith)
            assertTrue(data.bytes.isNotEmpty())
        }
    }

    @Test
    fun encoderValuesAndCopy() {
        assertEquals(PngEncoder(), PngEncoder().copy())
        assertEquals(JpegEncoder(quality = 73), JpegEncoder().copy(quality = 73))
        assertEquals(WebpEncoder(quality = 65f), WebpEncoder().copy(quality = 65f))
        assertEquals(setOf(PngEncoder.Filter.NONE, PngEncoder.Filter.SUB),
            PngEncoder(filters = setOf(PngEncoder.Filter.NONE, PngEncoder.Filter.SUB)).filters)
    }

    @Test
    fun invalidOptionsAreRejected() {
        assertFailsWith<IllegalArgumentException> { PngEncoder(compressionLevel = -1) }
        assertFailsWith<IllegalArgumentException> { PngEncoder(compressionLevel = 10) }
        assertFailsWith<IllegalArgumentException> { PngEncoder(filters = emptySet()) }
        assertFailsWith<IllegalArgumentException> { JpegEncoder(quality = -1) }
        assertFailsWith<IllegalArgumentException> { JpegEncoder(quality = 101) }
        assertFailsWith<IllegalArgumentException> { WebpEncoder(quality = Float.NaN) }
        assertFailsWith<IllegalArgumentException> { WebpEncoder(quality = Float.POSITIVE_INFINITY) }
        assertFailsWith<IllegalArgumentException> { WebpEncoder(quality = 101f) }
        assertFailsWith<IllegalArgumentException> { PngEncoder().copy(compressionLevel = 10) }
    }

    private fun withImage(block: (Image) -> Unit) {
        val pixels = ByteArray(16 * 16 * 4)
        for (y in 0 until 16) for (x in 0 until 16) {
            val index = (y * 16 + x) * 4
            pixels[index] = (x * 17).toByte()
            pixels[index + 1] = (y * 17).toByte()
            pixels[index + 2] = ((x * 11 + y * 13) and 255).toByte()
            pixels[index + 3] = 255.toByte()
        }
        Image.makeRaster(ImageInfo(16, 16, ColorType.RGBA_8888, ColorAlphaType.OPAQUE), pixels, 64).use(block)
    }
}
