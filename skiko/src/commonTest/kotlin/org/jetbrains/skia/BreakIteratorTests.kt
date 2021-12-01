package org.jetbrains.skia

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.tests.SkipJsTarget
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private fun BreakIterator.asSequence() = generateSequence { next().let { n -> if (n == -1) null else n } }

class BreakIteratorTests {

    @Test
    fun breakIteratorWordInstanceTest() {
        // Wasm and iOS builds of Skia do not include required data to implement those iterators,
        // see `third_party/externals/icu/flutter/README.md`.
        if (hostOs == OS.JS || hostOs == OS.Ios)
            return

        val boundary = BreakIterator.makeWordInstance()
        boundary.setText("家捷克的软件开发公司 ,software development company")

        assertContentEquals(listOf(1, 3, 4, 6, 8, 10, 11, 12, 20, 21, 32, 33, 40), boundary.asSequence().toList())

        assertEquals(0, boundary.first())
        assertEquals(40, boundary.last())

        val boundaryCloned = boundary.clone()
        assertContentEquals(boundary.asSequence().toList(), boundaryCloned.asSequence().toList())
    }

    @Test
    fun breakIteratorSentenceInstanceTest() {
        // Wasm and iOS builds of Skia do not include required data to implement those iterators,
        // see `third_party/externals/icu/flutter/README.md`.
        if (hostOs == OS.JS)
            return
        val boundary = BreakIterator.makeSentenceInstance()
        boundary.setText("""
            Skiko (short for Skia for Kotlin) is the graphical library exposing significant part of Skia library APIs to Kotlin, along with the gluing code for rendering context.
            At the moment, Linux(x86_64 and arm64), Windows(x86_64) and macOS(x86_64 and arm64) builds for Kotlin/JVM are available.
        """.trimIndent())

        assertContentEquals(listOf(167, 287), boundary.asSequence().toList())
    }
}