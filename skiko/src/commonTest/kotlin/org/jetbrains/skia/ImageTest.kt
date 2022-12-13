package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageTest {

    @Test
    fun imageTest() {
        imageUnderTest { image ->
            assertEquals(100, image.width)
            assertEquals(100, image.height)

            assertTrue(image.encodeToData()?.bytes!!.isNotEmpty())
            assertTrue(image.encodeToData(EncodedImageFormat.JPEG)?.bytes!!.isNotEmpty())
            assertTrue(image.encodeToData(EncodedImageFormat.JPEG, 50)?.bytes!!.isNotEmpty())
            assertTrue(image.encodeToData(EncodedImageFormat.WEBP)?.bytes!!.isNotEmpty())
            assertTrue(image.encodeToData(EncodedImageFormat.WEBP, 50)?.bytes!!.isNotEmpty())
        }
    }

    @Test
    fun canGetImageInfo() = runTest {
        imageUnderTest { image ->
            val info = image.imageInfo
            assertEquals(100, image.width)
            assertEquals(100, image.height)
        }
    }

    @Test
    fun canMakeFromEncodedBytes() = runTest {
        val encodedBytes = imageUnderTest { image ->
            image.encodeToData()?.bytes!!
        }

        val image = Image.makeFromEncoded(encodedBytes)
        assertEquals(100, image.width)
        assertEquals(100, image.height)
    }

    @Test
    fun canMakeShader() = runTest {
        val shader = imageUnderTest { image ->
            image.makeShader(Matrix33.makeRotate(45f))
        }
    }

    @Test
    fun canMakeRaster() = runTest {
        // source:  https://fiddle.skia.org/c/@Image_MakeRasterCopy
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
    }

//    @Test
//    fun canPeekPixels() = runTest {
//        val bytes = intArrayOf(
//            0xCA, 0xDA, 0xCA, 0xC9, 0xA3,
//            0xAC, 0xA8, 0x89, 0xA7, 0x87,
//            0x9B, 0xB5, 0xE5, 0x95, 0x46,
//            0x90, 0x81, 0xC5, 0x71, 0x33,
//            0x75, 0x55, 0x44, 0x40, 0x30
//        ).map { it.toByte() }.toByteArray()
//
//        val imageInfo = ImageInfo(
//            width = 5, height = 5,
//            colorType = ColorType.GRAY_8,
//            alphaType = ColorAlphaType.OPAQUE
//        )
//
//        val image = Image.makeRaster(imageInfo, bytes, 5)
//        val pixels = image.peekPixels()!!.buffer.bytes
//
//        assertContentEquals(bytes, pixels)
//    }

    private fun <T> imageUnderTest(block: (Image) -> T): T {
        return Surface.makeRasterN32Premul(100, 100).use { surface ->
            val paint = Paint()
            paint.color = -0x10000
            Path().moveTo(20f, 80f)
                .lineTo(50f, 20f)
                .lineTo(80f, 80f)
                .closePath()
                .use { path ->
                    val canvas = surface.canvas
                    canvas.drawPath(path, paint)
                    surface.makeImageSnapshot().use { image ->
                        block(image)
                    }
                }
        }
    }
}
