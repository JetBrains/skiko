package org.jetbrains.skia

import org.jetbrains.skia.util.assertIsNotNullPointer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PixelRefTest {

    @Test
    fun pixelRefTest() {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(7, 3, ColorAlphaType.OPAQUE))

        val pixelRef = bitmap.pixelRef
        assertNotNull(pixelRef)
        assertEquals(7, pixelRef.width)
        assertEquals(3, pixelRef.height)
        assertEquals(7 * 4, pixelRef.rowBytes)
        assertIsNotNullPointer(pixelRef.pixels)
    }

}