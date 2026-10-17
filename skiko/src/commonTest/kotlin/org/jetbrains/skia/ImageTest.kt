package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImageTest {

    @Test
    fun canMakeRasterFromDataAndPeekIntoPixmap() = runTest {
        val bytes = byteArrayOf(0, 64, 127, -1)
        val info = ImageInfo(2, 2, ColorType.GRAY_8, ColorAlphaType.OPAQUE)
        Data.makeFromBytes(bytes).use { data ->
            Image.makeRaster(info, data, 2).use { image ->
                assertEquals(2, image.width)
                assertEquals(2, image.height)
                Pixmap().use { pixmap ->
                    assertTrue(image.peekPixels(pixmap))
                    assertEquals(2, pixmap.info.width)
                    assertEquals(2, pixmap.info.height)
                    assertEquals(ColorType.GRAY_8, pixmap.info.colorType)
                    pixmap.buffer.use { buffer ->
                        assertContentEquals(bytes, buffer.bytes)
                    }
                    assertEquals(Color.BLACK, pixmap.getColor(0, 0))
                    assertEquals(Color.WHITE, pixmap.getColor(1, 1))
                }
            }
        }
    }

    @Test
    fun canMakeImageFromPixmap() = runTest {
        Bitmap().use { bitmap ->
            assertTrue(bitmap.allocPixels(ImageInfo.makeN32Premul(2, 2)))
            bitmap.erase(Color.RED)
            bitmap.erase(Color.BLUE, IRect.makeXYWH(1, 1, 1, 1))
            assertNotNull(bitmap.peekPixels()).use { source ->
                Image.makeFromPixmap(source).use { image ->
                    assertEquals(2, image.width)
                    assertEquals(2, image.height)
                    assertNotNull(image.peekPixels()).use { pixels ->
                        assertEquals(Color.RED, pixels.getColor(0, 0))
                        assertEquals(Color.BLUE, pixels.getColor(1, 1))
                    }
                }
            }
        }
    }

    @Test
    fun canReadImagePixelsIntoPixmapAtOffset() = runTest {
        Bitmap().use { source ->
            assertTrue(source.allocPixels(ImageInfo.makeN32Premul(3, 2)))
            source.erase(Color.RED)
            source.erase(Color.BLUE, IRect.makeXYWH(1, 1, 2, 1))
            Image.makeFromBitmap(source).use { image ->
                Bitmap().use { destination ->
                    assertTrue(destination.allocPixels(ImageInfo.makeN32Premul(2, 1)))
                    destination.erase(Color.TRANSPARENT)
                    assertNotNull(destination.peekPixels()).use { pixels ->
                        assertTrue(image.readPixels(pixels, 1, 1, false))
                        assertEquals(Color.BLUE, pixels.getColor(0, 0))
                        assertEquals(Color.BLUE, pixels.getColor(1, 0))
                        assertFalse(image.readPixels(pixels, 3, 2, true))
                    }
                }
            }
        }
    }

    @Test
    fun canScaleImagePixelsIntoPixmap() = runTest {
        Bitmap().use { source ->
            assertTrue(source.allocPixels(ImageInfo.makeN32Premul(2, 2)))
            source.erase(Color.RED)
            source.erase(Color.BLUE, IRect.makeXYWH(1, 1, 1, 1))
            Image.makeFromBitmap(source).use { image ->
                Bitmap().use { destination ->
                    assertTrue(destination.allocPixels(ImageInfo.makeN32Premul(4, 4)))
                    assertNotNull(destination.peekPixels()).use { pixels ->
                        assertTrue(image.scalePixels(pixels, SamplingMode.DEFAULT, false))
                        for (y in 0 until 4) {
                            for (x in 0 until 4) {
                                val expected = if (x >= 2 && y >= 2) Color.BLUE else Color.RED
                                assertEquals(expected, pixels.getColor(x, y))
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun imageTest() {
        imageUnderTest { image ->
            assertEquals(100, image.width)
            assertEquals(100, image.height)

            assertTrue(image.encodeToData()?.bytes!!.isNotEmpty())
            assertTrue(image.encodeToData(EncodedImageFormat.PNG, pngCompressionLevel = 1)?.bytes!!.isNotEmpty())
            assertTrue(image.encodeToData(EncodedImageFormat.PNG, pngCompressionLevel = 0)?.bytes!!.isNotEmpty())
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

    @Test
    fun canPeekPixels() = runTest {
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
        val pixels = image.peekPixels()!!.buffer.bytes

        assertContentEquals(bytes, pixels)
    }

    private fun <T> imageUnderTest(block: (Image) -> T): T {
        return Surface.makeRasterN32Premul(100, 100).use { surface ->
            val paint = Paint()
            paint.color = -0x10000
            PathBuilder()
                .moveTo(20f, 80f)
                .lineTo(50f, 20f)
                .lineTo(80f, 80f)
                .closePath()
                .detach()
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
