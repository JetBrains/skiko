package org.jetbrains.skia

import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skia.util.assertContentSame
import org.jetbrains.skiko.tests.runTest
import org.jetbrains.skiko.util.IMAGE_COLORS_8X8
import org.jetbrains.skiko.util.makeSolidColor
import kotlin.test.Test
import kotlin.test.assertEquals

class CodecTest {
    @Test
    fun decodePNG() = runTest {
        val codec = Codec.makeFromData(Data.makeFromResource("./colors_8x8.png"))
        assertEquals(1, codec.frameCount)
        assertEquals(8, codec.imageInfo.width)
        assertEquals(8, codec.imageInfo.height)
        assertEquals(8, codec.size.x)
        assertEquals(8, codec.size.y)

        assertEquals(EncodedImageFormat.PNG, codec.encodedImageFormat)

        val pixels = codec.readPixels()
        assertContentSame(IMAGE_COLORS_8X8, Image.makeFromBitmap(pixels), 0.01)
    }

    @Test
    fun decodeGIF() = runTest {
        val codec = Codec.makeFromData(Data.makeFromResource("./colored_square.gif"))
        assertEquals(5, codec.frameCount)
        assertEquals(8, codec.imageInfo.width)
        assertEquals(8, codec.imageInfo.height)
        assertEquals(8, codec.size.x)
        assertEquals(8, codec.size.y)
        assertEquals(EncodedImageFormat.GIF, codec.encodedImageFormat)

        val palette = intArrayOf(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE)
        val pixels = Bitmap()
        pixels.allocPixels(codec.imageInfo)
        for (frame in 0 until codec.frameCount) {
            codec.readPixels(pixels, frame)
            val expected = Image.makeSolidColor(palette[frame], 8, 8)
            assertContentSame(expected, Image.makeFromBitmap(pixels), 0.01)
        }

        assertEquals(200, codec.getFrameInfo(3).duration)
        val framesInfo = codec.framesInfo
        assertEquals(5, framesInfo.size)
        for (frameInfo in framesInfo) {
            assertEquals(200, frameInfo.duration)
        }
    }
}