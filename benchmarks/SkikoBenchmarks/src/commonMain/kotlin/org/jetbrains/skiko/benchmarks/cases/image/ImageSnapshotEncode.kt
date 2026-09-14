package org.jetbrains.skiko.benchmarks.cases.image

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.benchmarks.BenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.BenchmarkCase
import org.jetbrains.skiko.benchmarks.GpuBenchmarkSurfaceProvider
import org.jetbrains.skiko.benchmarks.RasterBenchmarkSurfaceProvider

val imageSnapshotEncodeBenchmark = BenchmarkCase("image_snapshot_encode", warmups = 2, iterations = 10) {
    runImageSnapshotEncode(RasterBenchmarkSurfaceProvider)
}

val imageSnapshotEncodeGpuBenchmark = BenchmarkCase("image_snapshot_encode_gpu",
    warmups = 2,
    iterations = 10,
    isSupported = { GpuBenchmarkSurfaceProvider.isSupported() },
    tearDown = { GpuBenchmarkSurfaceProvider.close() },
) {
    runImageSnapshotEncode(GpuBenchmarkSurfaceProvider.get()!!)
}

private fun runImageSnapshotEncode(surfaceProvider: BenchmarkSurfaceProvider): Long {
    return surfaceProvider.withSurface(256, 256) { surface ->
        Paint().use { paint ->
            val canvas = surface.canvas
            canvas.clear(0xFFFFFFFF.toInt())
            paint.mode = PaintMode.FILL
            repeat(64) { index ->
                val left = (index % 8) * 32f
                val top = (index / 8) * 32f
                paint.color = 0xFF000000.toInt() or (index * 1_313_131 and 0x00FFFFFF)
                canvas.drawOval(left, top, left + 28f, top + 28f, paint)
            }
            surface.flushAndSubmit()

            var checksum = 0L
            repeat(2) {
                surface.makeImageSnapshot().use { image ->
                    val data = image.encodeSurfaceSnapshotToData(surface)
                        ?: error("Failed to encode benchmark image")
                    data.use {
                        checksum += it.size
                    }
                }
            }
            checksum
        }
    }
}

private fun Image.encodeSurfaceSnapshotToData(surface: Surface): Data? {
    if (surface.recordingContext == null) {
        return encodeToData(EncodedImageFormat.PNG, 100)
    }

    // GPU-backed snapshots may not be encodable directly on wasm, so read back to raster pixels first.
    return Bitmap().use { bitmap ->
        bitmap.allocPixels(imageInfo)
        check(surface.readPixels(bitmap, 0, 0)) { "Failed to read pixels from GPU surface" }
        Image.makeFromBitmap(bitmap).use { rasterImage ->
            rasterImage.encodeToData(EncodedImageFormat.PNG, 100)
        }
    }
}
