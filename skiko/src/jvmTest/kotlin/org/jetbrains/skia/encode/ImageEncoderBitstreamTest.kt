package org.jetbrains.skia.encode

import org.jetbrains.skia.Codec
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.impl.use
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.InflaterInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImageEncoderBitstreamTest {
    @Test
    fun pngFiltersAndCompressionArePassedToSkia() = withImage { image ->
        val none = image.encodeToData(PngEncoder(filters = setOf(PngEncoder.Filter.NONE)))!!.use { it.bytes }
        val sub = image.encodeToData(PngEncoder(filters = setOf(PngEncoder.Filter.SUB)))!!.use { it.bytes }
        assertEquals(0, firstPngFilter(none))
        assertEquals(1, firstPngFilter(sub))

        val level0 = image.encodeToData(PngEncoder(compressionLevel = 0))!!.use { it.bytes }
        val level9 = image.encodeToData(PngEncoder(compressionLevel = 9))!!.use { it.bytes }
        assertTrue(level9.size < level0.size)
    }

    @Test
    fun jpegSamplingAndQualityArePassedToSkia() = withImage { image ->
        val yuv420 = image.encodeToData(JpegEncoder(downsample = JpegEncoder.Downsample.YUV_420))!!.use { it.bytes }
        val yuv422 = image.encodeToData(JpegEncoder(downsample = JpegEncoder.Downsample.YUV_422))!!.use { it.bytes }
        val yuv444 = image.encodeToData(JpegEncoder(downsample = JpegEncoder.Downsample.YUV_444))!!.use { it.bytes }
        assertEquals(0x22, jpegYSampling(yuv420))
        assertEquals(0x21, jpegYSampling(yuv422))
        assertEquals(0x11, jpegYSampling(yuv444))

        val low = image.encodeToData(JpegEncoder(quality = 20))!!.use { it.bytes }
        val high = image.encodeToData(JpegEncoder(quality = 95))!!.use { it.bytes }
        assertFalse(low.contentEquals(high))
        assertDecodes(low)
        assertDecodes(high)
    }

    @Test
    fun jpegAlphaOptionChangesDecodedPixels() {
        val pixels = ByteArray(16 * 16 * 4)
        for (i in 0 until 16 * 16) {
            pixels[4 * i] = 255.toByte()
            pixels[4 * i + 3] = 64
        }
        Image.makeRaster(ImageInfo(16, 16, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL), pixels, 64).use { image ->
            val ignore = image.encodeToData(JpegEncoder(alphaOption = JpegEncoder.AlphaOption.IGNORE))!!.use { it.bytes }
            val blend = image.encodeToData(JpegEncoder(alphaOption = JpegEncoder.AlphaOption.BLEND_ON_BLACK))!!.use { it.bytes }
            val ignoreRed = decodedRed(ignore)
            val blendRed = decodedRed(blend)
            assertTrue(ignoreRed > 220, "Ignoring alpha should retain the red channel: $ignoreRed")
            assertTrue(blendRed in 40..90, "Blending on black should scale red by alpha: $blendRed")
        }
    }

    @Test
    fun webpModeAndQualityArePassedToSkia() = withImage { image ->
        val lossy = image.encodeToData(WebpEncoder(compression = WebpEncoder.Compression.LOSSY))!!.use { it.bytes }
        val lossless = image.encodeToData(WebpEncoder(compression = WebpEncoder.Compression.LOSSLESS))!!.use { it.bytes }
        assertEquals("VP8 ", String(lossy, 12, 4, Charsets.US_ASCII))
        assertEquals("VP8L", String(lossless, 12, 4, Charsets.US_ASCII))
        assertDecodes(lossy)
        assertDecodes(lossless)

        val low = image.encodeToData(WebpEncoder(quality = 15f))!!.use { it.bytes }
        val high = image.encodeToData(WebpEncoder(quality = 95f))!!.use { it.bytes }
        assertFalse(low.contentEquals(high))
        assertDecodes(low)
        assertDecodes(high)
    }

    private fun firstPngFilter(png: ByteArray): Int {
        val idat = ByteArrayOutputStream()
        var position = 8
        while (position < png.size) {
            val size = int32(png, position)
            val type = String(png, position + 4, 4, Charsets.US_ASCII)
            if (type == "IDAT") idat.write(png, position + 8, size)
            position += size + 12
        }
        return InflaterInputStream(ByteArrayInputStream(idat.toByteArray())).use { it.read() }
    }

    private fun jpegYSampling(jpeg: ByteArray): Int {
        var position = 2
        while (position < jpeg.size - 10) {
            assertEquals(0xFF, jpeg[position].toInt() and 255)
            val marker = jpeg[position + 1].toInt() and 255
            position += 2
            if (marker in 0xC0..0xCF && marker !in setOf(0xC4, 0xC8, 0xCC)) {
                return jpeg[position + 9].toInt() and 255
            }
            position += int16(jpeg, position)
        }
        error("JPEG has no start-of-frame marker")
    }

    private fun int16(bytes: ByteArray, position: Int): Int =
        ((bytes[position].toInt() and 255) shl 8) or (bytes[position + 1].toInt() and 255)

    private fun int32(bytes: ByteArray, position: Int): Int =
        (int16(bytes, position) shl 16) or int16(bytes, position + 2)

    private fun decodedRed(bytes: ByteArray): Int = org.jetbrains.skia.Data.makeFromBytes(bytes).use { data ->
        Codec.makeFromData(data).use { codec ->
            codec.readPixels().use { bitmap -> Color.getR(bitmap.getColor(8, 8)) }
        }
    }

    private fun assertDecodes(bytes: ByteArray) {
        org.jetbrains.skia.Data.makeFromBytes(bytes).use { data ->
            Codec.makeFromData(data).use { codec ->
                assertEquals(16, codec.imageInfo.width)
                codec.readPixels().use { bitmap -> assertEquals(16, bitmap.width) }
            }
        }
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
