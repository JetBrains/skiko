package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private fun BreakIterator.asSequence() = generateSequence { next().let { n -> if (n == -1) null else n } }

class BreakIteratorTests {

    @Test
    fun breakIteratorWordInstanceTest() {
        val boundary = BreakIterator.makeWordInstance()
        boundary.setText("家捷克的软件开发公司 ,software development company")

        assertContentEquals(listOf(1, 3, 4, 6, 8, 10, 11, 12, 20, 21, 32, 33, 40), boundary.asSequence().toList())

        assertEquals(0, boundary.first())
        assertEquals(40, boundary.last())
    }

    @Test
    fun breakIteratorSentenceInstanceTest() {
        val boundary = BreakIterator.makeSentenceInstance()
        boundary.setText("""
            Skiko (short for Skia for Kotlin) is the graphical library exposing significant part of Skia library APIs to Kotlin, along with the gluing code for rendering context. 
            At the moment, Linux(x86_64 and arm64), Windows(x86_64) and macOS(x86_64 and arm64) builds for Kotlin/JVM are available.
        """.trimIndent())

        assertContentEquals(listOf(168, 288), boundary.asSequence().toList())
    }
}