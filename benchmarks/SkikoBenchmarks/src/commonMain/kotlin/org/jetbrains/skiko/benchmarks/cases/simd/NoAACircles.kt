package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

/**
 * Control test: draws 1000 circles with isAntiAlias=false.
 * Non-AA circles route to fast horizontal memset/span runs.
 * No regression here definitively isolates the defect to subpixel
 * coverage mask calculation in the AA path.
 */
val noAACirclesBenchmark = BenchmarkCase("simd_noaa_circles") {
    runNoAACircles(RasterBenchmarkSurfaceProvider)
}

val noAACirclesGpuBenchmark = BenchmarkCase("simd_noaa_circles_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runNoAACircles(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runNoAACircles(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return surfaceProvider.withSurface(1024, 1024) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            paint.isAntiAlias = false
            paint.mode = PaintMode.FILL
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
                    canvas.drawCircle(x, y, radius, paint)
                    checksum += (x + y + radius).toLong()
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
