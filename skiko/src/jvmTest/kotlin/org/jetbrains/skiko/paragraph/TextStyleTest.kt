package org.jetbrains.skiko.tests.org.jetbrains.skiko.paragraph

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.tests.runTest
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.junit.Rule
import org.junit.Test

class TextStyleTest {
    @get:Rule
    val screenshots = ScreenshotTestRule()

    @Test
    fun canApplyBaselineShift() = runTest {
        if (hostOs != OS.MacOS) {
            return@runTest
        }

        val fontCollection = FontCollection().apply {
            setDefaultFontManager(TypefaceFontProvider().apply {
                val inter = Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf", 0)
                registerTypeface(inter, "Inter")
            })
        }

        fun buildTextStyle(baselineShift: Float, color: Int = Color.BLACK) = TextStyle().apply {
            fontFamilies = arrayOf("Inter")
            fontSize = 14.0f
            this.color = color
            this.baselineShift = baselineShift
        }

        val paragraphStyle = ParagraphStyle().apply {
            height = 40.0f
            maxLinesCount = 2
            textStyle = buildTextStyle(0.0f)
        }

        val paragraph = ParagraphBuilder(paragraphStyle, fontCollection).run {
            val text = "Hello "

            addText(text)
            pushStyle(buildTextStyle(10.0f, Color.RED))
            addText(text)
            pushStyle(buildTextStyle(-10.0f, Color.GREEN))
            addText(text)
            build()
        }

        val image = with (Surface.makeRasterN32Premul(200, 64)) {
            canvas.drawRect(Rect(0.0f, 0.0f, width.toFloat(), height.toFloat()), Paint().apply { color = Color.WHITE })
            paragraph.layout(Float.POSITIVE_INFINITY)
            paragraph.paint(canvas, 30.0f, 20.0f)

            makeImageSnapshot()
        }

        screenshots.assert(image)
    }


    @Test
    fun canApplyTextIndent() = runTest {
        if (hostOs != OS.MacOS) {
            return@runTest
        }

        val fontCollection = FontCollection().apply {
            setDefaultFontManager(TypefaceFontProvider().apply {
                val inter = Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf", 0)
                registerTypeface(inter, "Inter")
            })
        }

        val textStyle = TextStyle().apply {
            fontFamilies = arrayOf("Inter")
            fontSize = 28.0f
            color = Color.BLACK
        }

        fun drawParagraph(canvas: Canvas, x: Float, y: Float, dir: Direction, align: Alignment) {
            val paragraphStyle = ParagraphStyle().apply {
                this.textStyle = textStyle
                alignment = align
                direction = dir
                textIndent = TextIndent(80f, 40.0f)
            }

            val paragraph = ParagraphBuilder(paragraphStyle, fontCollection).run {
                val text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do" +
                        " eiusmod tempor incididunt ut labore et dolore magna aliqua."

                addText(text)
                build()
            }
            paragraph.layout(500f)
            paragraph.paint(canvas, x, y)
        }

        val image = with (Surface.makeRasterN32Premul(1200, 800)) {
            canvas.drawRect(Rect(0.0f, 0.0f, width.toFloat(), height.toFloat()), Paint().apply { color = Color.WHITE })


            val linePaint = Paint().apply {
                strokeWidth = 2.0f
                setStroke(true)
                color = Color.makeRGB(128,128, 128)
            }

            for (tick in listOf(50f, 550f, 650f, 1150f)) {
                canvas.drawLine(tick, 0f, tick, height.toFloat(), linePaint)
            }

            var y = 20f
            for (align in listOf(Alignment.LEFT, Alignment.CENTER, Alignment.RIGHT, Alignment.JUSTIFY)) {
                var x = 50f
                for (dir in listOf(Direction.LTR, Direction.RTL)) {
                    drawParagraph(canvas, x, y, dir, align)
                    x += 600f
                }
                y += 200f
            }

            makeImageSnapshot()
        }

        screenshots.assert(image)
    }
}