package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

/**
 * Parses and renders a grid of 50 SVG icons (from path strings).
 * Tests SkPath rasterization (cubic/conic curve subdivision and edge-list sorting).
 * Path flattening is largely branch-heavy scalar code; vector blitting only
 * accelerates the resulting scanline spans.
 */
val svgIconGridBenchmark = BenchmarkCase("svg_icon_grid") {
    runSVGIconGrid(RasterBenchmarkSurfaceProvider)
}

val svgIconGridGpuBenchmark = BenchmarkCase("svg_icon_grid_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runSVGIconGrid(GpuBenchmarkSurfaceProvider.get()!!)
}

// Common Material Design-style icon SVG paths (24x24 viewbox)
private val iconSvgPaths = listOf(
    // Home
    "M10,20 L10,14 L14,14 L14,20 L19,20 L19,11 L22,11 L12,2 L2,11 L5,11 L5,20 Z",
    // Search
    "M15.5,14 L14.71,14 L14.43,13.73 C15.41,12.59 16,11.11 16,9.5 C16,5.91 13.09,3 9.5,3 C5.91,3 3,5.91 3,9.5 C3,13.09 5.91,16 9.5,16 C11.11,16 12.59,15.41 13.73,14.43 L14,14.71 L14,15.5 L19,20.49 L20.49,19 Z M9.5,14 C7.01,14 5,11.99 5,9.5 C5,7.01 7.01,5 9.5,5 C11.99,5 14,7.01 14,9.5 C14,11.99 11.99,14 9.5,14 Z",
    // Settings gear
    "M19.14,12.94 C19.18,12.64 19.2,12.33 19.2,12 C19.2,11.68 19.18,11.36 19.13,11.06 L21.16,9.48 C21.34,9.34 21.39,9.07 21.28,8.87 L19.36,5.55 C19.24,5.33 18.99,5.26 18.77,5.33 L16.38,6.29 C15.88,5.91 15.35,5.59 14.76,5.35 L14.4,2.81 C14.36,2.57 14.16,2.4 13.92,2.4 L10.08,2.4 C9.84,2.4 9.65,2.57 9.61,2.81 L9.25,5.35 C8.66,5.59 8.12,5.92 7.63,6.29 L5.24,5.33 C5.02,5.25 4.77,5.33 4.65,5.55 L2.74,8.87 C2.62,9.08 2.66,9.34 2.86,9.48 L4.89,11.06 C4.84,11.36 4.8,11.69 4.8,12 C4.8,12.31 4.82,12.64 4.87,12.94 L2.84,14.52 C2.66,14.66 2.61,14.93 2.72,15.13 L4.64,18.45 C4.76,18.67 5.01,18.74 5.23,18.67 L7.62,17.71 C8.12,18.09 8.65,18.41 9.24,18.65 L9.6,21.19 C9.65,21.43 9.84,21.6 10.08,21.6 L13.92,21.6 C14.16,21.6 14.36,21.43 14.39,21.19 L14.75,18.65 C15.34,18.41 15.88,18.09 16.37,17.71 L18.76,18.67 C18.98,18.75 19.23,18.67 19.35,18.45 L21.27,15.13 C21.39,14.91 21.34,14.66 21.15,14.52 Z M12,15.6 C10.02,15.6 8.4,13.98 8.4,12 C8.4,10.02 10.02,8.4 12,8.4 C13.98,8.4 15.6,10.02 15.6,12 C15.6,13.98 13.98,15.6 12,15.6 Z",
    // Star
    "M12,17.27 L18.18,21 L16.54,13.97 L22,9.24 L14.81,8.63 L12,2 L9.19,8.63 L2,9.24 L7.46,13.97 L5.82,21 Z",
    // Heart
    "M12,21.35 L10.55,20.03 C5.4,15.36 2,12.28 2,8.5 C2,5.42 4.42,3 7.5,3 C9.24,3 10.91,3.81 12,5.09 C13.09,3.81 14.76,3 16.5,3 C19.58,3 22,5.42 22,8.5 C22,12.28 18.6,15.36 13.45,20.04 Z",
    // Menu
    "M3,18 L21,18 L21,16 L3,16 Z M3,13 L21,13 L21,11 L3,11 Z M3,6 L3,8 L21,8 L21,6 Z",
    // Check
    "M9,16.17 L4.83,12 L3.41,13.41 L9,19 L21,7 L19.59,5.59 Z",
    // Close
    "M19,6.41 L17.59,5 L12,10.59 L6.41,5 L5,6.41 L10.59,12 L5,17.59 L6.41,19 L12,13.41 L17.59,19 L19,17.59 L13.41,12 Z",
    // Arrow forward
    "M12,4 L10.59,5.41 L16.17,11 L4,11 L4,13 L16.17,13 L10.59,18.59 L12,20 L20,12 Z",
    // Person
    "M12,12 C14.21,12 16,10.21 16,8 C16,5.79 14.21,4 12,4 C9.79,4 8,5.79 8,8 C8,10.21 9.79,12 12,12 Z M12,14 C9.33,14 4,15.34 4,18 L4,20 L20,20 L20,18 C20,15.34 14.67,14 12,14 Z",
)

private fun runSVGIconGrid(surfaceProvider: BenchmarkSurfaceProvider): Long {
    // Parse all icon paths
    val icons = (0 until 50).map { i ->
        Path.makeFromSVGString(iconSvgPaths[i % iconSvgPaths.size])
    }

    try {
        return surfaceProvider.withSurface(1024, 1024) { surface ->
            Paint().use { fillPaint ->
                Paint().use { strokePaint ->
                    val canvas = surface.canvas
                    fillPaint.isAntiAlias = true
                    fillPaint.mode = PaintMode.FILL
                    strokePaint.isAntiAlias = true
                    strokePaint.mode = PaintMode.STROKE
                    strokePaint.strokeWidth = 1.5f
                    var checksum = 0L

                    repeat(20) { frame ->
                        canvas.clear(0xFFFFFFFF.toInt())

                        icons.forEachIndexed { index, icon ->
                            val col = index % 10
                            val row = index / 10
                            val x = 20f + col * 100f
                            val y = 20f + row * 100f

                            canvas.save()
                            canvas.translate(x, y)
                            canvas.scale(3f, 3f) // Scale 24x24 icons to 72x72

                            val r = ((index * 47 + frame * 13) % 200 + 40)
                            val g = ((index * 71 + frame * 17) % 200 + 40)
                            val b = ((index * 97 + frame * 23) % 200 + 40)
                            fillPaint.color = 0xDD000000.toInt() or (r shl 16) or (g shl 8) or b
                            canvas.drawPath(icon, fillPaint)

                            strokePaint.color = 0xFF000000.toInt() or ((r / 2) shl 16) or ((g / 2) shl 8) or (b / 2)
                            canvas.drawPath(icon, strokePaint)

                            canvas.restore()
                            checksum += icon.pointsCount.toLong() + index
                        }
                    }
                    surface.flushAndSubmit()
                    checksum
                }
            }
        }
    } finally {
        icons.forEach { it.close() }
    }
}
