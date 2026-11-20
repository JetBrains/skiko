package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Draws 1000 small anti-aliased circles (radius 2-11px) per frame.
 * Targets SIMD coverage mask generation in SkScan_AAAPath.
 */

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

val antiAliasedCirclesBenchmark = BenchmarkCase("simd_aa_circles") {
    runAntiAliasedCircles(RasterBenchmarkSurfaceProvider)
}

val antiAliasedCirclesGpuBenchmark = BenchmarkCase("simd_aa_circles_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runAntiAliasedCircles(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runAntiAliasedCircles(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return surfaceProvider.withSurface(1024, 1024) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            paint.isAntiAlias = true
            var checksum = 0L

            repeat(10) { frame ->
                canvas.clear(0xFFFFFFFF.toInt())

                repeat(1000) { i ->
                    val x = ((i * 37 + frame * 13) % 1000 + 12).toFloat()
                    val y = ((i * 53 + frame * 7) % 1000 + 12).toFloat()
                    val radius = 4f + (i % 20).toFloat()
                    val r = (i * 71) and 0xFF
                    val g = (i * 113) and 0xFF
                    val b = (i * 157) and 0xFF
                    val a = 128 + (i % 128)

                    paint.color = (a shl 24) or (r shl 16) or (g shl 8) or b

                    // Alternate between filled and stroked circles
                    if (i % 3 == 0) {
                        paint.mode = PaintMode.STROKE
                        paint.strokeWidth = 1.5f
                    } else {
                        paint.mode = PaintMode.FILL
                    }

                    canvas.drawCircle(x, y, radius, paint)
                    checksum += (x + y + radius).toLong()
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
