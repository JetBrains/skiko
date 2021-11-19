package org.jetbrains.skia

import org.jetbrains.skia.shaper.RunHandler
import org.jetbrains.skia.shaper.RunInfo
import org.jetbrains.skia.shaper.Shaper
import org.jetbrains.skia.shaper.ShapingOptions
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ShaperTest {

    private suspend fun fontInter36() =
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)

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
    fun canShapeWithRunHandler() = runTest {
        val callCount = object {
            var beginLine = 0
            var runInfo = 0
            var commitRunInfo = 0
            var runOffset = 0
            var commitRun = 0
            var commitLine = 0
        }

        Shaper.make().shape(
            text = "text\ntext",
            font = fontInter36(),
            opts = ShapingOptions.DEFAULT,
            width = 100f,
            runHandler = object : RunHandler {
                override fun beginLine() {
                    callCount.beginLine += 1
                }

                override fun runInfo(info: RunInfo?) {
                    callCount.runInfo += 1
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
                }

                override fun commitLine() {
                    callCount.commitLine += 1
                }

            }
        )

        assertEquals(2, callCount.beginLine)
        assertEquals(2, callCount.runInfo)
        assertEquals(2, callCount.commitRunInfo)
        assertEquals(2, callCount.runOffset)
        assertEquals(2, callCount.commitRun)
        assertEquals(2, callCount.commitLine)
    }
}
