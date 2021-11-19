package org.jetbrains.skiko.tests.org.jetbrains.skiko.paragraph

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.junit.Rule
import org.junit.Test

class TextStyleTest {
    @get:Rule
    val screenshots = ScreenshotTestRule()

    @Test
    fun canApplyBaselineShift() = runTest {
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
}