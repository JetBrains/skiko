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
 * Measures isolated text blitting performance by pre-laying out a multi-line paragraph
 * and benchmarking only the paint (rasterization + blit) phase.
 * Separates HarfBuzz shaping cost from SIMD-accelerated glyph mask blitting.
 */
val textParagraphLayoutBenchmark = BenchmarkCase("text_paragraph_layout") {
    runTextParagraphLayout(RasterBenchmarkSurfaceProvider)
}

val textParagraphLayoutGpuBenchmark = BenchmarkCase("text_paragraph_layout_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runTextParagraphLayout(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runTextParagraphLayout(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val fontCollection = FontCollection()
    fontCollection.setDefaultFontManager(FontMgr.default)
    val paragraphStyle = ParagraphStyle()

    val text = buildString {
        repeat(20) {
            append("Lorem ipsum dolor sit amet, consectetur adipiscing elit. ")
            append("Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ")
        }
    }

    // Pre-layout the paragraph outside the measurement loop
    val builder = ParagraphBuilder(paragraphStyle, fontCollection)
    builder.addText(text)
    val paragraph = builder.build()
    paragraph.layout(1024f)

    try {
        return surfaceProvider.withSurface(1024, 1024) { surface ->
            Paint().use { paint ->
                val canvas = surface.canvas
                var checksum = 0L

                // Benchmark only the paint (blit) phase
                repeat(60) { frame ->
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
