package org.jetbrains.skiko

import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.impl.Native
import kotlin.test.Test

class SkiaTest {
    @Test
    fun `color_table`() {
        val array = ByteArray(256)
        val table = ColorFilter.makeTableARGB(array, array, array, array)
        require(table._ptr != Native.NullPointer)
    }
}