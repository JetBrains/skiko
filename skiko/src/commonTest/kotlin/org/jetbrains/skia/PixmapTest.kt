package org.jetbrains.skia

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PixmapTest {
    @Test
    fun canCreate() = runTest {
        val pixmap = Pixmap.make(
            info = ImageInfo.makeN32(8, 8, ColorAlphaType.UNPREMUL),
            buffer = Data.makeFromResource("./colors_8x8.png"),
            rowBytes = 8,
        )

        assertEquals(8, pixmap.info.width)
        assertEquals(8, pixmap.info.height)
        assertFalse(pixmap.computeIsOpaque())
    }

    @Test
    fun canResetPixelsAndColorSpace() = runTest {
        Pixmap().use { pixmap ->
            assertEquals(0, pixmap.info.width)
            assertEquals(Native.NullPointer, pixmap.addr)

            val info = ImageInfo.makeN32Premul(3, 2)
            Data.makeUninitialized(32).use { data ->
                pixmap.reset(info, data, 16)
                assertEquals(3, pixmap.info.width)
                assertEquals(2, pixmap.info.height)
                assertEquals(16, pixmap.rowBytes)
                assertEquals(4, pixmap.rowBytesAsPixels)
                assertEquals(data.writableData(), pixmap.addr)

                assertNull(pixmap.info.colorSpace)
                pixmap.setColorSpace(ColorSpace.sRGB)
                assertNotNull(pixmap.info.colorSpace).use { colorSpace ->
                    assertTrue(colorSpace.isSRGB)
                }
                pixmap.setColorSpace(null)
                assertNull(pixmap.info.colorSpace)

                pixmap.reset()
                assertEquals(0, pixmap.info.width)
                assertEquals(0, pixmap.info.height)
                assertEquals(0, pixmap.rowBytes)
                assertEquals(Native.NullPointer, pixmap.addr)
            }
        }
    }

    @Test
    fun canEraseAndExtractSubset() = runTest {
        withPixmap(3, 2) { pixmap ->
            assertTrue(pixmap.erase(Color.RED))
            assertEquals(Color.RED, pixmap.getColor(0, 0))
            assertEquals(1f, pixmap.getAlphaF(0, 0))
            assertEquals(pixmap.addr, pixmap.getAddr(0, 0))
            assertNotEquals(pixmap.addr, pixmap.getAddr(1, 1))

            assertTrue(pixmap.erase(Color.TRANSPARENT, IRect.makeXYWH(1, 1, 2, 1)))
            assertEquals(Color.RED, pixmap.getColor(0, 1))
            assertEquals(Color.TRANSPARENT, pixmap.getColor(1, 1))
            assertEquals(0f, pixmap.getAlphaF(1, 1))

            Pixmap().use { subset ->
                assertTrue(pixmap.extractSubset(subset, IRect.makeXYWH(1, 1, 2, 1)))
                assertEquals(2, subset.info.width)
                assertEquals(1, subset.info.height)
                assertEquals(pixmap.getAddr(1, 1), subset.addr)
                assertEquals(Color.TRANSPARENT, subset.getColor(0, 0))
                assertTrue(subset.erase(Color.BLUE))
                assertEquals(Color.BLUE, pixmap.getColor(1, 1))
                assertFalse(pixmap.extractSubset(subset, IRect.makeXYWH(4, 4, 1, 1)))
            }
        }
    }

    @Test
    fun canReadPixelsThroughAllOverloads() = runTest {
        withPixmap(2, 2) { source ->
            assertTrue(source.erase(Color.RED))
            assertTrue(source.erase(Color.BLUE, IRect.makeXYWH(1, 1, 1, 1)))
            withPixmap(2, 2) { destination ->
                assertTrue(destination.erase(Color.TRANSPARENT))
                assertTrue(source.readPixels(destination.info, destination.addr, destination.rowBytes))
                assertEquals(Color.RED, destination.getColor(0, 0))
                assertEquals(Color.BLUE, destination.getColor(1, 1))

                assertTrue(destination.erase(Color.TRANSPARENT))
                assertTrue(source.readPixels(destination))
                assertEquals(Color.RED, destination.getColor(0, 0))
                assertEquals(Color.BLUE, destination.getColor(1, 1))
            }
            withPixmap(1, 1) { destination ->
                assertTrue(destination.erase(Color.TRANSPARENT))
                assertTrue(source.readPixels(destination.info, destination.addr, destination.rowBytes, 1, 1))
                assertEquals(Color.BLUE, destination.getColor(0, 0))

                assertTrue(destination.erase(Color.TRANSPARENT))
                assertTrue(source.readPixels(destination, 1, 1))
                assertEquals(Color.BLUE, destination.getColor(0, 0))
                assertFalse(source.readPixels(destination, 2, 2))
            }
        }
    }

    @Test
    fun canScalePixels() = runTest {
        withPixmap(2, 2) { source ->
            assertTrue(source.erase(Color.RED))
            assertTrue(source.erase(Color.BLUE, IRect.makeXYWH(1, 1, 1, 1)))
            withPixmap(4, 4) { destination ->
                assertTrue(source.scalePixels(destination, SamplingMode.DEFAULT))
                for (y in 0 until 4) {
                    for (x in 0 until 4) {
                        val expected = if (x >= 2 && y >= 2) Color.BLUE else Color.RED
                        assertEquals(expected, destination.getColor(x, y))
                    }
                }
            }
        }
    }

    private fun <T> withPixmap(width: Int, height: Int, block: (Pixmap) -> T): T {
        val info = ImageInfo.makeN32Premul(width, height)
        return Data.makeUninitialized(info.computeMinByteSize()).use { data ->
            Pixmap.make(info, data, info.minRowBytes).use(block)
        }
    }
}
