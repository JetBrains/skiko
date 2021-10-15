package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CodePointsTest {
    @Test
    fun codePointsTest() {
        assertContentEquals(intArrayOf(97, 98, 99), "abc".intCodePoints())
        assertContentEquals(intArrayOf(774, 97, 774, 97, 97, 807, 776, 776), "̆ăaä̧̈".intCodePoints())
        assertContentEquals(intArrayOf(774, 9786), "̆☺".intCodePoints())
    }
}