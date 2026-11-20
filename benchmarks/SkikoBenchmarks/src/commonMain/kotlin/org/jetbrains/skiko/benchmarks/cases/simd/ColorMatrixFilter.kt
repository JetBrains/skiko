package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Applies grayscale/sepia/contrast 4×5 color matrix filters to a 1024×1024 image.
 * Targets SIMD-accelerated per-pixel matrix multiply in SkRasterPipeline.
 */

import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider
import kotlin.math.roundToLong

val colorMatrixFilterBenchmark = BenchmarkCase("simd_color_matrix_filter") {
    runColorMatrixFilter(RasterBenchmarkSurfaceProvider)
}

val colorMatrixFilterGpuBenchmark = BenchmarkCase("simd_color_matrix_filter_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runColorMatrixFilter(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runColorMatrixFilter(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val sourceImage = makePatternImage(1024, 1024)
    try {
        // Grayscale conversion matrix (4x5 color matrix)
        val grayscaleMatrix = ColorMatrix(
            0.2126f, 0.7152f, 0.0722f, 0f, 0f,
            0.2126f, 0.7152f, 0.0722f, 0f, 0f,
            0.2126f, 0.7152f, 0.0722f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        // Sepia tone matrix
        val sepiaMatrix = ColorMatrix(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        // High contrast matrix
        val contrastMatrix = ColorMatrix(
            1.5f, 0f, 0f, 0f, -0.25f,
            0f, 1.5f, 0f, 0f, -0.25f,
            0f, 0f, 1.5f, 0f, -0.25f,
            0f, 0f, 0f, 1f, 0f
        )

        val filters = listOf(
            ColorFilter.makeMatrix(grayscaleMatrix),
            ColorFilter.makeMatrix(sepiaMatrix),
            ColorFilter.makeMatrix(contrastMatrix),
        )

        try {
            return surfaceProvider.withSurface(1024, 1024) { surface ->
                Paint().use { paint ->
                    val canvas = surface.canvas
                    var checksum = 0L
                    repeat(30) { frame ->
                        canvas.clear(0xFFFFFFFF.toInt())
                        val filter = filters[frame % filters.size]
                        paint.colorFilter = filter
                        canvas.drawImage(sourceImage, 0f, 0f, paint)
                        checksum += frame.toLong() * 31L + filter.hashCode().toLong()
                    }
                    paint.colorFilter = null
                    surface.flushAndSubmit()
                    checksum
                }
            }
        } finally {
            filters.forEach { it.close() }
        }
    } finally {
        sourceImage.close()
    }
}

private fun makePatternImage(width: Int, height: Int): Image {
    return Surface.makeRasterN32Premul(width, height).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            paint.mode = PaintMode.FILL
            for (y in 0 until height step 4) {
                for (x in 0 until width step 4) {
                    val r = (x * 251 + y * 127) and 0xFF
                    val g = (x * 59 + y * 199) and 0xFF
                    val b = (x * 173 + y * 83) and 0xFF
                    paint.color = 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + 4).toFloat(), (y + 4).toFloat(), paint)
                }
            }
            surface.makeImageSnapshot()
        }
    }
}
