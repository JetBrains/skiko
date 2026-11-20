package org.jetbrains.skiko.benchmarks.cases.simd

/**
 * Renders complex vector paths (300 cubic Bézier spiral + 24-point star) with AA.
 * Exercises SIMD-accelerated path coverage computation and scanline blitting.
 */

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

val complexPathRenderingBenchmark = BenchmarkCase("simd_complex_path_rendering") {
    runComplexPathRendering(RasterBenchmarkSurfaceProvider)
}

val complexPathRenderingGpuBenchmark = BenchmarkCase("simd_complex_path_rendering_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runComplexPathRendering(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runComplexPathRendering(surfaceProvider: BenchmarkSurfaceProvider): Long {
    val pathSvg = buildComplexSvgPath()
    val path = Path.makeFromSVGString(pathSvg)
    try {
        return surfaceProvider.withSurface(1024, 1024) { surface ->
            Paint().use { paint ->
                val canvas = surface.canvas
                paint.isAntiAlias = true
                var checksum = 0L

                repeat(15) { frame ->
                    canvas.clear(0xFFFFFFFF.toInt())

                    // Filled path
                    paint.mode = PaintMode.FILL
                    paint.color = 0x80304FFE.toInt()
                    canvas.save()
                    canvas.translate(512f, 512f)
                    canvas.rotate(frame * 3f)
                    canvas.translate(-512f, -512f)
                    canvas.drawPath(path, paint)
                    canvas.restore()

                    // Stroked path
                    paint.mode = PaintMode.STROKE
                    paint.strokeWidth = 2f
                    paint.color = 0xFF1A237E.toInt()
                    canvas.save()
                    canvas.translate(512f, 512f)
                    canvas.rotate(-frame * 5f)
                    canvas.translate(-512f, -512f)
                    canvas.drawPath(path, paint)
                    canvas.restore()

                    checksum += path.pointsCount.toLong() + frame
                }
                surface.flushAndSubmit()
                checksum
            }
        }
    } finally {
        path.close()
    }
}

private fun buildComplexSvgPath(): String {
    val sb = StringBuilder()
    val cx = 512.0
    val cy = 512.0

    // Spiral of cubic Bézier curves (300 segments)
    var angle = 0.0
    var radius = 20.0
    sb.append("M${(cx + radius).fmt()} ${cy.fmt()} ")
    repeat(300) {
        val a0 = angle
        angle += 0.15
        radius += 1.2
        val a1 = a0 + 0.05
        val a2 = a0 + 0.10
        val a3 = angle

        val cp1x = cx + (radius - 10.0) * cos(a1)
        val cp1y = cy + (radius - 10.0) * sin(a1)
        val cp2x = cx + (radius + 10.0) * cos(a2)
        val cp2y = cy + (radius + 10.0) * sin(a2)
        val endx = cx + radius * cos(a3)
        val endy = cy + radius * sin(a3)

        sb.append("C${cp1x.fmt()} ${cp1y.fmt()}, ${cp2x.fmt()} ${cp2y.fmt()}, ${endx.fmt()} ${endy.fmt()} ")
    }

    // Star shape with quadratic curves
    val starPoints = 24
    for (i in 0 until starPoints) {
        val a = (i.toDouble() / starPoints) * 2.0 * kotlin.math.PI
        val r = if (i % 2 == 0) 450.0 else 200.0
        val x = cx + r * cos(a)
        val y = cy + r * sin(a)
        if (i == 0) {
            sb.append("M${x.fmt()} ${y.fmt()} ")
        } else {
            val midA = ((i - 0.5) / starPoints) * 2.0 * kotlin.math.PI
            val midR = if (i % 2 == 0) 280.0 else 350.0
            val mx = cx + midR * cos(midA)
            val my = cy + midR * sin(midA)
            sb.append("Q${mx.fmt()} ${my.fmt()}, ${x.fmt()} ${y.fmt()} ")
        }
    }
    sb.append("Z")

    return sb.toString()
}

private fun Double.fmt(): String = ((this * 100).roundToLong() / 100.0).toString()
