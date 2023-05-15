package org.jetbrains.skia

import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.tests.*
import kotlin.test.*

private fun BreakIterator.asSequence() = generateSequence { next().let { n -> if (n == -1) null else n } }


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
    @SkipJsTarget
    @SkipWasmTarget
    fun breakIteratorCloneTest() {
        // Wasm and iOS builds of Skia do not include required data to implement those iterators,
        // see `third_party/externals/icu/flutter/README.md`.
        if (hostOs == OS.Ios)
            return

        if (isDebugModeOnJvm)
            throw Error("This test is usually crashes in DEBUG mode")

        val boundary = BreakIterator.makeWordInstance()

        val boundaryCloned = boundary.clone()

        boundaryCloned.setText("家捷克的软件开发公司 ,software development company")
        val boundariesList = listOf(1, 3, 4, 6, 8, 10, 11, 12, 20, 21, 32, 33, 40)
        assertContentEquals(boundariesList, boundaryCloned.asSequence().toList())

        assertEquals(0, boundaryCloned.first())
        assertEquals(40, boundaryCloned.last())
        reachabilityBarrier(boundary)
    }

    @Test
    @SkipNativeTarget
    @SkipJvmTarget
    fun breakIteratorSentenceFailsOnJsTest() {
        // Wasm and iOS builds of Skia do not include required data to implement those iterators,
        // see `third_party/externals/icu/flutter/README.md`.
        // unfortunately js target does not check neither message nor the type of exception so here we can rely only on the fact there was exception
        assertFailsWith<RuntimeException> {
            BreakIterator.makeSentenceInstance()
        }
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