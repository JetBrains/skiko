package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertEquals

class BreakIteratorTests {

    @Test
    fun breakIteratorTest() {
        val boundary = BreakIterator.makeWordInstance()
        boundary.setText("家捷克的软件开发公司")
        assertEquals(U16String("家捷克的软件开发公司").toString(), boundary._text.toString())
    }
}