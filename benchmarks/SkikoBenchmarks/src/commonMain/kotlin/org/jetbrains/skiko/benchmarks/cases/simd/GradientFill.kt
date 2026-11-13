package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Fills large rectangles with linear, radial, and sweep gradients (8 color stops).
 * Exercises SIMD-accelerated color interpolation in SkRasterPipeline.
 */

import org.jetbrains.skia.Color4f
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Gradient
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Point
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

val gradientFillBenchmark = BenchmarkCase("simd_gradient_fill") {
    runGradientFill(RasterBenchmarkSurfaceProvider)
}

val gradientFillGpuBenchmark = BenchmarkCase("simd_gradient_fill_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runGradientFill(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runGradientFill(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val stops = arrayOf(
        Color4f(1f, 0f, 0f, 1f),
        Color4f(1f, 0.5f, 0f, 1f),
        Color4f(1f, 1f, 0f, 1f),
        Color4f(0f, 1f, 0f, 1f),
        Color4f(0f, 0.5f, 1f, 1f),
        Color4f(0f, 0f, 1f, 1f),
        Color4f(0.5f, 0f, 1f, 1f),
        Color4f(1f, 0f, 0.5f, 1f),
    )
    val positions = floatArrayOf(0f, 0.14f, 0.28f, 0.42f, 0.57f, 0.71f, 0.85f, 1f)

    return surfaceProvider.withSurface(1024, 1024) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            var checksum = 0L
            paint.mode = PaintMode.FILL

            repeat(25) { frame ->
                canvas.clear(0xFFFFFFFF.toInt())

                // Linear gradient
                val linearGradient = Gradient(
                    Gradient.Colors(stops, positions, FilterTileMode.CLAMP)
                )
                Shader.makeLinearGradient(
                    Point(0f, 0f), Point(1024f, 512f), linearGradient
                ).use { shader ->
                    paint.shader = shader
                    canvas.drawRect(Rect(0f, 0f, 1024f, 512f), paint)
                    checksum += shader.hashCode().toLong()
                }

                // Radial gradient
                val radialGradient = Gradient(
                    Gradient.Colors(stops, positions, FilterTileMode.MIRROR)
                )
                Shader.makeRadialGradient(
                    Point(512f, 768f), 400f, radialGradient
                ).use { shader ->
                    paint.shader = shader
                    canvas.drawRect(Rect(0f, 512f, 1024f, 1024f), paint)
                    checksum += shader.hashCode().toLong()
                }

                // Sweep gradient
                val sweepGradient = Gradient(
                    Gradient.Colors(stops, positions, FilterTileMode.CLAMP)
                )
                Shader.makeSweepGradient(
                    Point(512f, 256f), sweepGradient
                ).use { shader ->
                    paint.shader = shader
                    canvas.drawOval(Rect(256f, 0f, 768f, 512f), paint)
                    checksum += shader.hashCode().toLong()
                }

                paint.shader = null
                checksum += frame.toLong()
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
