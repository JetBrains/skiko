package org.jetbrains.skiko.benchmarks.cases.surface

import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase

val surfaceAllocationBenchmark = BenchmarkCase("surface_allocation") {
    var checksum = 0L
    repeat(50) { index ->
        Surface.makeRasterN32Premul(256, 256).use { surface ->
            surface.canvas.clear(0xFFFFFFFF.toInt())
            checksum += surface.width.toLong() * 31L + surface.height + index
        }
    }
    checksum
}
