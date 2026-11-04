package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

/**
 * Compares SIMD impact across blend modes: SRC (pure overwrite, memory bandwidth bound),
 * SRC_OVER (standard alpha composite), and math-intensive modes (MULTIPLY, SCREEN, OVERLAY).
 * SIMD optimizations often show massive 2-4x gains on complex color-math blend modes.
 */
val blendModesBenchmark = BenchmarkCase("simd_blend_modes") {
    runBlendModes(RasterBenchmarkSurfaceProvider)
}

val blendModesGpuBenchmark = BenchmarkCase("simd_blend_modes_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runBlendModes(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runBlendModes(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val sourceImage = makeBlendTestImage(512, 512, 0)
    val overlayImage = makeBlendTestImage(512, 512, 1)
    val modes = listOf(
        BlendMode.SRC,
        BlendMode.SRC_OVER,
        BlendMode.MULTIPLY,
        BlendMode.SCREEN,
        BlendMode.OVERLAY,
    )

    try {
        return surfaceProvider.withSurface(512, 512) { surface ->
            Paint().use { paint ->
                val canvas = surface.canvas
                var checksum = 0L

                repeat(30) { frame ->
                    for (mode in modes) {
                        canvas.clear(0xFFFFFFFF.toInt())
                        // Draw base image
                        paint.blendMode = BlendMode.SRC
                        canvas.drawImage(sourceImage, 0f, 0f, paint)
                        // Composite overlay with the test blend mode
                        paint.blendMode = mode
                        paint.alpha = 180
                        canvas.drawImage(overlayImage, 0f, 0f, paint)
                        checksum += mode.ordinal.toLong() + frame
                    }
                }
                paint.alpha = 255
                paint.blendMode = BlendMode.SRC_OVER
                surface.flushAndSubmit()
                checksum
            }
        }
    } finally {
        sourceImage.close()
        overlayImage.close()
    }
}

private fun makeBlendTestImage(width: Int, height: Int, seed: Int): Image {
    return Surface.makeRasterN32Premul(width, height).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0x00000000)
            paint.mode = PaintMode.FILL
            val step = 4
            for (y in 0 until height step step) {
                for (x in 0 until width step step) {
                    val r = ((x * 13 + seed * 97 + y) and 0xFF)
                    val g = ((y * 7 + seed * 53 + x) and 0xFF)
                    val b = ((x + y + seed * 31) and 0xFF)
                    val a = 100 + ((x + y + seed * 41) % 156)
                    paint.color = (a shl 24) or (r shl 16) or (g shl 8) or b
                    canvas.drawRect(
                        org.jetbrains.skia.Rect(x.toFloat(), y.toFloat(), (x + step).toFloat(), (y + step).toFloat()),
                        paint
                    )
                }
            }
            surface.makeImageSnapshot()
        }
    }
}
