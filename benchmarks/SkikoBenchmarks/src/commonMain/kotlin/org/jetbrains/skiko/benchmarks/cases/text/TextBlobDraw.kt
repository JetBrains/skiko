package org.jetbrains.skiko.benchmarks.cases.text

import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Point
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextBlob
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import kotlin.math.roundToLong

val textBlobDrawBenchmark = BenchmarkCase("text_blob_draw") {
    Font(null, 18f).use { font ->
        val glyphs = ShortArray(96) { ((it % 58) + 33).toShort() }
        val positions = Array(glyphs.size) { index ->
            Point((index % 24) * 20f, (index / 24) * 26f)
        }
        val blob = TextBlob.makeFromPos(glyphs, positions, font) ?: return@BenchmarkCase 0L
        blob.use { textBlob ->
            Surface.makeRasterN32Premul(512, 256).use { surface ->
                Paint().use { paint ->
                    val canvas = surface.canvas
                    canvas.clear(0xFFFFFFFF.toInt())
                    paint.color = 0xFF202124.toInt()
                    var checksum = textBlob.uniqueId.toLong()
                    repeat(220) { index ->
                        canvas.drawTextBlob(textBlob, (index % 9) * 3f, 24f + (index % 7), paint)
                        checksum += textBlob.bounds.width.roundToLong() + index
                    }
                    surface.flushAndSubmit()
                    checksum
                }
            }
        }
    }
}
