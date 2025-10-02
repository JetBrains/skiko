package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.*
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The tests here draw a Paragraph on canvas and check the pixels.
 * Ideally it could be a screenshot test, but we don't have them on web yet.
 */
class ParagraphWebTest {

    private val fontCollection = FontCollection().setDefaultFontManager(FontMgr.default)

    @Test
    fun paragraphWithTabulationReplacingTabs1() {
        val paragraphStyle = ParagraphStyle().apply {
            replaceTabCharacters = true
            textStyle = TextStyle().apply {
                this.fontSize = 32.0f
            }.setColor(Color.BLACK)
        }
        val paragraph = ParagraphBuilder(paragraphStyle, fontCollection).use {
            it.addText("\t\t\t  .")
            it.build().layout(100.0f)
        }

        val surface = Surface.makeRasterN32Premul(100, 50)
        surface.canvas.clear(Color.WHITE)
        paragraph.paint(surface.canvas, 0.0f, 0.0f)
        surface.makeImageSnapshot().use { image ->
            val bitmap = Bitmap.makeFromImage(image)
            assertTrue(bitmap.height == 50)
            assertTrue(bitmap.width == 100)

            val right = paragraph.lineMetrics[0].right.roundToInt()
            val baselineY = paragraph.lineMetrics[0].baseline.roundToInt()

            var notAllWhite = false
            for (x in (right - 5)until (right + 5)) {
                for (y in (baselineY - 5)until (baselineY + 5)) {
                    if (bitmap.getColor(x, y) != Color.WHITE) {
                        notAllWhite = true
                        break
                    }
                }
            }

            assertTrue(notAllWhite, "Expected some non-white pixels for a dot.")

            val rects = paragraph.getRectsForRange(0, 4, RectHeightMode.TIGHT, RectWidthMode.TIGHT)
            val tabsRectRight = rects[0].rect.right.roundToInt()
            assertTrue(tabsRectRight > paragraph.lineMetrics[0].left &&
                    tabsRectRight < paragraph.lineMetrics[0].right
            )
            var countNotWhite = 0
            for (x in 0 until tabsRectRight) {
                for (y in 0 until bitmap.height) {
                    if (bitmap.getColor(x, y) != Color.WHITE) {
                        countNotWhite++
                    }
                }
            }

            assertEquals(0, countNotWhite, "Expected all pixels for tabs to be white.")
        }
    }

    @Test
    fun paragraphWithTabulationNoReplacingTabs() {
        val paragraphStyle = ParagraphStyle().apply {
            replaceTabCharacters = false
            textStyle = TextStyle().apply {
                this.fontSize = 32.0f
            }.setColor(Color.BLACK)
        }
        val paragraph = ParagraphBuilder(paragraphStyle, fontCollection).use {
            it.addText("\t\t\t  .")
            it.build().layout(100.0f)
        }

        val surface = Surface.makeRasterN32Premul(100, 50)
        surface.canvas.clear(Color.WHITE)
        paragraph.paint(surface.canvas, 0.0f, 0.0f)
        surface.makeImageSnapshot().use { image ->
            val bitmap = Bitmap.makeFromImage(image)
            assertTrue(bitmap.height == 50)
            assertTrue(bitmap.width == 100)

            val rects = paragraph.getRectsForRange(0, 4, RectHeightMode.TIGHT, RectWidthMode.TIGHT)
            val tabsRect = rects[0].rect
            val tabsRectRight = tabsRect.right.roundToInt()
            assertTrue(tabsRectRight > paragraph.lineMetrics[0].left &&
                    tabsRectRight < paragraph.lineMetrics[0].right
            )
            var countNotWhite = 0
            for (x in 0 until tabsRectRight) {
                for (y in 0 until bitmap.height) {
                    if (bitmap.getColor(x, y) != Color.WHITE) {
                        countNotWhite++
                    }
                }
            }

            assertTrue(countNotWhite < (tabsRect.width * tabsRect.height) / 2, "Too many non-white pixels for tabs.")
            assertTrue(countNotWhite > 300, "Expected at least 300 non-white pixels for tabs (tofu glyphs), got $countNotWhite")
        }
    }
}