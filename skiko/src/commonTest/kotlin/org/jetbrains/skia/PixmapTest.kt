package org.jetbrains.skia

import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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

}