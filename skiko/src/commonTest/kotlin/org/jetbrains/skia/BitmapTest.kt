package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class BitmapTest {

    @Test
    fun canAllocateZeroedPixelsAndEraseSubset() = runTest {
        Bitmap().use { bitmap ->
            assertTrue(bitmap.allocPixelsFlags(ImageInfo.makeN32Premul(3, 2), true))
            assertEquals(Color.TRANSPARENT, bitmap.getColor(0, 0))
            assertEquals(Color.TRANSPARENT, bitmap.getColor(2, 1))
            assertFalse(bitmap.computeIsOpaque())
            assertEquals(0f, bitmap.getAlphaf(2, 1))

            bitmap.erase(Color.RED)
            assertTrue(bitmap.computeIsOpaque())
            assertEquals(Color.RED, bitmap.getColor(0, 0))
            assertEquals(1f, bitmap.getAlphaf(2, 1))

            bitmap.erase(Color.TRANSPARENT, IRect.makeXYWH(1, 1, 2, 1))
            assertEquals(Color.RED, bitmap.getColor(0, 1))
            assertEquals(Color.TRANSPARENT, bitmap.getColor(1, 1))
            assertEquals(Color.TRANSPARENT, bitmap.getColor(2, 1))
            assertFalse(bitmap.computeIsOpaque())
        }
    }

    @Test
    fun canChangeAlphaTypeAndReset() = runTest {
        Bitmap().use { bitmap ->
            assertTrue(bitmap.allocPixels(ImageInfo.makeN32Premul(2, 3)))
            assertEquals(ColorAlphaType.PREMUL, bitmap.imageInfo.colorAlphaType)
            assertTrue(bitmap.setAlphaType(ColorAlphaType.OPAQUE))
            assertEquals(ColorAlphaType.OPAQUE, bitmap.imageInfo.colorAlphaType)
            assertTrue(bitmap.setAlphaType(ColorAlphaType.UNPREMUL))
            assertEquals(ColorAlphaType.UNPREMUL, bitmap.imageInfo.colorAlphaType)

            bitmap.reset()
            assertTrue(bitmap.isNull)
            assertTrue(bitmap.isEmpty)
            assertEquals(0, bitmap.width)
            assertEquals(0, bitmap.height)
            assertEquals(0, bitmap.rowBytes)
            assertEquals(ColorType.UNKNOWN, bitmap.imageInfo.colorType)
            assertEquals(ColorAlphaType.UNKNOWN, bitmap.imageInfo.colorAlphaType)
        }
    }

    @Test
    fun cloneSharesPixelsAndImmutability() = runTest {
        Bitmap().use { bitmap ->
            assertTrue(bitmap.allocPixels(ImageInfo.makeN32Premul(2, 2)))
            bitmap.erase(Color.RED)
            bitmap.makeClone().use { clone ->
                assertEquals(2, clone.width)
                assertEquals(2, clone.height)
                assertEquals(Color.RED, clone.getColor(1, 1))
                clone.erase(Color.BLUE)
                assertEquals(Color.BLUE, bitmap.getColor(1, 1))

                assertFalse(bitmap.isImmutable)
                assertFalse(clone.isImmutable)
                bitmap.setImmutable()
                assertTrue(bitmap.isImmutable)
                assertTrue(clone.isImmutable)
            }
        }
    }

    @Test
    fun canSwapBitmaps() = runTest {
        Bitmap().use { first ->
            Bitmap().use { second ->
                assertTrue(first.allocPixels(ImageInfo.makeN32Premul(2, 3)))
                assertTrue(second.allocPixels(ImageInfo.makeN32Premul(4, 1)))
                first.erase(Color.RED)
                second.erase(Color.BLUE)

                first.swap(second)
                assertEquals(4, first.width)
                assertEquals(1, first.height)
                assertEquals(Color.BLUE, first.getColor(3, 0))
                assertEquals(2, second.width)
                assertEquals(3, second.height)
                assertEquals(Color.RED, second.getColor(1, 2))
            }
        }
    }

    @Test
    fun canSharePixelRefAtOffsetAndNotifyChanges() = runTest {
        Bitmap().use { source ->
            assertTrue(source.allocPixels(ImageInfo.makeN32Premul(3, 3)))
            source.erase(Color.RED)
            source.erase(Color.BLUE, IRect.makeXYWH(1, 1, 2, 2))
            assertNotNull(source.pixelRef).use { pixelRef ->
                Bitmap().use { subset ->
                    assertTrue(subset.setImageInfo(ImageInfo.makeN32Premul(2, 2), source.rowBytes))
                    subset.setPixelRef(pixelRef, 1, 1)
                    assertEquals(IPoint(1, 1), subset.pixelRefOrigin)
                    assertEquals(Color.BLUE, subset.getColor(0, 0))
                    assertEquals(Color.BLUE, subset.getColor(1, 1))

                    val generation = source.generationId
                    assertNotNull(subset.peekPixels()).use { pixmap ->
                        assertTrue(pixmap.erase(Color.GREEN))
                    }
                    source.notifyPixelsChanged()
                    assertNotEquals(generation, source.generationId)
                    assertEquals(source.generationId, subset.generationId)
                    assertEquals(Color.GREEN, source.getColor(1, 1))
                    assertEquals(Color.RED, source.getColor(0, 0))
                }
            }
        }
    }

    @Test
    fun bitmapTest() = runTest {
        val bitmap = Bitmap()
        val id1: Int = bitmap.generationId

        assertTrue(bitmap.isNull)
        assertTrue(bitmap.isEmpty)

        bitmap.allocPixels(ImageInfo.makeS32(7, 3, ColorAlphaType.OPAQUE))
        assertNotEquals(id1, bitmap.generationId)
        assertFalse(bitmap.isNull)
        assertFalse(bitmap.isEmpty)
        assertEquals(7 * 4, bitmap.rowBytes)
        assertEquals(4, bitmap.bytesPerPixel)
        assertEquals(7, bitmap.rowBytesAsPixels)
        assertTrue(bitmap.imageInfo.colorSpace!!.isSRGB)

        bitmap.allocPixels(ImageInfo.makeS32(7, 3, ColorAlphaType.OPAQUE), 32)
        assertEquals(32, bitmap.rowBytes)
        assertEquals(4, bitmap.bytesPerPixel)
        assertEquals(8, bitmap.rowBytesAsPixels)
        assertTrue(bitmap.imageInfo.colorSpace!!.isSRGB)

        bitmap.setImageInfo(ImageInfo.makeS32(7, 3, ColorAlphaType.OPAQUE))
        assertTrue(bitmap.isNull)
        assertFalse(bitmap.isEmpty)
        assertFalse(bitmap.isReadyToDraw)
        assertTrue(bitmap.imageInfo.colorSpace!!.isSRGB)

        bitmap.allocPixels()
        assertFalse(bitmap.isNull)
        assertTrue(bitmap.isReadyToDraw)

        assertEquals(84, bitmap.computeByteSize())

        bitmap.generationId
    }

    @Test
    fun canMakeShader() = runTest {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(25, 25, ColorAlphaType.OPAQUE))
        val shader = bitmap.makeShader(Matrix33.makeRotate(45f))
    }

    @Test
    fun canExtractAlpha() = runTest {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(25, 25, ColorAlphaType.OPAQUE))

        val bitmap2 = Bitmap()
        bitmap2.allocPixels(ImageInfo.makeS32(15, 15, ColorAlphaType.OPAQUE))

        assertTrue(bitmap.extractAlpha(bitmap2))
    }

    @Test
    fun canReadPixels() = runTest {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(10, 10, ColorAlphaType.OPAQUE))
        val result = bitmap.readPixels(srcY = 5)!!

        assertTrue(bitmap.rowBytes > 0)
        assertEquals(5 * bitmap.rowBytes, result.size)
    }

    @Test
    fun canPeekPixels() = runTest {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(10, 10, ColorAlphaType.OPAQUE))
        val result = bitmap.peekPixels()!!

        assertEquals(10 * 4, result.rowBytes)
        assertEquals(10 * 10 * 4, result.computeByteSize())
        if (kotlinBackend == KotlinBackend.JVM) {
            assertEquals(bitmap.imageInfo, result.info)
        }
    }

    @Test //fixed bug https://github.com/JetBrains/skiko/pull/266
    fun canReadPixelsWithGivenRowBytes() = runTest {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(15, 15, ColorAlphaType.OPAQUE))

        val newImageInfo = ImageInfo.makeS32(5, 5, ColorAlphaType.OPAQUE)

        val result = bitmap.readPixels(
            srcY = 1, srcX = 1,
            dstInfo = newImageInfo,
            dstRowBytes = newImageInfo.minRowBytes
        )!!

        assertTrue(newImageInfo.minRowBytes > 0 && newImageInfo.minRowBytes < bitmap.rowBytes)
        assertEquals(newImageInfo.minRowBytes * 5, result.size)
    }

    @Test
    fun canInstallPixels() = runTest {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(2, 2, ColorAlphaType.OPAQUE))

        val setArray = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        assertTrue(bitmap.installPixels(setArray))

        val result = bitmap.readPixels()!!
        assertTrue(bitmap.rowBytes > 0)
        assertEquals(setArray.size, result.size)
        assertContentEquals(setArray, result)
    }

    @Test
    fun canPeekPixelsAndCompare() = runTest {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(2, 2, ColorAlphaType.OPAQUE))

        val setArray = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        assertTrue(bitmap.installPixels(setArray))

        val dataBytes = bitmap.peekPixels()!!.buffer.bytes
        assertEquals(setArray.size, dataBytes.size)


        setArray.forEachIndexed { ix, value ->
            assertEquals(value, dataBytes[ix])
        }
    }

    @Test
    fun canSetOrGetPixelRefOrigin() = runTest {
        val source = Bitmap()
        source.allocPixels(ImageInfo.makeS32(15, 15, ColorAlphaType.OPAQUE))
        val subset = Bitmap()
        source.extractSubset(subset, IRect.makeXYWH(5, 5, 10, 10))
        val sourceOrigin = source.pixelRefOrigin
        val subsetOrigin = subset.pixelRefOrigin

        assertEquals(sourceOrigin.x, 0)
        assertEquals(sourceOrigin.y, 0)
        assertEquals(subsetOrigin.x, 5)
        assertEquals(subsetOrigin.y, 5)
    }
}
