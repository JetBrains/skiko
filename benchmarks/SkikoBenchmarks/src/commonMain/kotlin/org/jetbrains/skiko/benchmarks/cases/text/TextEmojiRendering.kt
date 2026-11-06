package org.jetbrains.skiko.benchmarks.cases.text

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Paint
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.Paragraph
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider
import kotlin.math.roundToLong

/**
 * Color emoji use pre-rendered RGBA bitmaps (CBDT/CBLC, sbix) bypassing A8 mask
 * expansion and exercising the SkRasterPipeline image blit stage.
 * Validates SIMD doesn't regress the bitmap glyph path.
 */
val textEmojiRenderingBenchmark = BenchmarkCase("text_emoji_rendering") {
    runTextEmojiRendering(RasterBenchmarkSurfaceProvider)
}

val textEmojiRenderingGpuBenchmark = BenchmarkCase("text_emoji_rendering_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runTextEmojiRendering(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runTextEmojiRendering(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val fontCollection = FontCollection()
    fontCollection.setDefaultFontManager(FontMgr.default)
    val paragraphStyle = ParagraphStyle()

    // Mix of color emoji and regular text
    val text = buildString {
        repeat(10) {
            append("\uD83D\uDE00\uD83D\uDE03\uD83D\uDE04\uD83D\uDE01\uD83D\uDE06") // 😀😃😄😁😆
            append(" Hello World ")
            append("\uD83C\uDF1F\uD83C\uDF1E\u2B50\uD83C\uDF19\u2600\uFE0F") // 🌟🌞⭐🌙☀️
            append(" Testing emoji rendering ")
            append("\uD83D\uDC4D\uD83D\uDC4E\uD83D\uDC4B\u270C\uFE0F\uD83E\uDD1E") // 👍👎👋✌️🤞
            append("\n")
        }
    }

    // Pre-layout outside measurement
    val builder = ParagraphBuilder(paragraphStyle, fontCollection)
    builder.addText(text)
    val paragraph = builder.build()
    paragraph.layout(1024f)

    try {
        return surfaceProvider.withSurface(1024, 1024) { surface ->
            Paint().use { paint ->
                val canvas = surface.canvas
                var checksum = 0L

                repeat(40) { frame ->
                    canvas.clear(0xFFFFFFFF.toInt())
                    paragraph.paint(canvas, 0f, 0f)
                    checksum += paragraph.height.roundToLong() + frame
                }
                surface.flushAndSubmit()
                checksum
            }
        }
    } finally {
        paragraph.close()
        paragraphStyle.close()
        fontCollection.close()
    }
}
