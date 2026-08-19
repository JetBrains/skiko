package org.jetbrains.skiko.benchmarks.cases.path

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import kotlin.math.roundToLong

val pathParseAndDrawBenchmark = BenchmarkCase("path_parse_and_draw") {
    Surface.makeRasterN32Premul(512, 512).use { surface ->
        Paint().use { paint ->
            paint.mode = PaintMode.STROKE
            paint.strokeWidth = 2f
            paint.color = 0xFF1769AA.toInt()

            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            var checksum = 0L
            repeat(200) { index ->
                Path.makeFromSVGString(complexPath(index)).use { path ->
                    canvas.drawPath(path, paint)
                    checksum += index.toLong() * 17L + path.bounds.width.roundToLong()
                }
            }
            surface.flushAndSubmit()
            checksum
        }
    }
}

private fun complexPath(seed: Int): String {
    val x = (seed % 23) + 8
    val y = (seed % 19) + 8
    return "M$x $y C${x + 35} ${y + 4}, ${x + 46} ${y + 80}, ${x + 96} ${y + 84} " +
        "S${x + 172} ${y + 132}, ${x + 224} ${y + 40} L${x + 246} ${y + 132} Z"
}
