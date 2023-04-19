package org.jetbrains.skia.paragraph

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DirectionTest {
    
    @Test
    fun directionality() {
        assertNull(charDirectionality('0'.code)) // Number
        assertEquals(Direction.LTR, charDirectionality('A'.code)) // Latin
        assertEquals(Direction.RTL, charDirectionality('א'.code)) // Hebrew
        assertEquals(Direction.RTL, charDirectionality('؈'.code)) // Arabic
    }
}