package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.shaper.RunHandler
import org.jetbrains.skia.shaper.RunInfo
import org.jetbrains.skia.shaper.Shaper
import org.jetbrains.skia.shaper.ShapingOptions
import org.jetbrains.skia.shaper.TrivialBidiRunIterator
import org.jetbrains.skia.shaper.TrivialFontRunIterator
import org.jetbrains.skia.shaper.TrivialLanguageRunIterator
import org.jetbrains.skia.shaper.TrivialScriptRunIterator
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class ShaperTest {

    private suspend fun fontInter36() =
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)

    @Test
    fun canShapeWithDifferentFactories() = runTest {
        fontInter36().use { font ->
            val factories = listOf<() -> Shaper>(
                { Shaper.makePrimitive() },
                { Shaper.makeShaperDrivenWrapper() },
                { Shaper.makeShapeThenWrap() }
            )
            for (factory in factories) {
                factory().use { shaper ->
                    shaper.shapeLine("text", font).use { line ->
                        assertEquals(4, line.glyphsLength)
                        assertContentEquals(shortArrayOf(882, 611, 943, 882), line.glyphs)
                        assertTrue(line.width > 0f)
                    }
                }
            }
        }
    }

    @Test
    fun canCreateCoreTextShaper() = runTest {
        if (!kotlinBackend.isWeb() && (hostOs == OS.MacOS || hostOs == OS.Ios || hostOs == OS.Tvos)) {
            fontInter36().use { font ->
                Shaper.makeCoreText().use { shaper ->
                    assertNotNull(shaper)
                    shaper.shapeLine("text", font).use { line ->
                        assertContentEquals(shortArrayOf(882, 611, 943, 882), line.glyphs)
                        assertEquals(4, line.glyphs.size)
                        assertTrue(line.width > 0f)
                    }
                }
            }
        } else {
            assertFailsWith<UnsupportedOperationException> { Shaper.makeCoreText() }
        }
    }

    @Test
    fun canShapeSingleLineWithRunHandler() = runTest {
        val text = "text"
        val offset = Point(12f, 34f)
        fontInter36().use { font ->
            Shaper.make().use { shaper ->
                for (useCustomIterators in listOf(false, true)) {
                    val calls = mutableListOf<String>()
                    val infos = mutableListOf<RunInfo>()
                    var actualGlyphs: ShortArray? = null
                    var actualPositions: Array<Point?>? = null
                    var actualClusters: IntArray? = null
                    val handler = object : RunHandler {
                        override fun beginLine() {
                            calls.add("beginLine")
                        }

                        override fun runInfo(info: RunInfo?) {
                            calls.add("runInfo")
                            infos.add(assertNotNull(info))
                        }

                        override fun commitRunInfo() {
                            calls.add("commitRunInfo")
                        }

                        override fun runOffset(info: RunInfo?): Point {
                            calls.add("runOffset")
                            infos.add(assertNotNull(info))
                            return offset
                        }

                        override fun commitRun(
                            info: RunInfo?,
                            glyphs: ShortArray?,
                            positions: Array<Point?>?,
                            clusters: IntArray?
                        ) {
                            calls.add("commitRun")
                            infos.add(assertNotNull(info))
                            actualGlyphs = glyphs
                            actualPositions = positions
                            actualClusters = clusters
                        }

                        override fun commitLine() {
                            calls.add("commitLine")
                        }
                    }
                    val result = if (useCustomIterators) {
                        shaper.shape(
                            text,
                            TrivialFontRunIterator(text, font),
                            TrivialBidiRunIterator(text, 0),
                            TrivialScriptRunIterator(text, "Latn"),
                            TrivialLanguageRunIterator(text, "en"),
                            ShapingOptions.DEFAULT,
                            Float.POSITIVE_INFINITY,
                            handler
                        )
                    } else {
                        shaper.shape(text, font, ShapingOptions.DEFAULT, Float.POSITIVE_INFINITY, handler)
                    }

                    assertSame(shaper, result)
                    assertEquals(
                        listOf("beginLine", "runInfo", "commitRunInfo", "runOffset", "commitRun", "commitLine"),
                        calls
                    )
                    assertEquals(3, infos.size)
                    for (info in infos) {
                        assertEquals(0, info.bidiLevel)
                        assertEquals(0, info.rangeBegin)
                        assertEquals(text.length, info.rangeEnd)
                        assertEquals(4, info.glyphCount)
                        assertTrue(info.advanceX > 0f)
                        assertEquals(0f, info.advanceY)
                    }
                    assertContentEquals(shortArrayOf(882, 611, 943, 882), actualGlyphs)
                    assertContentEquals(intArrayOf(0, 1, 2, 3), actualClusters)
                    val positions = assertNotNull(actualPositions)
                    assertEquals(4, positions.size)
                    assertEquals(offset, positions[0])
                    for (i in positions.indices) {
                        val position = assertNotNull(positions[i])
                        assertEquals(offset.y, position.y)
                        if (i > 0) {
                            assertTrue(position.x > assertNotNull(positions[i - 1]).x)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun canShapeLine() = runTest {
        val textLine = Shaper.make().shapeLine(
            text = "Abc123", font = fontInter36()
        )

        assertEquals(6, textLine.glyphsLength)

        assertContentEquals(
            expected = shortArrayOf(2, 574, 581, 1292, 1293, 1295),
            actual = textLine.glyphs
        )
    }

    @Test
    fun canShapeTextBlob() = runTest {
        val textBlob = Shaper.make().shape(
            text = "text",
            font = fontInter36(),
            width = 100f
        )

        assertNotEquals(null, textBlob)
        assertEquals(4, textBlob!!.glyphsLength)
        assertContentEquals(
            expected = shortArrayOf(882, 611, 943, 882),
            actual = textBlob.glyphs
        )
    }

    @Test
    // TODO: m126 update: on web and windows callCount values are bigger than on other platforms.
    //  Although the shaper impl seems to be identical
    @Ignore
    fun canShapeWithRunHandler() = runTest {
        val callCount = object {
            var beginLine = 0
            var runInfo = 0
            var commitRunInfo = 0
            var runOffset = 0
            var commitRun = 0
            var commitLine = 0
        }

        data class CommitRunArgs(
            val glyphs: ShortArray?,
            val positions: Array<Point?>?,
            val clusters: IntArray?
        )


        val commitRuns = mutableListOf<CommitRunArgs>()
        val runInfos = mutableListOf<RunInfo?>()

        Shaper.make().shape(
            text = "text\ntext text\r\ntext",
            font = fontInter36(),
            opts = ShapingOptions.DEFAULT,
            width = 100f,
            runHandler = object : RunHandler {
                override fun beginLine() {
                    callCount.beginLine += 1
                }

                override fun runInfo(info: RunInfo?) {
                    callCount.runInfo += 1
                    runInfos.add(info)
                }

                override fun commitRunInfo() {
                    callCount.commitRunInfo += 1
                }

                override fun runOffset(info: RunInfo?): Point {
                    callCount.runOffset += 1
                    return Point.ZERO
                }

                override fun commitRun(
                    info: RunInfo?,
                    glyphs: ShortArray?,
                    positions: Array<Point?>?,
                    clusters: IntArray?
                ) {
                    callCount.commitRun += 1
                    commitRuns.add(CommitRunArgs(glyphs, positions, clusters))
                }

                override fun commitLine() {
                    callCount.commitLine += 1
                }

            }
        )

        assertEquals(4, callCount.beginLine)
        assertEquals(4, callCount.runInfo)
        assertEquals(4, callCount.commitRunInfo)
        assertEquals(4, callCount.runOffset)
        assertEquals(4, callCount.commitRun)
        assertEquals(4, callCount.commitLine)

        assertTrue(runInfos[2]!!.advance.x > 80.0f)
        assertEquals(10, runInfos[2]?.rangeBegin)
        assertEquals(16, runInfos[2]?.rangeEnd)
        assertEquals(6, runInfos[2]?.glyphCount)

        // For some reasons \r and \n are handled differently on macos and ios, so they are ignored during tests
        assertContentEquals(intArrayOf(10, 11, 12, 13, 14, 15), commitRuns[2].clusters)
        assertContentEquals(shortArrayOf(882, 611, 943, 882), commitRuns[2].glyphs?.sliceArray(0..3))
        @Suppress("UNCHECKED_CAST")
        assertContentCloseEnough(arrayOf(
            Point(0.0f, 0.0f),
            Point(12.795441f, 0.0f),
            Point(33.284073f, 0.0f),
            Point(52.284073f, 0.0f),
            Point(65.28407f, 0.0f)
        ), commitRuns[2].positions!!.sliceArray(0..4) as Array<Point>, 0.01f)
    }
}
