package org.jetbrains.skia

import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class BitmapTest {

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