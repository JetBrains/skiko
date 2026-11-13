package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.RRect
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

/**
 * Draws 1000 AA rounded rectangles (similar sizes to circles: 4-24px).
 * If this does NOT regress while circles do, the penalty is isolated to
 * Skia's analytic circular coverage math (SkScan_AntiEllipse).
 */
val aaRoundedRectsBenchmark = BenchmarkCase("simd_aa_rounded_rects") {
    runAARoundedRects(RasterBenchmarkSurfaceProvider)
}

val aaRoundedRectsGpuBenchmark = BenchmarkCase("simd_aa_rounded_rects_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runAARoundedRects(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runAARoundedRects(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return surfaceProvider.withSurface(1024, 1024) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            paint.isAntiAlias = true
            paint.mode = PaintMode.FILL
            var checksum = 0L

            repeat(10) { frame ->
                canvas.clear(0xFFFFFFFF.toInt())

                repeat(1000) { i ->
                    val x = ((i * 37 + frame * 13) % 980 + 10).toFloat()
                    val y = ((i * 53 + frame * 7) % 980 + 10).toFloat()
                    val size = 4f + (i % 20).toFloat() // 4-24px, matching circle radii
                    val radius = size * 0.3f
                    val r = (i * 71) and 0xFF
                    val g = (i * 113) and 0xFF
                    val b = (i * 157) and 0xFF
                    val a = 128 + (i % 128)

                    paint.color = (a shl 24) or (r shl 16) or (g shl 8) or b
                    canvas.drawRRect(
                        RRect.makeXYWH(x, y, size * 2, size * 2, radius),
                        paint
                    )
                    checksum += (x + y + size).toLong()
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
