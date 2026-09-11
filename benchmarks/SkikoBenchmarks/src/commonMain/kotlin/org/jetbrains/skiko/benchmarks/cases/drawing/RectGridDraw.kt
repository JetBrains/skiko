package org.jetbrains.skiko.benchmarks.cases.drawing

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

val rectGridDrawBenchmark = BenchmarkCase("rect_grid_draw") {
    runRectGridDraw(RasterBenchmarkSurfaceProvider)
}

val rectGridDrawGpuBenchmark = BenchmarkCase("rect_grid_draw_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runRectGridDraw(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runRectGridDraw(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return surfaceProvider.withSurface(512, 512) { surface ->
        Paint().use { paint ->
            paint.mode = PaintMode.FILL
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            var checksum = 0L
            repeat(30) { frame ->
                repeat(32) { row ->
                    repeat(32) { col ->
                        val left = col * 16f
                        val top = row * 16f
                        paint.color = 0xFF000000.toInt() or ((row * 7) shl 16) or ((col * 5) shl 8) or frame
                        canvas.drawRect(left, top, left + 12f, top + 12f, paint)
                        checksum += paint.color.toLong() and 0xFFFFFFFFL
                    }
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
