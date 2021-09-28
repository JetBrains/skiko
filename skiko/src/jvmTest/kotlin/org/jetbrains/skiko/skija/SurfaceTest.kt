package org.jetbrains.skiko.skija

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ContentChangeMode
import org.jetbrains.skia.IRect
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

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
            assertEquals(true, surface.readPixels(readPixelsBitmap, 0, 0))
            val id: Int = surface.generationId
            assertEquals(id, surface.generationId)
            val writePixelsBitmap = Bitmap()
            writePixelsBitmap.setImageInfo(ImageInfo.makeN32Premul(100, 200))
            writePixelsBitmap.allocPixels()
            surface.writePixels(writePixelsBitmap, 0, 0)
            assertNotEquals(id, surface.generationId)
            assertEquals(true, surface.isUnique)
            val imageInfo = surface.imageInfo
            assertEquals(100, imageInfo.width)
            assertEquals(200, imageInfo.height)
            val newSurface = surface.makeSurface(50, 100)!!
            assertEquals(50, newSurface.width)
            assertEquals(100, newSurface.height)
            val newSurface2 = surface.makeSurface(ImageInfo.makeN32Premul(200, 400))!!
            assertEquals(200, newSurface2.width)
            assertEquals(400, newSurface2.height)
            val image = surface.makeImageSnapshot(IRect(0, 0, 20, 30))!!
            assertEquals(20, image.width)
            assertEquals(30, image.height)
            val id2: Int = surface.generationId
            assertEquals(id2, surface.generationId)
            surface.notifyContentWillChange(ContentChangeMode.DISCARD)
            assertNotEquals(id2, surface.generationId)
            val context = surface.recordingContext
            assertEquals(context, null)
        }
    }
}