package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Alternates unpremul/premul pixel readback on a 512×512 surface.
 * Exercises SIMD-accelerated per-pixel alpha multiply/divide operations.
 */

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase

val premultiplyReadbackBenchmark = BenchmarkCase("simd_premultiply_readback", warmups = 3, iterations = 15) {
    runPremultiplyReadback()
}

private fun runPremultiplyReadback(): Long {
    return Surface.makeRasterN32Premul(512, 512).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0x00000000)
            paint.mode = PaintMode.FILL

            // Draw semi-transparent content (premultiplied alpha)
            for (y in 0 until 512 step 4) {
                for (x in 0 until 512 step 4) {
                    val r = (x * 131 + y * 97) and 0xFF
                    val g = (x * 67 + y * 211) and 0xFF
                    val b = (x * 179 + y * 53) and 0xFF
                    val a = 64 + ((x + y) % 192)
                    paint.color = (a shl 24) or (r shl 16) or (g shl 8) or b
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + 4).toFloat(), (y + 4).toFloat(), paint)
                }
            }

            // Read as unpremultiplied (forces per-pixel divide by alpha)
            Bitmap().use { unpremulBitmap ->
                unpremulBitmap.allocPixels(
                    ImageInfo(512, 512, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL)
                )

                // Read as premultiplied
                Bitmap().use { premulBitmap ->
                    premulBitmap.allocPixels(
                        ImageInfo(512, 512, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
                    )

                    var checksum = 0L
                    repeat(40) { index ->
                        // Unpremul readback (per-pixel alpha division — SIMD vectorized)
                        surface.readPixels(unpremulBitmap, 0, 0)
                        // Premul readback (per-pixel alpha multiply — SIMD vectorized)
                        surface.readPixels(premulBitmap, 0, 0)
                        checksum += unpremulBitmap.rowBytes.toLong() + premulBitmap.rowBytes.toLong() + index
                    }
                    checksum
                }
            }
        }
    }
}
