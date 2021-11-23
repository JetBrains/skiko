package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private fun BreakIterator.asSequence() = generateSequence { next().let { n -> if (n == -1) null else n } }

class BreakIteratorTests {

    @Test
    fun breakIteratorTest() {
        val boundary = BreakIterator.makeWordInstance()
        boundary.setText("家捷克的软件开发公司 ,software development company")

        assertContentEquals(listOf(1, 3, 4, 6, 8, 10, 11, 12, 20, 21, 32, 33, 40), boundary.asSequence().toList())

        assertEquals(0, boundary.first())
        assertEquals(40, boundary.last())

    }
}