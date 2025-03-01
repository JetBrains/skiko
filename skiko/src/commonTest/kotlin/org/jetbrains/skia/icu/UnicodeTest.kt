package org.jetbrains.skia.icu

import org.jetbrains.skia.codePoints
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnicodeTest {
    @Test
    fun directionality() {
        assertEquals(CharDirection.EUROPEAN_NUMBER, CharDirection.of('0'.code)) // Number
        assertEquals(CharDirection.LEFT_TO_RIGHT, CharDirection.of('A'.code)) // Latin
        assertEquals(CharDirection.RIGHT_TO_LEFT, CharDirection.of('א'.code)) // Hebrew
        assertEquals(CharDirection.RIGHT_TO_LEFT_ARABIC, CharDirection.of('؈'.code)) // Arabic
    }

    @Test
    fun binaryProperties() {
        fun String.firstCodePointHasProperty(property: Int): Boolean {
            val codePoint = this.codePoints.first()
            println(codePoint.toString(16))
            return CharProperties.codePointHasBinaryProperty(codePoint, property)
        }

        assertTrue("⌚".firstCodePointHasProperty(CharProperties.EMOJI))
        assertTrue("✅".firstCodePointHasProperty(CharProperties.EMOJI_PRESENTATION))
        assertTrue("♥️".firstCodePointHasProperty(CharProperties.EXTENDED_PICTOGRAPHIC))
        assertTrue("🇮🇱".firstCodePointHasProperty(CharProperties.EMOJI))  // flag

        assertFalse("x".firstCodePointHasProperty(CharProperties.EMOJI))
    }
}