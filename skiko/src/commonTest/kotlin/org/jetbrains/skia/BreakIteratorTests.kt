package org.jetbrains.skia

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.tests.*
import kotlin.test.*

private fun BreakIterator.asSequence() = generateSequence { next().let { n -> if (n == -1) null else n } }

/**
 * `true` when [BreakIterator] is served by a non-ICU backend, i.e. by the browser
 * `Intl.Segmenter` in an ICU-less `skiko.wasm` (`-Pskiko.wasm.useIcu=false`).
 */
private val usesBrowserBreakIterators: Boolean
    get() = createBreakIteratorBackend(BreakIterator.TYPE_CHARACTER, null) != null

class BreakIteratorTests {

    @Test
    @SkipJsTarget
    @SkipWasmTarget
    fun breakIteratorWordInstanceTest() {
        // Wasm and iOS builds of Skia do not include required data to implement those iterators,
        // see `third_party/externals/icu/flutter/README.md`.
        if (hostOs == OS.Ios || hostOs == OS.Tvos)
            return

        val boundary = BreakIterator.makeWordInstance()
        boundary.setText("家捷克的软件开发公司 ,software development company")

        val boundariesList = listOf(1, 3, 4, 6, 8, 10, 11, 12, 20, 21, 32, 33, 40)
        assertContentEquals(boundariesList, boundary.asSequence().toList())
        assertEquals(-1, boundary.next())

        //todo check what happens if we will continue with next() after -1

        assertEquals(0, boundary.first())
        assertEquals(40, boundary.last())
    }

    @Test
    @SkipNativeTarget
    @SkipJvmTarget
    fun breakIteratorSentenceFailsOnJsTest() {
        if (usesBrowserBreakIterators) {
            // An ICU-less binary segments with the browser `Intl.Segmenter` instead of failing.
            val boundary = BreakIterator.makeSentenceInstance()
            boundary.setText("One. Two.")
            assertEquals(0, boundary.first())
            assertEquals(9, boundary.last())
            return
        }
        // Wasm and iOS builds of Skia do not include required data to implement those iterators,
        // see `third_party/externals/icu/flutter/README.md`.
        // unfortunately js target does not check neither message nor the type of exception so here we can rely only on the fact there was exception
        assertFailsWith<RuntimeException> {
            BreakIterator.makeSentenceInstance()
        }
    }

    /**
     * Backend agnostic invariants that must hold for ICU and for the browser
     * `Intl.Segmenter` backend alike (see UAX #29).
     */
    @Test
    fun characterInstanceKeepsGraphemeClustersTogether() {
        if (!usesBrowserBreakIterators && (hostOs == OS.Ios || hostOs == OS.Tvos)) return

        val text = "a\u0301\uD83D\uDC69\u200D\uD83D\uDCBBz"
        val boundary = BreakIterator.makeCharacterInstance()
        boundary.setText(text)

        assertEquals(0, boundary.first())
        assertEquals(text.length, boundary.last())

        // Every boundary must be a grapheme cluster start, so the combining acute
        // accent and the ZWJ emoji sequence are never split.
        val boundaries = buildList {
            add(boundary.first())
            addAll(boundary.asSequence())
        }
        assertTrue(boundaries.contains(0))
        assertTrue(boundaries.contains(text.length))
        assertFalse(boundaries.contains(1), "combining mark must not start a cluster")
        assertFalse(boundaries.contains(3), "ZWJ emoji sequence must not be split")
    }

    @Test
    fun lineInstanceReportsMandatoryBreaks() {
        if (!usesBrowserBreakIterators && (hostOs == OS.Ios || hostOs == OS.Tvos)) return

        val text = "first line\nsecond line"
        val boundary = BreakIterator.makeLineInstance()
        boundary.setText(text)

        assertEquals(0, boundary.first())
        assertEquals(text.length, boundary.last())
        assertTrue(boundary.isBoundary(text.indexOf('\n') + 1))
    }


    @Test
    @SkipJsTarget
    @SkipWasmTarget
    fun breakIteratorSentenceInstanceTest() {
        // Wasm and iOS builds of Skia do not include required data to implement those iterators,
        // see `third_party/externals/icu/flutter/README.md`.

        val boundary = BreakIterator.makeSentenceInstance()

        boundary.setText(
            """
            Skiko (short for Skia for Kotlin) is the graphical library exposing significant part of Skia library APIs to Kotlin, along with the gluing code for rendering context.
            At the moment, Linux(x86_64 and arm64), Windows(x86_64) and macOS(x86_64 and arm64) builds for Kotlin/JVM are available.
        """.trimIndent()
        )

        assertContentEquals(listOf(167, 287), boundary.asSequence().toList())
    }

    @Test
    fun breakRuleStatusesTest() {
        val boundary = BreakIterator.makeWordInstance()
        boundary.setText("Hello world!")
        boundary.next()
        assertEquals(boundary.ruleStatus, 200)
        assertContentEquals(listOf(200), boundary.ruleStatuses.toList())
    }
}