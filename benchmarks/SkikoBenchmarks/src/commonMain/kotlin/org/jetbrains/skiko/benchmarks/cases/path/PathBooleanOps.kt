package org.jetbrains.skiko.benchmarks.cases.path

import org.jetbrains.skia.Path
import org.jetbrains.skia.PathOp
import org.jetbrains.skia.RRect
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import kotlin.math.roundToLong

val pathBooleanOpsBenchmark = BenchmarkCase("path_boolean_ops", warmups = 3, iterations = 15) {
    var checksum = 0L
    repeat(160) { index ->
        Path.Circle(64f + (index % 11), 64f, 48f).use { circle ->
            Path.RRect(
                RRect.makeXYWH(32f, 24f + (index % 9), 104f, 86f, 16f, 16f)
            ).use { roundedRect ->
                val op = if (index % 2 == 0) PathOp.INTERSECT else PathOp.UNION
                Path.makeCombining(circle, roundedRect, op)?.use { combined ->
                    checksum += combined.bounds.width.roundToLong() * 17L + combined.bounds.height.roundToLong()
                }
            }
        }
    }
    checksum
}
