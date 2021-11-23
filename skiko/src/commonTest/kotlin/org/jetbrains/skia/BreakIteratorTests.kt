package org.jetbrains.skia

import kotlin.test.Test

class BreakIteratorTests {

    @Test
    fun breakIteratorTest() {
        //BreakIterator boundary = BreakIterator.
        val boundary = BreakIterator.makeWordInstance()
        boundary.setText("家捷克的软件开发公司")
    }
}