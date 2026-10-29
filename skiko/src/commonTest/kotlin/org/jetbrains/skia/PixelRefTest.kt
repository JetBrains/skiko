package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.util.assertIsNotNullPointer
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PixelRefTest {

    @Test
    fun canNotifyChangesAndBecomeImmutable() = runTest {
        Bitmap().use { bitmap ->
            assertTrue(bitmap.allocPixels(ImageInfo.makeN32Premul(2, 2)))
            bitmap.erase(Color.RED)
            assertNotNull(bitmap.pixelRef).use { pixelRef ->
                val generation = pixelRef.generationId
                assertNotEquals(0, generation)
                assertEquals(generation, pixelRef.generationId)
                assertFalse(pixelRef.isImmutable)

                assertNotNull(bitmap.peekPixels()).use { pixmap ->
                    assertTrue(pixmap.erase(Color.BLUE))
                }
                pixelRef.notifyPixelsChanged()
                assertNotEquals(generation, pixelRef.generationId)
                assertEquals(pixelRef.generationId, bitmap.generationId)
                assertEquals(Color.BLUE, bitmap.getColor(1, 1))

                pixelRef.setImmutable()
                assertTrue(pixelRef.isImmutable)
                assertTrue(bitmap.isImmutable)
            }
        }
    }

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
