package org.jetbrains.skiko.benchmarks.cases.simd

import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.Point
import org.jetbrains.skia.RRect
import org.jetbrains.skia.TextBlob
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider
import kotlin.math.roundToLong

/**
 * Renders 30 list items (rect background + text + small icon path), shifting positions
 * each frame to simulate scrolling. Tests combined text + shape + compositing path
 * that dominates mobile/web UIs.
 */
val uiListScrollSimulationBenchmark = BenchmarkCase("ui_list_scroll_simulation") {
    runUIListScroll(RasterBenchmarkSurfaceProvider)
}

val uiListScrollSimulationGpuBenchmark = BenchmarkCase("ui_list_scroll_simulation_gpu",
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runUIListScroll(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runUIListScroll(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return Font(null, 14f).use { font ->
        // Pre-build text blobs for list items
        val itemLabels = (0 until 30).mapNotNull { i ->
            val text = "List item #${i + 1} - Description text here"
            val glyphs = ShortArray(text.length) { (text[it].code).toShort() }
            val positions = Array(glyphs.size) { idx -> Point(idx * 8f, 0f) }
            TextBlob.makeFromPos(glyphs, positions, font)
        }

        // Small icon path (chevron arrow)
        val iconPath = Path.makeFromSVGString("M4,2 L12,10 L4,18")

        try {
            surfaceProvider.withSurface(512, 1024) { surface ->
                Paint().use { bgPaint ->
                    Paint().use { textPaint ->
                        Paint().use { iconPaint ->
                            Paint().use { dividerPaint ->
                                val canvas = surface.canvas
                                var checksum = 0L
                                val itemHeight = 56f

                                textPaint.color = 0xFF212121.toInt()
                                textPaint.isAntiAlias = true
                                iconPaint.isAntiAlias = true
                                iconPaint.mode = PaintMode.STROKE
                                iconPaint.strokeWidth = 2f
                                iconPaint.color = 0xFF757575.toInt()
                                dividerPaint.color = 0xFFE0E0E0.toInt()

                                repeat(40) { frame ->
                                    canvas.clear(0xFFFAFAFA.toInt())
                                    val scrollOffset = (frame * 8f) % (30 * itemHeight)

                                    for (i in 0 until 30) {
                                        val y = i * itemHeight - scrollOffset
                                        if (y < -itemHeight || y > 1024f) continue

                                        // Item background
                                        val bgAlpha = if (i % 2 == 0) 0xFFFFFFFF.toInt() else 0xFFF5F5F5.toInt()
                                        bgPaint.color = bgAlpha
                                        canvas.drawRRect(
                                            RRect.makeXYWH(0f, y, 512f, itemHeight, 0f),
                                            bgPaint
                                        )

                                        // Text label
                                        if (i < itemLabels.size) {
                                            canvas.drawTextBlob(itemLabels[i], 56f, y + 34f, textPaint)
                                        }

                                        // Icon (chevron)
                                        canvas.save()
                                        canvas.translate(480f, y + 19f)
                                        canvas.drawPath(iconPath, iconPaint)
                                        canvas.restore()

                                        // Divider line
                                        canvas.drawLine(56f, y + itemHeight - 1f, 512f, y + itemHeight - 1f, dividerPaint)

                                        checksum += (y + i).toLong()
                                    }
                                }
                                surface.flushAndSubmit()
                                checksum
                            }
                        }
                    }
                }
            }
        } finally {
            itemLabels.forEach { it.close() }
            iconPath.close()
        }
    }
}
