package org.jetbrains.skiko.benchmarks.cases.drawing

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase

val rectGridDrawBenchmark = BenchmarkCase("rect_grid_draw") {
    Surface.makeRasterN32Premul(512, 512).use { surface ->
        Paint().use { paint ->
            paint.mode = PaintMode.FILL
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            var checksum = 0L
            repeat(30) { frame ->
                repeat(32) { row ->
                    repeat(32) { col ->
                        val left = col * 16f
                        val top = row * 16f
                        paint.color = 0xFF000000.toInt() or ((row * 7) shl 16) or ((col * 5) shl 8) or frame
                        canvas.drawRect(left, top, left + 12f, top + 12f, paint)
                        checksum += paint.color.toLong() and 0xFFFFFFFFL
                    }
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
