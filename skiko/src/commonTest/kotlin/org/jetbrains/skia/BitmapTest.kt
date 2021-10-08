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
}
