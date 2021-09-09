package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorSpace
import org.junit.Test

internal class LibraryTest {
    @Test
    fun `load library`() {
        Library.load()
    }

    @Test
    fun `make bitmap`() {
        val bitmap = Bitmap()
        assert(bitmap.isNull)
    }
}