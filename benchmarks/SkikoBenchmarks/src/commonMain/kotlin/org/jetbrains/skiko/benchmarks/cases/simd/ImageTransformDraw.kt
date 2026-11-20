package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Draws 16 images per frame with rotation + scale transforms.
 * Exercises SIMD-accelerated matrix point transforms and image sampling.
 */

import org.jetbrains.skia.Image
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

val imageTransformDrawBenchmark = BenchmarkCase("simd_image_transform_draw") {
    runImageTransformDraw(RasterBenchmarkSurfaceProvider)
}

val imageTransformDrawGpuBenchmark = BenchmarkCase("simd_image_transform_draw_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runImageTransformDraw(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runImageTransformDraw(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val sourceImage = makeCheckerImage(256, 256)
    try {
        return surfaceProvider.withSurface(1024, 1024) { surface ->
            Paint().use { paint ->
                val canvas = surface.canvas
                val src = Rect(0f, 0f, 256f, 256f)
                var checksum = 0L

                repeat(20) { frame ->
                    canvas.clear(0xFFFFFFFF.toInt())

                    repeat(16) { i ->
                        val angle = (frame * 7 + i * 23).toDouble() * kotlin.math.PI / 180.0
                        val scale = 0.3f + (i % 5) * 0.15f
                        val tx = (i % 4) * 256f
                        val ty = (i / 4) * 256f

                        canvas.save()
                        canvas.translate(tx + 128f, ty + 128f)
                        canvas.rotate((angle * 180.0 / kotlin.math.PI).toFloat())
                        canvas.scale(scale, scale)
                        canvas.translate(-128f, -128f)

                        val dst = Rect(0f, 0f, 256f, 256f)
                        canvas.drawImageRect(sourceImage, src, dst, SamplingMode.LINEAR, paint, false)
                        canvas.restore()

                        checksum += (scale * 1000).roundToLong() + i
                    }
                }
                surface.flushAndSubmit()
                checksum
            }
        }
    } finally {
        sourceImage.close()
    }
}

private fun makeCheckerImage(width: Int, height: Int): Image {
    return Surface.makeRasterN32Premul(width, height).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            paint.mode = PaintMode.FILL
            val tileSize = 8
            for (y in 0 until height step tileSize) {
                for (x in 0 until width step tileSize) {
                    if ((x / tileSize + y / tileSize) % 2 == 0) {
                        paint.color = 0xFF000000.toInt()
                    } else {
                        paint.color = 0xFFFF6600.toInt()
                    }
                    canvas.drawRect(
                        x.toFloat(), y.toFloat(),
                        (x + tileSize).toFloat(), (y + tileSize).toFloat(),
                        paint
                    )
                }
            }
            surface.makeImageSnapshot()
        }
    }
}
