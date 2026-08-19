package org.jetbrains.skiko.benchmarks.cases.readback

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase

val surfaceReadPixelsBenchmark = BenchmarkCase("surface_read_pixels", warmups = 3, iterations = 15) {
    Surface.makeRasterN32Premul(256, 256).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            paint.mode = PaintMode.FILL
            repeat(128) { index ->
                paint.color = 0xFF000000.toInt() or (index * 65_537 and 0x00FFFFFF)
                val x = (index % 16) * 16f
                val y = (index / 16) * 32f
                canvas.drawRect(x, y, x + 14f, y + 28f, paint)
            }
            surface.flushAndSubmit()

            Bitmap().use { bitmap ->
                bitmap.allocPixels(ImageInfo.makeN32Premul(256, 256))
                var checksum = 0L
                repeat(40) { index ->
                    check(surface.readPixels(bitmap, 0, 0)) { "Failed to read pixels from surface" }
                    checksum += bitmap.rowBytes + index
                }
                checksum
            }
        }
    }
}
