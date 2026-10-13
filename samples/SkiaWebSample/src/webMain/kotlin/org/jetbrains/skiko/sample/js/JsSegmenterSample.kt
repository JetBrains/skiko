package org.jetbrains.skiko.sample.js

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.Paragraph
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.SkUnicodeClient
import org.jetbrains.skia.paragraph.SkikoUnicodeBackend
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.paragraph.createForText
import org.jetbrains.skiko.SkikoFeatureFlags
import org.jetbrains.skiko.SkikoRenderDelegate

/**
 * Lays out and renders multi-line, mixed-direction text using **only** the browser native
 * text segmentation (`Intl.Segmenter` via [SkUnicodeClient]).
 *
 * This is the mandatory path for an ICU-less `skiko.wasm` (built with
 * `-Pskiko.wasm.useIcu=false`): there is no ICU break iterator in the binary, so all
 * grapheme, word and line boundaries come from JS.
 */
internal class JsSegmenterSample : SkikoRenderDelegate {
    private val fontCollection = FontCollection().setDefaultFontManager(FontMgr.default)

    private val labelFont: Font? = FontMgr.default
        .matchFamilyStyle("Roboto", FontStyle.BOLD)
        ?.let { Font(it, 20f) }

    private val labelPaint = Paint().apply {
        color = Color.makeRGB(0, 0, 128)
        setStroke(false)
    }

    private var paragraph: Paragraph? = null
    private var laidOutWidth = -1f

    init {
        // Always segment with the browser: there is no ICU fallback in this build.
        SkikoFeatureFlags.useBrowserSegmenter = true
        check(SkUnicodeClient.isSupported) {
            "Intl.Segmenter is not supported by this engine, and ICU is excluded from this Skiko binary"
        }
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentWidth = width - 2 * PADDING
        if (paragraph == null || laidOutWidth != contentWidth) {
            paragraph = layout(contentWidth)
            laidOutWidth = contentWidth
        }

        canvas.clear(Color.WHITE)

        val label = "Intl.Segmenter (ICU linked: ${SkikoUnicodeBackend.isIcuAvailable})"
        // drawString() instead of TextLine: TextLine.make() requires a process wide
        // SkUnicode, which is not available in an ICU-less skiko.wasm.
        labelFont?.let { font ->
            canvas.drawString(label, PADDING, PADDING + font.size, font, labelPaint)
        }
        paragraph?.paint(canvas, PADDING, PADDING + 2 * (labelFont?.size ?: 20f))
    }

    private fun layout(width: Float): Paragraph {
        val style = ParagraphStyle().apply {
            textStyle = TextStyle().apply {
                color = Color.BLACK
                fontSize = 22f
            }
        }
        // createForText() picks SkUnicodeClient (Intl.Segmenter) automatically.
        return ParagraphBuilder.createForText(style, fontCollection, TEXT, locale = "en").use {
            it.addText(TEXT)
            it.build().layout(width)
        }
    }

    private companion object {
        const val PADDING = 10f

        // Mandatory line breaks, soft wrapping, a multi-code-unit grapheme cluster
        // and mixed direction text (BiDi is resolved by Skiko's embedded UAX #9).
        val TEXT = buildString {
            appendLine("Pack my box with five dozen liquor jugs.")
            appendLine("Привет Мир! \uD83D\uDC69\u200D\uD83D\uDCBB e\u0301")
            append("mixed direction: abc \u05D0\u05D1\u05D2 def")
        }
    }
}
