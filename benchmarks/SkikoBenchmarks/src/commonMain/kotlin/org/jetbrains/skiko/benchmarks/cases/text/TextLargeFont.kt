package org.jetbrains.skiko.benchmarks.cases.text

import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Point
import org.jetbrains.skia.TextBlob
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider
import kotlin.math.roundToLong

/**
 * Maximizes inner-loop throughput by rendering large glyphs (72pt+).
 * Large glyphs produce large A8/LCD masks, isolating SIMD blitting saturation.
 * Surface is large enough to prevent canvas clipping from terminating scanline loops.
 */
val textLargeFontBenchmark = BenchmarkCase("text_large_font") {
    runTextLargeFont(RasterBenchmarkSurfaceProvider)
}

val textLargeFontGpuBenchmark = BenchmarkCase("text_large_font_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runTextLargeFont(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runTextLargeFont(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return Font(null, 72f).use { font ->
        // Create text blobs with large glyphs across multiple lines
        val blobs = (0 until 8).mapNotNull { row ->
            val glyphs = ShortArray(14) { ((it + row * 5) % 58 + 33).toShort() }
            val positions = Array(glyphs.size) { index ->
                Point(index * 52f, row * 90f + 80f)
            }
            TextBlob.makeFromPos(glyphs, positions, font)
        }

        try {
            surfaceProvider.withSurface(1024, 1024) { surface ->
                Paint().use { paint ->
                    val canvas = surface.canvas
                    paint.color = 0xFF1A1A1A.toInt()
                    paint.isAntiAlias = true
                    var checksum = 0L

                    repeat(60) { frame ->
                        canvas.clear(0xFFFFFFFF.toInt())
                        blobs.forEachIndexed { index, blob ->
                            canvas.drawTextBlob(blob, 0f, 0f, paint)
                            checksum += blob.bounds.width.roundToLong() + index
                        }
                    }
                    surface.flushAndSubmit()
                    checksum
                }
            }
        } finally {
            blobs.forEach { it.close() }
        }
    }
}
