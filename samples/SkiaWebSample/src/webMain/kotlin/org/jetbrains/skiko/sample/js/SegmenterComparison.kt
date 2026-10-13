package org.jetbrains.skiko.sample.js

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.Paragraph
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.SkUnicodeClient
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.paragraph.createForText
import org.jetbrains.skiko.SkikoFeatureFlags
import org.jetbrains.skiko.SkikoRenderDelegate

/**
 * Renders the very same text twice, side by side:
 *  - on the left with the ICU data embedded into the Skiko binary,
 *  - on the right with the browser native `Intl.Segmenter`
 *    ([SkikoFeatureFlags.useBrowserSegmenter] = `true`).
 *
 * Both columns are laid out with the same wrapping width, so grapheme, word and line break
 * differences between the two segmentation engines become visible immediately.
 */
internal class SegmenterComparison : SkikoRenderDelegate {
    private val fontCollection = FontCollection().setDefaultFontManager(FontMgr.default)

    private val labelFont: Font? = FontMgr.default
        .matchFamilyStyle("Roboto", FontStyle.BOLD)
        ?.let { Font(it, 20f) }

    private val labelPaint = Paint().apply {
        color = Color.makeRGB(0, 0, 128)
        setStroke(false)
    }

    private var icu: Paragraph? = null
    private var browser: Paragraph? = null

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val columnWidth = (width / 2f) - 2 * PADDING

        if (icu == null) {
            icu = layout(useBrowserSegmenter = false, width = columnWidth)
        }
        if (browser == null) {
            browser = layout(useBrowserSegmenter = true, width = columnWidth)
        }

        canvas.clear(Color.WHITE)
        drawColumn(canvas, PADDING, "ICU (useBrowserSegmenter = false)", icu)
        drawColumn(
            canvas,
            width / 2f + PADDING,
            if (SkUnicodeClient.isSupported) {
                "Intl.Segmenter (useBrowserSegmenter = true)"
            } else {
                "Intl.Segmenter is NOT supported, ICU is used"
            },
            browser
        )
    }

    private fun drawColumn(canvas: Canvas, x: Float, label: String, paragraph: Paragraph?) {
        labelFont?.let { font ->
            TextLine.make(label, font).use { line ->
                canvas.drawTextLine(line, x, PADDING + font.size, labelPaint)
            }
        }
        paragraph?.paint(canvas, x, PADDING + 2 * (labelFont?.size ?: 20f))
    }

    private fun layout(useBrowserSegmenter: Boolean, width: Float): Paragraph {
        val previous = SkikoFeatureFlags.useBrowserSegmenter
        SkikoFeatureFlags.useBrowserSegmenter = useBrowserSegmenter
        try {
            val style = ParagraphStyle().apply {
                textStyle = TextStyle().apply {
                    color = Color.BLACK
                    fontSize = 22f
                }
            }
            return ParagraphBuilder.createForText(style, fontCollection, TEXT, locale = "en").use {
                it.addText(TEXT)
                it.build().layout(width)
            }
        } finally {
            SkikoFeatureFlags.useBrowserSegmenter = previous
        }
    }

    private companion object {
        const val PADDING = 10f

        // Mandatory line breaks, soft wrapping, a grapheme cluster built of several code units
        // and mixed direction text - everything the two engines may disagree about.
        val TEXT = buildString {
            appendLine("Pack my box with five dozen liquor jugs.")
            appendLine("Привет Мир! \uD83D\uDC69\u200D\uD83D\uDCBB e\u0301")
            append("mixed direction: abc \u05D0\u05D1\u05D2 def")
        }
    }
}
