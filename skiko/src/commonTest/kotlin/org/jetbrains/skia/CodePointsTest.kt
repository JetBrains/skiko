package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertEquals

class CodePointsTest {

    private fun assertEqualsIntArray(a: IntArray, s: String) {
        assertEquals(a.asList(), s.intCodePoints().asList())
    }

    @Test
    fun codePointsTest() {
        assertEqualsIntArray(intArrayOf(97, 98, 99), "abc")
        assertEqualsIntArray(intArrayOf(774, 97, 774, 97, 97, 807, 776, 776), "̆ăaä̧̈")
        assertEqualsIntArray(intArrayOf(774, 9786), "̆☺")
    }
}