package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Renders 20 text blobs × 80 glyphs × 30 frames to stress glyph mask blitting at scale.
 * Exercises SIMD-accelerated blit_mask paths in Skia's raster pipeline.
 */

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

val bulkTextRenderingBenchmark = BenchmarkCase("simd_bulk_text_rendering") {
    runBulkTextRendering(RasterBenchmarkSurfaceProvider)
}

val bulkTextRenderingGpuBenchmark = BenchmarkCase("simd_bulk_text_rendering_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runBulkTextRendering(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runBulkTextRendering(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return Font(null, 14f).use { font ->
        // Create multiple text blobs simulating a dense text document
        val blobs = (0 until 20).mapNotNull { row ->
            val glyphs = ShortArray(80) { ((it % 58) + 33).toShort() }
            val positions = Array(glyphs.size) { index ->
                Point(index * 10f, row * 22f + 16f)
            }
            TextBlob.makeFromPos(glyphs, positions, font)
        }

        try {
            surfaceProvider.withSurface(1024, 512) { surface ->
                Paint().use { paint ->
                    val canvas = surface.canvas
                    var checksum = 0L

                    repeat(30) { frame ->
                        canvas.clear(0xFFFFFFFF.toInt())
                        paint.color = 0xFF202124.toInt()
                        paint.isAntiAlias = true

                        blobs.forEachIndexed { index, blob ->
                            // Slight offset per frame to exercise different sub-pixel positions
                            val offsetX = (frame % 4) * 0.25f
                            val offsetY = (frame % 3) * 0.33f
                            canvas.drawTextBlob(blob, offsetX, offsetY, paint)
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
