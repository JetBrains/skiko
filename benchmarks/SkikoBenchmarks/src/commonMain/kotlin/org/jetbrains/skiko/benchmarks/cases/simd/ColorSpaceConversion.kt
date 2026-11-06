package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Reads pixels from an sRGB surface into a Display P3 bitmap repeatedly.
 * Exercises SIMD-accelerated gamma curve and color space transform in SkOpts::color_xform.
 */

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

val colorSpaceConversionBenchmark = BenchmarkCase("simd_color_space_conversion") {
    runColorSpaceConversion()
}

private fun runColorSpaceConversion(): Long {
    // Create a source surface in sRGB
    return Surface.makeRasterN32Premul(512, 512).use { srcSurface ->
        Paint().use { paint ->
            val canvas = srcSurface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            paint.mode = PaintMode.FILL
            // Fill with varied colors
            for (y in 0 until 512 step 4) {
                for (x in 0 until 512 step 4) {
                    val r = (x * 251 + y * 127) and 0xFF
                    val g = (x * 59 + y * 199) and 0xFF
                    val b = (x * 173 + y * 83) and 0xFF
                    paint.color = 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + 4).toFloat(), (y + 4).toFloat(), paint)
                }
            }

            val sourceImage = srcSurface.makeImageSnapshot()
            try {
                // Read pixels back with different color space (triggers sRGB <-> linear conversion)
                Bitmap().use { bitmap ->
                    val displayP3 = ColorSpace.displayP3
                    val targetInfo = ImageInfo(
                        512, 512,
                        ColorType.RGBA_8888,
                        ColorAlphaType.PREMUL,
                        displayP3,
                    )
                    bitmap.allocPixels(targetInfo)

                    var checksum = 0L
                    repeat(60) { index ->
                        srcSurface.readPixels(bitmap, 0, 0)
                        checksum += bitmap.rowBytes.toLong() + index
                    }
                    checksum
                }
            } finally {
                sourceImage.close()
            }
        }
    }
}
