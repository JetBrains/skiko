package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

/**
 * Draws 2000 AA lines at various angles and thicknesses (1-4px).
 * AA lines route through SkAntiAliasingRunBoxer / hairline blitters,
 * isolating whether fractional line edge coverage math was affected by SIMD.
 */
val aaLinesBenchmark = BenchmarkCase("simd_aa_lines") {
    runAALines(RasterBenchmarkSurfaceProvider)
}

val aaLinesGpuBenchmark = BenchmarkCase("simd_aa_lines_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runAALines(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runAALines(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return surfaceProvider.withSurface(1024, 1024) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            paint.isAntiAlias = true
            paint.mode = PaintMode.STROKE
            var checksum = 0L

            repeat(10) { frame ->
                canvas.clear(0xFFFFFFFF.toInt())

                repeat(2000) { i ->
                    val x0 = ((i * 37 + frame * 5) % 1024).toFloat()
                    val y0 = ((i * 53 + frame * 11) % 1024).toFloat()
                    // Vary angle and length
                    val angle = (i * 17 + frame * 3) % 360
                    val length = 20f + (i % 80).toFloat()
                    val rad = angle.toFloat() * 0.01745329f // degrees to radians
                    val x1 = x0 + length * kotlin.math.cos(rad)
                    val y1 = y0 + length * kotlin.math.sin(rad)

                    paint.strokeWidth = 1f + (i % 4).toFloat() // 1-4px
                    val r = (i * 71) and 0xFF
                    val g = (i * 113) and 0xFF
                    val b = (i * 157) and 0xFF
                    paint.color = 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b

                    canvas.drawLine(x0, y0, x1, y1, paint)
                    checksum += (x0 + y0 + x1 + y1).toLong()
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
