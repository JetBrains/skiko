package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Decodes synthetic 1024×1024 PNG and JPEG images repeatedly.
 * Targets SIMD-accelerated defiltering (PNG) and IDCT/color conversion (JPEG).
 */

import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

val imageDecodePngBenchmark = BenchmarkCase("simd_image_decode_png", warmups = 3, iterations = 15) {
    runImageDecode(EncodedImageFormat.PNG)
}

val imageDecodeJpegBenchmark = BenchmarkCase("simd_image_decode_jpeg", warmups = 3, iterations = 15) {
    runImageDecode(EncodedImageFormat.JPEG)
}

private fun runImageDecode(format: EncodedImageFormat): Long {
    // Create a colorful source image and encode it
    val encodedBytes = Surface.makeRasterN32Premul(1024, 1024).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            paint.mode = PaintMode.FILL
            for (y in 0 until 1024 step 4) {
                for (x in 0 until 1024 step 4) {
                    val r = (x * 251 + y * 127) and 0xFF
                    val g = (x * 59 + y * 199) and 0xFF
                    val b = (x * 173 + y * 83) and 0xFF
                    paint.color = 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + 4).toFloat(), (y + 4).toFloat(), paint)
                }
            }
            val image = surface.makeImageSnapshot()
            try {
                val data = image.encodeToData(format, 90) ?: error("Failed to encode image")
                data.bytes
            } finally {
                image.close()
            }
        }
    }

    // Decode repeatedly and draw to surface (exercises SIMD defiltering/color conversion)
    return RasterBenchmarkSurfaceProvider.withSurface(1024, 1024) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            var checksum = 0L
            repeat(20) { index ->
                Image.makeFromEncoded(encodedBytes).use { decoded ->
                    canvas.clear(0xFFFFFFFF.toInt())
                    canvas.drawImage(decoded, 0f, 0f, paint)
                    checksum += decoded.width.toLong() * decoded.height.toLong() + index
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
