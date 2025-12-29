package org.jetbrains.skia

import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.TestGlContext
import org.jetbrains.skiko.tests.allocateBytesForPixels
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class SurfaceTest {

    @Test
    fun surfaceTest() {
        assertFailsWith<IllegalArgumentException> {
            Surface.makeRasterN32Premul(0, 0)
        }
        Surface.makeRasterN32Premul(100, 200).use { surface ->
            assertEquals(100, surface.width)
            assertEquals(200, surface.height)
            val readPixelsBitmap = Bitmap()
            readPixelsBitmap.setImageInfo(ImageInfo.makeN32Premul(100, 200))
            readPixelsBitmap.allocPixels()
            assertTrue(surface.readPixels(readPixelsBitmap, 0, 0))

            val id = surface.generationId
            assertEquals(id, surface.generationId)

            val writePixelsBitmap = Bitmap()
            writePixelsBitmap.setImageInfo(ImageInfo.makeN32Premul(100, 200))
            writePixelsBitmap.allocPixels()
            surface.writePixels(writePixelsBitmap, 0, 0)
            assertNotEquals(id, surface.generationId)
            assertTrue(surface.isUnique)

            val imageInfo = surface.imageInfo
            assertEquals(100, imageInfo.width)
            assertEquals(200, imageInfo.height)

            val newSurface = surface.makeSurface(50, 100)!!
            assertEquals(50, newSurface.width)
            assertEquals(100, newSurface.height)

            val newSurface2 = surface.makeSurface(ImageInfo.makeN32Premul(200, 400))!!
            assertEquals(200, newSurface2.width)
            assertEquals(400, newSurface2.height)

            val image = surface.makeImageSnapshot(0, 0, 20, 30)!!
            assertEquals(20, image.width)
            assertEquals(30, image.height)

            val id2 = surface.generationId
            assertEquals(id2, surface.generationId)
            surface.notifyContentWillChange(ContentChangeMode.DISCARD)
            assertNotEquals(id2, surface.generationId)

            val context = surface.recordingContext
            assertEquals(context, null)
        }
    }

    @Test
    fun canMakeRaster() = runTest {
        val imageInfo = ImageInfo.makeN32Premul(100, 100)
        val surface1 = Surface.makeRaster(
            imageInfo, imageInfo.minRowBytes, SurfaceProps()
        )

        assertEquals(100, surface1.width)
        assertEquals(100, surface1.height)

        val surface2 = Surface.makeRaster(
            imageInfo, imageInfo.minRowBytes, null
        )
    }

    @Test
    fun canMakeRasterDirect() = runTest {
        interopScope {
            val imageInfo = ImageInfo.makeN32Premul(25, 25)
            val addr = allocateBytesForPixels(25 * imageInfo.minRowBytes)
            val surface = Surface.makeRasterDirect(imageInfo, addr, imageInfo.minRowBytes)

            val writePixelsBitmap = Bitmap()
            writePixelsBitmap.setImageInfo(ImageInfo.makeN32Premul(10, 20))
            writePixelsBitmap.allocPixels()

            surface.writePixels(writePixelsBitmap, 0, 0)
        }
    }

    @Test
    fun canMakeRasterDirectUsingPixmap() = runTest {
        interopScope {
            val imageInfo = ImageInfo.makeN32Premul(20, 20)
            val addr = allocateBytesForPixels(20 * imageInfo.minRowBytes)
            val pixmap = Pixmap.make(imageInfo, addr, imageInfo.minRowBytes)
            val surface = Surface.makeRasterDirect(pixmap)

            val writePixelsBitmap = Bitmap()
            writePixelsBitmap.setImageInfo(ImageInfo.makeN32Premul(10, 10))
            writePixelsBitmap.allocPixels()
            surface.writePixels(writePixelsBitmap, 0, 0)
        }
    }

    @Test
    fun canMakeRenderTarget() {
        if (!TestGlContext.isAvailabale()) return

        val pixels = TestGlContext.run {
            DirectContext.makeGL().useContext { ctx ->
                val imageInfo = ImageInfo.makeN32Premul(16, 16)
                val surface = Surface.makeRenderTarget(ctx, budgeted = false, imageInfo)

                surface.canvas.drawRect(
                    r = Rect(4f, 4f, 12f, 12f),
                    paint = Paint().apply { color = Color.RED }
                )
                Bitmap.makeFromImage(surface.makeImageSnapshot(), ctx)
            }
        }

        assertEquals(Color.RED, pixels.getColor(8, 8))
    }
}
