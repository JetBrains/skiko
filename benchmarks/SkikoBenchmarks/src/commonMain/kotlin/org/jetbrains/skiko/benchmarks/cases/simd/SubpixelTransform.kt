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
 * Tests integer-aligned vs subpixel-fractional coordinates to ensure SIMD edge paths
 * do not degrade when snapping occurs. Small shapes drawn with fractional coordinates
 * (canvas.translate(0.5f, 0.5f)) force subpixel AA filtering, while integer alignment
 * can take faster blit shortcuts.
 */
val subpixelTransformBenchmark = BenchmarkCase("simd_subpixel_transform") {
    runSubpixelTransform(RasterBenchmarkSurfaceProvider)
}

val subpixelTransformGpuBenchmark = BenchmarkCase("simd_subpixel_transform_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runSubpixelTransform(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runSubpixelTransform(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return surfaceProvider.withSurface(1024, 1024) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            paint.isAntiAlias = true
            paint.mode = PaintMode.FILL
            var checksum = 0L

            repeat(15) { frame ->
                canvas.clear(0xFFFFFFFF.toInt())

                // Phase 1: Integer-aligned coordinates (fast blit path)
                repeat(500) { i ->
                    val x = ((i * 37) % 980 + 10).toFloat()
                    val y = ((i * 53) % 480 + 10).toFloat()
                    val size = 8f + (i % 16).toFloat()
                    val r = (i * 71) and 0xFF
                    val g = (i * 113) and 0xFF
                    val b = (i * 157) and 0xFF
                    paint.color = 0xC0000000.toInt() or (r shl 16) or (g shl 8) or b

                    // Integer-aligned: no subpixel AA needed
                    canvas.drawCircle(x, y, size, paint)
                    canvas.drawRect(Rect(x, y, x + size * 2, y + size), paint)
                    checksum += (x + y).toLong()
                }

                // Phase 2: Subpixel-fractional coordinates (forces AA filtering)
                canvas.save()
                canvas.translate(0.37f, 0.73f) // Non-trivial fractional offset
                repeat(500) { i ->
                    val x = ((i * 37) % 980 + 10).toFloat()
                    val y = ((i * 53) % 480 + 520).toFloat()
                    val size = 8f + (i % 16).toFloat()
                    val r = (i * 71) and 0xFF
                    val g = (i * 113) and 0xFF
                    val b = (i * 157) and 0xFF
                    paint.color = 0xC0000000.toInt() or (r shl 16) or (g shl 8) or b

                    // Fractional offset: subpixel AA filtering required
                    canvas.drawCircle(x, y, size, paint)
                    canvas.drawRect(Rect(x, y, x + size * 2, y + size), paint)
                    checksum += (x + y).toLong() + 1
                }
                canvas.restore()
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
