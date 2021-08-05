package org.jetbrains.skiko

import org.jetbrains.skija.Bitmap
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