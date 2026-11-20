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
 * Validates SIMD benefit across glyph complexities by rendering text mixing
 * Latin, CJK, and Arabic scripts. Pre-layouts text to isolate coverage mask
 * generation and blitting from HarfBuzz shaping overhead.
 */
val textMixedScriptsBenchmark = BenchmarkCase("text_mixed_scripts") {
    runTextMixedScripts(RasterBenchmarkSurfaceProvider)
}

val textMixedScriptsGpuBenchmark = BenchmarkCase("text_mixed_scripts_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runTextMixedScripts(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runTextMixedScripts(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val fontCollection = FontCollection()
    fontCollection.setDefaultFontManager(FontMgr.default)
    val paragraphStyle = ParagraphStyle()

    // Mix of Latin, CJK (Chinese/Japanese), and Arabic scripts
    val text = buildString {
        repeat(8) {
            append("The quick brown fox jumps over the lazy dog. ")
            append("\u4E16\u754C\u4F60\u597D\u3002\u6211\u4EEC\u6B63\u5728\u6D4B\u8BD5\u6587\u672C\u6E32\u67D3\u3002") // 世界你好。我们正在测试文本渲染。
            append("\u0645\u0631\u062D\u0628\u0627 \u0628\u0627\u0644\u0639\u0627\u0644\u0645. ") // مرحبا بالعالم
            append("Pack my box with five dozen liquor jugs. ")
            append("\u65E5\u672C\u8A9E\u306E\u30C6\u30AD\u30B9\u30C8\u3082\u542B\u307E\u308C\u3066\u3044\u307E\u3059\u3002") // 日本語のテキストも含まれています。
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
