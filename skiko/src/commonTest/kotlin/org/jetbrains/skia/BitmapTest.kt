package org.jetbrains.skia

import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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
}
