package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

/**
 * Definitive breakpoint test: draws AA circles at discrete radius buckets
 * (4, 8, 16, 32, 64, 128, 256) to identify the exact geometric crossover
 * where SIMD lane width outpaces scalar execution.
 * Large circles (50-256px radius) should show SIMD speedup as scanline spans
 * saturate vector register width.
 */
val aaLargeCirclesBenchmark = BenchmarkCase("simd_aa_large_circles") {
    runAALargeCircles(RasterBenchmarkSurfaceProvider)
}

val aaLargeCirclesGpuBenchmark = BenchmarkCase("simd_aa_large_circles_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runAALargeCircles(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runAALargeCircles(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val radii = intArrayOf(4, 8, 16, 32, 64, 128, 256)

    return surfaceProvider.withSurface(1024, 1024) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            paint.isAntiAlias = true
            paint.mode = PaintMode.FILL
            var checksum = 0L

            repeat(15) { frame ->
                canvas.clear(0xFFFFFFFF.toInt())

                for (bucket in radii) {
                    // Draw enough circles per bucket to get measurable time
                    val count = 2000 / bucket.coerceAtLeast(1)
                    repeat(count) { i ->
                        val cx = ((i * 97 + bucket * 13 + frame * 7) % 900 + 50).toFloat()
                        val cy = ((i * 61 + bucket * 29 + frame * 11) % 900 + 50).toFloat()
                        val r = (i * 71 + bucket) and 0xFF
                        val g = (i * 113 + bucket) and 0xFF
                        val b = (i * 157 + bucket) and 0xFF
                        paint.color = 0xC0000000.toInt() or (r shl 16) or (g shl 8) or b
                        canvas.drawCircle(cx, cy, bucket.toFloat(), paint)
                        checksum += (cx + cy + bucket).toLong()
                    }
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
