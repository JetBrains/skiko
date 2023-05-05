package org.jetbrains.skia.icu

import kotlin.test.Test
import kotlin.test.assertEquals

class UnicodeTest {

    @Test
    fun directionality() {
        assertEquals(CharDirection.EUROPEAN_NUMBER, CharDirection.of('0'.code)) // Number
        assertEquals(CharDirection.LEFT_TO_RIGHT, CharDirection.of('A'.code)) // Latin
        assertEquals(CharDirection.RIGHT_TO_LEFT, CharDirection.of('א'.code)) // Hebrew
        assertEquals(CharDirection.RIGHT_TO_LEFT_ARABIC, CharDirection.of('؈'.code)) // Arabic
    }
}