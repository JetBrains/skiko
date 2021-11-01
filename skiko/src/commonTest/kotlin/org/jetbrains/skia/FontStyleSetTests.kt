package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertEquals

class FontStyleSetTests {
    @Test
    fun fontStyleSetTest() {
        val fontStyle = FontStyleSet.makeEmpty()
        assertEquals(0, fontStyle.count())
    }
}