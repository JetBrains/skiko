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
 * Tests SIMD pipeline setup/teardown overhead when blitting fragmented 3-8 glyph spans.
 * Simulates UI labels/buttons scattered across the canvas.
 * Blob positions are fixed across runs to avoid profiling transform recomputation.
 */
val textSmallLabelsBenchmark = BenchmarkCase("text_small_labels") {
    runTextSmallLabels(RasterBenchmarkSurfaceProvider)
}

val textSmallLabelsGpuBenchmark = BenchmarkCase("text_small_labels_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runTextSmallLabels(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runTextSmallLabels(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return Font(null, 14f).use { font ->
        // Create 200 short text blobs (3-8 glyphs each) at fixed positions
        val labels = (0 until 200).mapNotNull { i ->
            val glyphCount = 3 + (i % 6) // 3 to 8 glyphs
            val glyphs = ShortArray(glyphCount) { ((it + i * 7) % 58 + 33).toShort() }
            val positions = Array(glyphs.size) { idx ->
                Point(idx * 9f, 0f)
            }
            TextBlob.makeFromPos(glyphs, positions, font)
        }

        // Fixed positions for each label (deterministic, no per-frame recomputation)
        val labelPositions = (0 until labels.size).map { i ->
            val x = ((i * 97) % 980 + 10).toFloat()
            val y = ((i * 53) % 980 + 14).toFloat()
            Point(x, y)
        }

        try {
            surfaceProvider.withSurface(1024, 1024) { surface ->
                Paint().use { paint ->
                    val canvas = surface.canvas
                    paint.color = 0xFF202124.toInt()
                    paint.isAntiAlias = true
                    var checksum = 0L

                    repeat(80) { frame ->
                        canvas.clear(0xFFFFFFFF.toInt())
                        labels.forEachIndexed { index, blob ->
                            val pos = labelPositions[index]
                            canvas.drawTextBlob(blob, pos.x, pos.y, paint)
                            checksum += blob.bounds.width.roundToLong() + index
                        }
                    }
                    surface.flushAndSubmit()
                    checksum
                }
            }
        } finally {
            labels.forEach { it.close() }
        }
    }
}
