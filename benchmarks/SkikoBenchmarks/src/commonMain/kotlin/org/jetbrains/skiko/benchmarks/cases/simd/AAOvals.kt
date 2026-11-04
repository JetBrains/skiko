package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

/**
 * Draws 1000 AA ovals with non-uniform radii to test whether the circle
 * regression is tied to circular symmetry optimization or general oval/ellipse AA.
 */
val aaOvalsBenchmark = BenchmarkCase("simd_aa_ovals") {
    runAAOvals(RasterBenchmarkSurfaceProvider)
}

val aaOvalsGpuBenchmark = BenchmarkCase("simd_aa_ovals_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runAAOvals(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runAAOvals(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return surfaceProvider.withSurface(1024, 1024) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            paint.isAntiAlias = true
            paint.mode = PaintMode.FILL
            var checksum = 0L

            repeat(10) { frame ->
                canvas.clear(0xFFFFFFFF.toInt())

                repeat(1000) { i ->
                    val x = ((i * 37 + frame * 13) % 960 + 20).toFloat()
                    val y = ((i * 53 + frame * 7) % 960 + 20).toFloat()
                    val rx = 3f + (i % 15).toFloat() // 3-18px horizontal radius
                    val ry = 5f + ((i + 7) % 20).toFloat() // 5-25px vertical radius
                    val r = (i * 71) and 0xFF
                    val g = (i * 113) and 0xFF
                    val b = (i * 157) and 0xFF
                    val a = 128 + (i % 128)

                    paint.color = (a shl 24) or (r shl 16) or (g shl 8) or b
                    canvas.drawOval(Rect(x - rx, y - ry, x + rx, y + ry), paint)
                    checksum += (x + y + rx + ry).toLong()
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
