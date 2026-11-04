package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

/**
 * Aggregate balance test: mix of 300 circles + 300 rects + 300 rounded rects + 100 ovals,
 * all anti-aliased. Final regression gate for real-world mixed-shape workloads.
 */
val mixedShapesAABenchmark = BenchmarkCase("simd_mixed_shapes_aa") {
    runMixedShapesAA(RasterBenchmarkSurfaceProvider)
}

val mixedShapesAAGpuBenchmark = BenchmarkCase("simd_mixed_shapes_aa_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runMixedShapesAA(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runMixedShapesAA(surfaceProvider: BenchmarkSurfaceProvider): Long {
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
                    val size = 6f + (i % 18).toFloat()
                    val r = (i * 71) and 0xFF
                    val g = (i * 113) and 0xFF
                    val b = (i * 157) and 0xFF
                    val a = 128 + (i % 128)
                    paint.color = (a shl 24) or (r shl 16) or (g shl 8) or b

                    when (i % 4) {
                        0 -> canvas.drawCircle(x, y, size, paint) // 250 circles
                        1 -> canvas.drawRect(Rect(x, y, x + size * 2, y + size * 2), paint) // 250 rects
                        2 -> canvas.drawRRect(
                            RRect.makeXYWH(x, y, size * 2, size * 2, size * 0.3f), paint
                        ) // 250 rounded rects
                        3 -> canvas.drawOval(
                            Rect(x - size, y - size * 0.7f, x + size, y + size * 0.7f), paint
                        ) // 250 ovals
                    }
                    checksum += (x + y + size).toLong()
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
