package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Font
import org.jetbrains.skia.Gradient
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.Point
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader
import org.jetbrains.skia.TextBlob
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider
import kotlin.math.roundToLong

/**
 * Simulates a dashboard UI: background gradient, 20 rounded-rect cards with shadows,
 * text labels on each card, a few icon paths, and a chart (lines + circles).
 * Establishes real-world net impact on UI frame budgets.
 */
val uiDashboardSimulationBenchmark = BenchmarkCase("ui_dashboard_simulation") {
    runUIDashboard(RasterBenchmarkSurfaceProvider)
}

val uiDashboardSimulationGpuBenchmark = BenchmarkCase("ui_dashboard_simulation_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runUIDashboard(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runUIDashboard(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return Font(null, 14f).use { font ->
        // Pre-build text blobs for card labels
        val labels = (0 until 20).mapNotNull { i ->
            val text = "Card ${i + 1}"
            val glyphs = ShortArray(text.length) { (text[it].code).toShort() }
            val positions = Array(glyphs.size) { idx -> Point(idx * 9f, 0f) }
            TextBlob.makeFromPos(glyphs, positions, font)
        }

        // Pre-build a simple chart path
        val chartPath = Path.makeFromSVGString(
            "M50,700 L150,650 L250,680 L350,600 L450,620 L550,550 L650,580 L750,500 L850,530 L950,480"
        )

        try {
            surfaceProvider.withSurface(1024, 1024) { surface ->
                Paint().use { bgPaint ->
                    Paint().use { cardPaint ->
                        Paint().use { shadowPaint ->
                            Paint().use { textPaint ->
                                Paint().use { chartPaint ->
                                    val canvas = surface.canvas
                                    var checksum = 0L

                                    // Background gradient colors
                                    val bgStops = arrayOf(
                                        Color4f(0.1f, 0.1f, 0.2f, 1f),
                                        Color4f(0.15f, 0.15f, 0.3f, 1f),
                                    )
                                    val bgPositions = floatArrayOf(0f, 1f)

                                    shadowPaint.color = 0x40000000
                                    shadowPaint.maskFilter = MaskFilter.makeBlur(org.jetbrains.skia.FilterBlurMode.NORMAL, 4f)
                                    textPaint.color = 0xFFFFFFFF.toInt()
                                    textPaint.isAntiAlias = true
                                    chartPaint.isAntiAlias = true
                                    chartPaint.mode = PaintMode.STROKE
                                    chartPaint.strokeWidth = 2f

                                    repeat(20) { frame ->
                                        canvas.clear(0xFF1A1A2E.toInt())

                                        // Background gradient
                                        val gradient = Gradient(
                                            Gradient.Colors(bgStops, bgPositions, FilterTileMode.CLAMP)
                                        )
                                        Shader.makeLinearGradient(
                                            Point(0f, 0f), Point(1024f, 1024f), gradient
                                        ).use { shader ->
                                            bgPaint.shader = shader
                                            canvas.drawRect(Rect(0f, 0f, 1024f, 1024f), bgPaint)
                                        }
                                        bgPaint.shader = null

                                        // 20 cards in a 4x5 grid
                                        for (i in 0 until 20) {
                                            val col = i % 4
                                            val row = i / 4
                                            val cx = 30f + col * 250f
                                            val cy = 30f + row * 120f

                                            // Shadow
                                            canvas.drawRRect(
                                                RRect.makeXYWH(cx + 3f, cy + 3f, 220f, 100f, 8f),
                                                shadowPaint
                                            )
                                            // Card background
                                            val cardR = ((i * 31 + 60) % 80 + 40)
                                            val cardG = ((i * 47 + 80) % 80 + 40)
                                            val cardB = ((i * 67 + 100) % 80 + 80)
                                            cardPaint.color = 0xFF000000.toInt() or (cardR shl 16) or (cardG shl 8) or cardB
                                            canvas.drawRRect(
                                                RRect.makeXYWH(cx, cy, 220f, 100f, 8f),
                                                cardPaint
                                            )
                                            // Label
                                            if (i < labels.size) {
                                                canvas.drawTextBlob(labels[i], cx + 12f, cy + 30f, textPaint)
                                            }
                                            checksum += (cx + cy).toLong()
                                        }

                                        // Chart line
                                        chartPaint.color = 0xFF4CAF50.toInt()
                                        canvas.drawPath(chartPath, chartPaint)

                                        // Chart data points (circles)
                                        chartPaint.mode = PaintMode.FILL
                                        for (j in 0 until 10) {
                                            val px = 50f + j * 100f
                                            val py = 700f - (j * 20f + frame * 2f) % 220f
                                            canvas.drawCircle(px, py, 5f, chartPaint)
                                        }
                                        chartPaint.mode = PaintMode.STROKE

                                        checksum += frame.toLong()
                                    }
                                    surface.flushAndSubmit()
                                    checksum
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            labels.forEach { it.close() }
            chartPath.close()
        }
    }
}
