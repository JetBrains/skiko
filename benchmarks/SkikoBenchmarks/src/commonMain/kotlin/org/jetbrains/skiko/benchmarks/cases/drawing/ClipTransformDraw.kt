package org.jetbrains.skiko.benchmarks.cases.drawing

import org.jetbrains.skia.ClipMode
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase

val clipTransformDrawBenchmark = BenchmarkCase("clip_transform_draw") {
    Surface.makeRasterN32Premul(512, 512).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            paint.mode = PaintMode.FILL
            var checksum = 0L
            repeat(220) { index ->
                val saveCount = canvas.save()
                val x = (index % 16) * 32f
                val y = (index / 16) * 28f
                canvas.translate(x, y)
                canvas.rotate((index % 24) * 3f, 12f, 12f)
                canvas.scale(0.75f + (index % 5) * 0.08f, 0.75f + (index % 7) * 0.06f)
                canvas.clipRect(0f, 0f, 28f, 28f, ClipMode.INTERSECT, true)
                paint.color = 0xFF000000.toInt() or (index * 97_531 and 0x00FFFFFF)
                canvas.drawRRect(0f, 0f, 34f, 34f, floatArrayOf(4f, 4f, 10f, 10f, 4f, 4f, 10f, 10f), paint)
                canvas.restoreToCount(saveCount)
                checksum += paint.color.toLong() and 0xFFFFFFFFL
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}
