package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Composites 12 semi-transparent layers using SrcOver blending.
 * Exercises Skia's SIMD-optimized Porter-Duff blend path (blit_row_s32a_opaque).
 */

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

val alphaCompositingBenchmark = BenchmarkCase("simd_alpha_compositing") {
    runAlphaCompositing(RasterBenchmarkSurfaceProvider)
}

val alphaCompositingGpuBenchmark = BenchmarkCase("simd_alpha_compositing_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runAlphaCompositing(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runAlphaCompositing(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val layers = Array(12) { index ->
        makeColorLayer(512, 512, index)
    }
    try {
        return surfaceProvider.withSurface(512, 512) { surface ->
            Paint().use { paint ->
                val canvas = surface.canvas
                var checksum = 0L
                repeat(20) { frame ->
                    canvas.clear(0xFFFFFFFF.toInt())
                    paint.blendMode = BlendMode.SRC_OVER
                    layers.forEachIndexed { index, layer ->
                        paint.alpha = (80 + (index * 15 + frame * 3) % 176)
                        canvas.drawImage(layer, 0f, 0f, paint)
                        checksum += paint.alpha.toLong()
                    }
                    surface.flushAndSubmit()
                }
                checksum
            }
        }
    } finally {
        layers.forEach { it.close() }
    }
}

private fun makeColorLayer(width: Int, height: Int, seed: Int): Image {
    return Surface.makeRasterN32Premul(width, height).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0x00000000)
            paint.mode = PaintMode.FILL
            val step = 8
            for (y in 0 until height step step) {
                for (x in 0 until width step step) {
                    val r = ((x * 7 + seed * 53) and 0xFF)
                    val g = ((y * 11 + seed * 37) and 0xFF)
                    val b = ((x + y + seed * 19) and 0xFF)
                    val a = 128 + ((x + y + seed * 23) % 128)
                    paint.color = (a shl 24) or (r shl 16) or (g shl 8) or b
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + step).toFloat(), (y + step).toFloat(), paint)
                }
            }
            surface.makeImageSnapshot()
        }
    }
}
