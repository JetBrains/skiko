package org.jetbrains.skiko.benchmarks.cases.image

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase

val imageSnapshotEncodeBenchmark = BenchmarkCase("image_snapshot_encode", warmups = 2, iterations = 10) {
    Surface.makeRasterN32Premul(256, 256).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            paint.mode = PaintMode.FILL
            repeat(64) { index ->
                val left = (index % 8) * 32f
                val top = (index / 8) * 32f
                paint.color = 0xFF000000.toInt() or (index * 1_313_131 and 0x00FFFFFF)
                canvas.drawOval(left, top, left + 28f, top + 28f, paint)
            }
            surface.flushAndSubmit()

            var checksum = 0L
            repeat(2) {
                surface.makeImageSnapshot().use { image ->
                    val data = image.encodeToData(EncodedImageFormat.PNG, 100)
                        ?: error("Failed to encode benchmark image")
                    data.use {
                        checksum += it.size
                    }
                }
            }
            checksum
        }
    }
}
