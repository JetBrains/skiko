package org.jetbrains.skiko.benchmarks.cases.image

import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider
import kotlin.math.roundToLong

val imageScaleDrawBenchmark = BenchmarkCase("image_scale_draw") {
    runImageScaleDraw(RasterBenchmarkSurfaceProvider)
}

val imageScaleDrawGpuBenchmark = BenchmarkCase("image_scale_draw_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runImageScaleDraw(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runImageScaleDraw(surfaceProvider: BenchmarkSurfaceProvider): Long {
    makePatternImage(128, 128).use { image ->
        return surfaceProvider.withSurface(512, 512) { surface ->
            Paint().use { paint ->
                val canvas = surface.canvas
                canvas.clear(0xFFFFFFFF.toInt())
                var checksum = 0L
                val src = Rect(0f, 0f, image.width.toFloat(), image.height.toFloat())
                repeat(180) { index ->
                    val left = (index % 12) * 41f
                    val top = (index / 12) * 31f
                    val dst = Rect(
                        left,
                        top,
                        left + 24f + (index % 5) * 11f,
                        top + 24f + (index % 7) * 9f
                    )
                    canvas.drawImageRect(image, src, dst, SamplingMode.LINEAR, paint, false)
                    checksum += dst.width.roundToLong() * 31L + dst.height.roundToLong()
                }
                surface.flushAndSubmit()
                checksum
            }
        }
    }
}

private fun makePatternImage(width: Int, height: Int): Image {
    return Surface.makeRasterN32Premul(width, height).use { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            paint.mode = PaintMode.FILL
            repeat(height / 8) { row ->
                repeat(width / 8) { col ->
                    paint.color = 0xFF000000.toInt() or ((row * 31) shl 16) or ((col * 37) shl 8) or ((row + col) * 13)
                    canvas.drawRect(col * 8f, row * 8f, col * 8f + 8f, row * 8f + 8f, paint)
                }
            }
            surface.makeImageSnapshot()
        }
    }
}
